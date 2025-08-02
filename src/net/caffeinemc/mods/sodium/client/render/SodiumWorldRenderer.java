package net.caffeinemc.mods.sodium.client.render;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.function.Consumer;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker;
import net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTrackerHolder;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger.CameraMovement;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.caffeinemc.mods.sodium.client.util.NativeBuffer;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
import net.caffeinemc.mods.sodium.client.world.LevelRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.core.SectionPos;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class SodiumWorldRenderer {
   private final Minecraft client;
   private Level level;
   private int renderDistance;
   private Vector3d lastCameraPos;
   private double lastCameraPitch;
   private double lastCameraYaw;
   private float lastFogDistance;
   private Matrix4f lastProjectionMatrix;
   private boolean useEntityCulling;
   private RenderSectionManager renderSectionManager;
   private static final double MAX_ENTITY_CHECK_VOLUME = 61440.0;

   public static SodiumWorldRenderer instance() {
      SodiumWorldRenderer instance = instanceNullable();
      if (instance == null) {
         throw new IllegalStateException("No renderer attached to active level");
      } else {
         return instance;
      }
   }

   public static SodiumWorldRenderer instanceNullable() {
      return Minecraft.getInstance().levelRenderer instanceof LevelRendererExtension extension ? extension.sodium$getWorldRenderer() : null;
   }

   public SodiumWorldRenderer(Minecraft client) {
      this.client = client;
   }

   public void setLevel(Level level) {
      if (this.level != level) {
         if (this.level != null) {
            this.unloadLevel();
         }

         if (level != null) {
            this.loadLevel(level);
         }
      }
   }

   private void loadLevel(Level level) {
      this.level = level;

      try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
         this.initRenderer(commandList);
      }
   }

   private void unloadLevel() {
      if (this.renderSectionManager != null) {
         this.renderSectionManager.destroy();
         this.renderSectionManager = null;
      }

      this.level = null;
   }

   public int getVisibleChunkCount() {
      return this.renderSectionManager.getVisibleChunkCount();
   }

   public void scheduleTerrainUpdate() {
      if (this.renderSectionManager != null) {
         this.renderSectionManager.markGraphDirty();
      }
   }

   public boolean isTerrainRenderComplete() {
      return this.renderSectionManager.getBuilder().isBuildQueueEmpty();
   }

   public void setupTerrain(
      Camera camera, Viewport viewport, FogParameters fogParameters, boolean spectator, boolean updateChunksImmediately, ChunkRenderMatrices matrices
   ) {
      NativeBuffer.reclaim(false);
      this.processChunkEvents();
      this.useEntityCulling = SodiumClientMod.options().performance.useEntityCulling;
      if (this.client.options.getEffectiveRenderDistance() != this.renderDistance) {
         this.reload();
      }

      ProfilerFiller profiler = Profiler.get();
      profiler.push("camera_setup");
      LocalPlayer player = this.client.player;
      if (player == null) {
         throw new IllegalStateException("Client instance has no active player entity");
      } else {
         Vec3 posRaw = camera.getPosition();
         Vector3d pos = new Vector3d(posRaw.x(), posRaw.y(), posRaw.z());
         float pitch = camera.getXRot();
         float yaw = camera.getYRot();
         float fogDistance = fogParameters.renderEnd();
         if (this.lastCameraPos == null) {
            this.lastCameraPos = new Vector3d(pos);
         }

         if (this.lastProjectionMatrix == null) {
            this.lastProjectionMatrix = new Matrix4f(matrices.projection());
         }

         boolean cameraLocationChanged = !pos.equals(this.lastCameraPos);
         boolean cameraAngleChanged = pitch != this.lastCameraPitch || yaw != this.lastCameraYaw || fogDistance != this.lastFogDistance;
         boolean cameraProjectionChanged = !matrices.projection().equals(this.lastProjectionMatrix);
         this.lastProjectionMatrix.set(matrices.projection());
         this.lastCameraPitch = pitch;
         this.lastCameraYaw = yaw;
         if (cameraLocationChanged || cameraAngleChanged || cameraProjectionChanged) {
            this.renderSectionManager.markGraphDirty();
         }

         this.lastFogDistance = fogDistance;
         this.renderSectionManager.updateCameraState(pos, camera);
         if (cameraLocationChanged) {
            profiler.popPush("translucent_triggering");
            this.renderSectionManager.processGFNIMovement(new CameraMovement(this.lastCameraPos, pos));
            this.lastCameraPos = new Vector3d(pos);
         }

         int maxChunkUpdates = updateChunksImmediately ? this.renderDistance : 1;

         for (int i = 0; i < maxChunkUpdates; i++) {
            if (this.renderSectionManager.needsUpdate()) {
               profiler.popPush("chunk_render_lists");
               this.renderSectionManager.update(camera, viewport, fogParameters, spectator);
            }

            profiler.popPush("chunk_update");
            this.renderSectionManager.cleanupAndFlip();
            this.renderSectionManager.updateChunks(updateChunksImmediately);
            profiler.popPush("chunk_upload");
            this.renderSectionManager.uploadChunks();
            if (!this.renderSectionManager.needsUpdate()) {
               break;
            }
         }

         profiler.popPush("chunk_render_tick");
         this.renderSectionManager.tickVisibleRenders();
         profiler.pop();
         Entity.setViewScale(
            Mth.clamp(this.client.options.getEffectiveRenderDistance() / 8.0, 1.0, 2.5) * (Double)this.client.options.entityDistanceScaling().get()
         );
      }
   }

   private void processChunkEvents() {
      ChunkTracker tracker = ChunkTrackerHolder.get(this.level);
      tracker.forEachEvent(this.renderSectionManager::onChunkAdded, this.renderSectionManager::onChunkRemoved);
   }

   public void drawChunkLayer(ChunkSectionLayerGroup group, ChunkRenderMatrices matrices, double x, double y, double z) {
      if (group == ChunkSectionLayerGroup.OPAQUE) {
         this.renderSectionManager.renderLayer(matrices, DefaultTerrainRenderPasses.SOLID, x, y, z);
         this.renderSectionManager.renderLayer(matrices, DefaultTerrainRenderPasses.CUTOUT, x, y, z);
      } else if (group == ChunkSectionLayerGroup.TRANSLUCENT) {
         this.renderSectionManager.renderLayer(matrices, DefaultTerrainRenderPasses.TRANSLUCENT, x, y, z);
      }
   }

   public void reload() {
      if (this.level != null) {
         try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
            this.initRenderer(commandList);
         }
      }
   }

   private void initRenderer(CommandList commandList) {
      if (this.renderSectionManager != null) {
         this.renderSectionManager.destroy();
         this.renderSectionManager = null;
      }

      this.renderDistance = this.client.options.getEffectiveRenderDistance();
      this.renderSectionManager = new RenderSectionManager(this.level, this.renderDistance, commandList);
      ChunkTracker tracker = ChunkTrackerHolder.get(this.level);
      ChunkTracker.forEachChunk(tracker.getReadyChunks(), this.renderSectionManager::onChunkAdded);
   }

   public void renderBlockEntities(
      PoseStack matrices,
      RenderBuffers bufferBuilders,
      Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
      Camera camera,
      float tickDelta,
      LocalBooleanRef isGlowing
   ) {
      BufferSource immediate = bufferBuilders.bufferSource();
      Vec3 cameraPos = camera.getPosition();
      double x = cameraPos.x();
      double y = cameraPos.y();
      double z = cameraPos.z();
      LocalPlayer player = this.client.player;
      if (player == null) {
         throw new IllegalStateException("Client instance has no active player entity");
      } else {
         BlockEntityRenderDispatcher blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();
         this.renderBlockEntities(matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, blockEntityRenderer, player, isGlowing);
         this.renderGlobalBlockEntities(
            matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, blockEntityRenderer, player, isGlowing
         );
      }
   }

   private void renderBlockEntities(
      PoseStack matrices,
      RenderBuffers bufferBuilders,
      Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
      float tickDelta,
      BufferSource immediate,
      double x,
      double y,
      double z,
      BlockEntityRenderDispatcher blockEntityRenderer,
      LocalPlayer player,
      LocalBooleanRef isGlowing
   ) {
      SortedRenderLists renderLists = this.renderSectionManager.getRenderLists();
      Iterator<ChunkRenderList> renderListIterator = renderLists.iterator();

      while (renderListIterator.hasNext()) {
         var renderList = renderListIterator.next();
         RenderRegion renderRegion = renderList.getRegion();
         ByteIterator renderSectionIterator = renderList.sectionsWithEntitiesIterator();
         if (renderSectionIterator != null) {
            while (renderSectionIterator.hasNext()) {
               int renderSectionId = renderSectionIterator.nextByteAsInt();
               RenderSection renderSection = renderRegion.getSection(renderSectionId);
               BlockEntity[] blockEntities = renderSection.getCulledBlockEntities();
               if (blockEntities != null) {
                  for (BlockEntity blockEntity : blockEntities) {
                     renderBlockEntity(
                        matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, blockEntityRenderer, blockEntity, player, isGlowing
                     );
                  }
               }
            }
         }
      }
   }

   private void renderGlobalBlockEntities(
      PoseStack matrices,
      RenderBuffers bufferBuilders,
      Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
      float tickDelta,
      BufferSource immediate,
      double x,
      double y,
      double z,
      BlockEntityRenderDispatcher blockEntityRenderer,
      LocalPlayer player,
      LocalBooleanRef isGlowing
   ) {
      for (RenderSection renderSection : this.renderSectionManager.getSectionsWithGlobalEntities()) {
         BlockEntity[] blockEntities = renderSection.getGlobalBlockEntities();
         if (blockEntities != null) {
            for (BlockEntity blockEntity : blockEntities) {
               renderBlockEntity(
                  matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x, y, z, blockEntityRenderer, blockEntity, player, isGlowing
               );
            }
         }
      }
   }

   private static void renderBlockEntity(
      PoseStack matrices,
      RenderBuffers bufferBuilders,
      Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
      float tickDelta,
      BufferSource immediate,
      double x,
      double y,
      double z,
      BlockEntityRenderDispatcher dispatcher,
      BlockEntity entity,
      LocalPlayer player,
      LocalBooleanRef isGlowing
   ) {
      BlockPos pos = entity.getBlockPos();
      matrices.pushPose();
      matrices.translate(pos.getX() - x, pos.getY() - y, pos.getZ() - z);
      MultiBufferSource consumer = immediate;
      SortedSet<BlockDestructionProgress> breakingInfo = (SortedSet<BlockDestructionProgress>)blockBreakingProgressions.get(pos.asLong());
      if (breakingInfo != null && !breakingInfo.isEmpty()) {
         int stage = breakingInfo.last().getProgress();
         if (stage >= 0) {
            VertexConsumer bufferBuilder = bufferBuilders.crumblingBufferSource().getBuffer((RenderType) ModelBakery.DESTROY_TYPES.get(stage));
            Pose entry = matrices.last();
            VertexConsumer transformer = new SheetedDecalTextureGenerator(bufferBuilder, entry, 1.0F);
            consumer = layer -> layer.affectsCrumbling() ? VertexMultiConsumer.create(transformer, immediate.getBuffer(layer)) : immediate.getBuffer(layer);
         }
      }

      dispatcher.render(entity, tickDelta, matrices, consumer);
      if (isGlowing != null && PlatformBlockAccess.getInstance().shouldBlockEntityGlow(entity, player)) {
         isGlowing.set(true);
      }

      matrices.popPose();
   }

   public void iterateVisibleBlockEntities(Consumer<BlockEntity> blockEntityConsumer) {
      SortedRenderLists renderLists = this.renderSectionManager.getRenderLists();
      Iterator<ChunkRenderList> renderListIterator = renderLists.iterator();

      while (renderListIterator.hasNext()) {
         var renderList = renderListIterator.next();
         RenderRegion renderRegion = renderList.getRegion();
         ByteIterator renderSectionIterator = renderList.sectionsWithEntitiesIterator();
         if (renderSectionIterator != null) {
            while (renderSectionIterator.hasNext()) {
               int renderSectionId = renderSectionIterator.nextByteAsInt();
               RenderSection renderSection = renderRegion.getSection(renderSectionId);
               BlockEntity[] blockEntities = renderSection.getCulledBlockEntities();
               if (blockEntities != null) {
                  for (BlockEntity blockEntity : blockEntities) {
                     blockEntityConsumer.accept(blockEntity);
                  }
               }
            }
         }
      }

      for (RenderSection renderSection : this.renderSectionManager.getSectionsWithGlobalEntities()) {
         BlockEntity[] blockEntities = renderSection.getGlobalBlockEntities();
         if (blockEntities != null) {
            for (BlockEntity blockEntity : blockEntities) {
               blockEntityConsumer.accept(blockEntity);
            }
         }
      }
   }

   public <T extends Entity, S extends EntityRenderState> boolean isEntityVisible(EntityRenderer<T, S> renderer, T entity) {
      if (!this.useEntityCulling) {
         return true;
      } else if (!this.client.shouldEntityAppearGlowing(entity) && !entity.shouldShowName()) {
         AABB bb = renderer.cullAABB(entity);
         double entityVolume = (bb.maxX - bb.minX) * (bb.maxY - bb.minY) * (bb.maxZ - bb.minZ);
         return entityVolume > 61440.0 ? true : this.isBoxVisible(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
      } else {
         return true;
      }
   }

   public boolean isBoxVisible(double x1, double y1, double z1, double x2, double y2, double z2) {
      if (!(y2 < this.level.getMinY() + 0.5) && !(y1 > this.level.getMaxY() - 0.5)) {
         int minX = SectionPos.posToSectionCoord(x1 - 0.5);
         int minY = SectionPos.posToSectionCoord(y1 - 0.5);
         int minZ = SectionPos.posToSectionCoord(z1 - 0.5);
         int maxX = SectionPos.posToSectionCoord(x2 + 0.5);
         int maxY = SectionPos.posToSectionCoord(y2 + 0.5);
         int maxZ = SectionPos.posToSectionCoord(z2 + 0.5);

         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               for (int y = minY; y <= maxY; y++) {
                  if (this.renderSectionManager.isSectionVisible(x, y, z)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public String getChunksDebugString() {
      return String.format(
         "C: %d/%d D: %d", this.renderSectionManager.getVisibleChunkCount(), this.renderSectionManager.getTotalSections(), this.renderDistance
      );
   }

   public void scheduleRebuildForBlockArea(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
      this.scheduleRebuildForChunks(minX >> 4, minY >> 4, minZ >> 4, maxX >> 4, maxY >> 4, maxZ >> 4, important);
   }

   public void scheduleRebuildForChunks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
      for (int chunkX = minX; chunkX <= maxX; chunkX++) {
         for (int chunkY = minY; chunkY <= maxY; chunkY++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
               this.scheduleRebuildForChunk(chunkX, chunkY, chunkZ, important);
            }
         }
      }
   }

   public void scheduleRebuildForChunk(int x, int y, int z, boolean important) {
      this.renderSectionManager.scheduleRebuild(x, y, z, important);
   }

   public Collection<String> getDebugStrings() {
      return this.renderSectionManager.getDebugStrings();
   }

   public boolean isSectionReady(int x, int y, int z) {
      return this.renderSectionManager.isSectionBuilt(x, y, z);
   }
}
