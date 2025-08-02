package net.caffeinemc.mods.sodium.client.render.chunk;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMaps;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.arena.GlBufferArena;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.BuilderTaskOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkSortOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkJobCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkJobResult;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkJobTyped;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderSortingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderTask;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.VisibleChunkCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.GraphDirection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegion;
import net.caffeinemc.mods.sodium.client.render.chunk.region.RenderRegionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.DynamicTopoData;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.NoData;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.TranslucentData;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger.CameraMovement;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.trigger.SortTriggering;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkMeshFormats;
import net.caffeinemc.mods.sodium.client.render.util.RenderAsserts;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.caffeinemc.mods.sodium.client.util.MathUtil;
import net.caffeinemc.mods.sodium.client.util.iterator.ByteIterator;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.util.Mth;
import net.minecraft.core.SectionPos;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

public class RenderSectionManager {
   private final ChunkBuilder builder;
   private final RenderRegionManager regions;
   private final ClonedChunkSectionCache sectionCache;
   private final Long2ReferenceMap<RenderSection> sectionByPosition = new Long2ReferenceOpenHashMap();
   private final ConcurrentLinkedDeque<ChunkJobResult<? extends BuilderTaskOutput>> buildResults = new ConcurrentLinkedDeque<>();
   private final ChunkRenderer chunkRenderer;
   private final Level level;
   private final ReferenceSet<RenderSection> sectionsWithGlobalEntities = new ReferenceOpenHashSet();
   private final OcclusionCuller occlusionCuller;
   private final int renderDistance;
   private final SortTriggering sortTriggering;
   private ChunkJobCollector lastBlockingCollector;
   @NotNull
   private SortedRenderLists renderLists;
   @NotNull
   private Map<ChunkUpdateType, ArrayDeque<RenderSection>> taskLists;
   private int lastUpdatedFrame;
   private boolean needsGraphUpdate;
   @Nullable
   private BlockPos cameraBlockPos;
   @Nullable
   private Vector3dc cameraPosition;
   private FogParameters lastFogParameters = FogParameters.NONE;
   private static final float NEARBY_REBUILD_DISTANCE = Mth.square(16.0F);
   private static final float NEARBY_SORT_DISTANCE = Mth.square(25.0F);

   public RenderSectionManager(Level level, int renderDistance, CommandList commandList) {
      this.chunkRenderer = new DefaultChunkRenderer(RenderDevice.INSTANCE, ChunkMeshFormats.COMPACT);
      this.level = level;
      this.builder = new ChunkBuilder(level, ChunkMeshFormats.COMPACT);
      this.needsGraphUpdate = true;
      this.renderDistance = renderDistance;
      this.sortTriggering = new SortTriggering();
      this.regions = new RenderRegionManager(commandList);
      this.sectionCache = new ClonedChunkSectionCache(this.level);
      this.renderLists = SortedRenderLists.empty();
      this.occlusionCuller = new OcclusionCuller(Long2ReferenceMaps.unmodifiable(this.sectionByPosition), this.level);
      this.taskLists = new EnumMap<>(ChunkUpdateType.class);

      for (ChunkUpdateType type : ChunkUpdateType.values()) {
         this.taskLists.put(type, new ArrayDeque<>());
      }
   }

   public void updateCameraState(Vector3dc cameraPosition, Camera camera) {
      this.cameraBlockPos = camera.getBlockPosition();
      this.cameraPosition = cameraPosition;
   }

   public void update(Camera camera, Viewport viewport, FogParameters fogParameters, boolean spectator) {
      this.lastUpdatedFrame++;
      this.lastFogParameters = fogParameters;
      this.createTerrainRenderList(camera, viewport, fogParameters, this.lastUpdatedFrame, spectator);
      this.needsGraphUpdate = false;
   }

   private void createTerrainRenderList(Camera camera, Viewport viewport, FogParameters fogParameters, int frame, boolean spectator) {
      this.resetRenderLists();
      float searchDistance = this.getSearchDistance(fogParameters);
      boolean useOcclusionCulling = this.shouldUseOcclusionCulling(camera, spectator);
      VisibleChunkCollector visitor = new VisibleChunkCollector(frame);
      this.occlusionCuller.findVisible(visitor, viewport, searchDistance, useOcclusionCulling, frame);
      this.renderLists = visitor.createRenderLists(viewport);
      this.taskLists = visitor.getRebuildLists();
   }

   private float getSearchDistance(FogParameters fogParameters) {
      float distance;
      if (SodiumClientMod.options().performance.useFogOcclusion) {
         distance = this.getEffectiveRenderDistance(fogParameters);
      } else {
         distance = this.getRenderDistance();
      }

      return distance;
   }

   private boolean shouldUseOcclusionCulling(Camera camera, boolean spectator) {
      BlockPos origin = camera.getBlockPosition();
      boolean useOcclusionCulling;
      if (spectator && this.level.getBlockState(origin).isSolidRender()) {
         useOcclusionCulling = false;
      } else {
         useOcclusionCulling = Minecraft.getInstance().smartCull;
      }

      return useOcclusionCulling;
   }

   private void resetRenderLists() {
      this.renderLists = SortedRenderLists.empty();

      for (ArrayDeque<RenderSection> list : this.taskLists.values()) {
         list.clear();
      }
   }

   public void onSectionAdded(int x, int y, int z) {
      long key = SectionPos.asLong(x, y, z);
      if (!this.sectionByPosition.containsKey(key)) {
         RenderRegion region = this.regions.createForChunk(x, y, z);
         RenderSection renderSection = new RenderSection(region, x, y, z);
         region.addSection(renderSection);
         this.sectionByPosition.put(key, renderSection);
         ChunkAccess chunk = this.level.getChunk(x, z);
         LevelChunkSection section = chunk.getSections()[this.level.getSectionIndexFromSectionY(y)];
         if (section.hasOnlyAir()) {
            this.updateSectionInfo(renderSection, BuiltSectionInfo.EMPTY);
         } else {
            renderSection.setPendingUpdate(ChunkUpdateType.INITIAL_BUILD);
         }

         this.connectNeighborNodes(renderSection);
         this.needsGraphUpdate = true;
      }
   }

   public void onSectionRemoved(int x, int y, int z) {
      long sectionPos = SectionPos.asLong(x, y, z);
      RenderSection section = (RenderSection)this.sectionByPosition.remove(sectionPos);
      if (section != null) {
         if (section.getTranslucentData() != null) {
            this.sortTriggering.removeSection(section.getTranslucentData(), sectionPos);
         }

         RenderRegion region = section.getRegion();
         if (region != null) {
            region.removeSection(section);
         }

         this.disconnectNeighborNodes(section);
         this.updateSectionInfo(section, null);
         section.delete();
         this.needsGraphUpdate = true;
      }
   }

   public void renderLayer(ChunkRenderMatrices matrices, TerrainRenderPass pass, double x, double y, double z) {
      RenderDevice device = RenderDevice.INSTANCE;
      CommandList commandList = device.createCommandList();
      this.chunkRenderer.render(matrices, commandList, this.renderLists, pass, new CameraTransform(x, y, z), this.lastFogParameters);
      commandList.flush();
   }

   public void tickVisibleRenders() {
      Iterator<ChunkRenderList> it = this.renderLists.iterator();

      while (it.hasNext()) {
         ChunkRenderList renderList = it.next();
         RenderRegion region = renderList.getRegion();
         ByteIterator iterator = renderList.sectionsWithSpritesIterator();
         if (iterator != null) {
            while (iterator.hasNext()) {
               RenderSection section = region.getSection(iterator.nextByteAsInt());
               if (section != null) {
                  TextureAtlasSprite[] sprites = section.getAnimatedSprites();
                  if (sprites != null) {
                     for (TextureAtlasSprite sprite : sprites) {
                        SpriteUtil.INSTANCE.markSpriteActive(sprite);
                     }
                  }
               }
            }
         }
      }
   }

   public boolean isSectionVisible(int x, int y, int z) {
      RenderSection render = this.getRenderSection(x, y, z);
      return render == null ? false : render.getLastVisibleFrame() == this.lastUpdatedFrame;
   }

   public void uploadChunks() {
      ArrayList<BuilderTaskOutput> results = this.collectChunkBuildResults();
      if (!results.isEmpty()) {
         this.needsGraphUpdate = this.needsGraphUpdate | this.processChunkBuildResults(results);

         for (BuilderTaskOutput result : results) {
            result.destroy();
         }
      }
   }

   private boolean processChunkBuildResults(ArrayList<BuilderTaskOutput> results) {
      List<BuilderTaskOutput> filtered = filterChunkBuildResults(results);
      this.regions.uploadResults(RenderDevice.INSTANCE.createCommandList(), filtered);
      boolean touchedSectionInfo = false;

      for (BuilderTaskOutput result : filtered) {
         TranslucentData oldData = result.render.getTranslucentData();
         if (result instanceof ChunkBuildOutput chunkBuildOutput) {
            touchedSectionInfo |= this.updateSectionInfo(result.render, chunkBuildOutput.info);
            if (chunkBuildOutput.translucentData != null) {
               this.sortTriggering.integrateTranslucentData(oldData, chunkBuildOutput.translucentData, this.cameraPosition, this::scheduleSort);
               result.render.setTranslucentData(chunkBuildOutput.translucentData);
            }
         } else if (result instanceof ChunkSortOutput sortOutput
            && sortOutput.getTopoSorter() != null
            && result.render.getTranslucentData() instanceof DynamicTopoData data) {
            this.sortTriggering.applyTriggerChanges(data, sortOutput.getTopoSorter(), result.render.getPosition(), this.cameraPosition);
         }

         CancellationToken job = result.render.getTaskCancellationToken();
         if (job != null && result.submitTime >= result.render.getLastSubmittedFrame()) {
            result.render.setTaskCancellationToken(null);
         }

         result.render.setLastUploadFrame(result.submitTime);
      }

      return touchedSectionInfo;
   }

   private boolean updateSectionInfo(RenderSection render, BuiltSectionInfo info) {
      boolean infoChanged = render.setInfo(info);
      return info != null && !ArrayUtils.isEmpty(info.globalBlockEntities)
         ? this.sectionsWithGlobalEntities.add(render) || infoChanged
         : this.sectionsWithGlobalEntities.remove(render) || infoChanged;
   }

   private static List<BuilderTaskOutput> filterChunkBuildResults(ArrayList<BuilderTaskOutput> outputs) {
      Reference2ReferenceLinkedOpenHashMap<RenderSection, BuilderTaskOutput> map = new Reference2ReferenceLinkedOpenHashMap();

      for (BuilderTaskOutput output : outputs) {
         if (!output.render.isDisposed() && output.render.getLastUploadFrame() <= output.submitTime) {
            RenderSection render = output.render;
            BuilderTaskOutput previous = (BuilderTaskOutput)map.get(render);
            if (previous == null || previous.submitTime < output.submitTime) {
               map.put(render, output);
            }
         }
      }

      return new ArrayList<>(map.values());
   }

   private ArrayList<BuilderTaskOutput> collectChunkBuildResults() {
      ArrayList<BuilderTaskOutput> results = new ArrayList<>();

      ChunkJobResult<? extends BuilderTaskOutput> result;
      while ((result = this.buildResults.poll()) != null) {
         results.add(result.unwrap());
      }

      return results;
   }

   public void cleanupAndFlip() {
      this.sectionCache.cleanup();
      this.regions.update();
   }

   public void updateChunks(boolean updateImmediately) {
      ChunkJobCollector thisFrameBlockingCollector = this.lastBlockingCollector;
      this.lastBlockingCollector = null;
      if (thisFrameBlockingCollector == null) {
         thisFrameBlockingCollector = new ChunkJobCollector(this.buildResults::add);
      }

      if (updateImmediately) {
         this.submitSectionTasks(thisFrameBlockingCollector, thisFrameBlockingCollector, thisFrameBlockingCollector);
         thisFrameBlockingCollector.awaitCompletion(this.builder);
      } else {
         ChunkJobCollector nextFrameBlockingCollector = new ChunkJobCollector(this.buildResults::add);
         ChunkJobCollector deferredCollector = new ChunkJobCollector(
            this.builder.getHighEffortSchedulingBudget(), this.builder.getLowEffortSchedulingBudget(), this.buildResults::add
         );
         if (SodiumClientMod.options().debug.getSortBehavior().getDeferMode() == SortBehavior.DeferMode.ZERO_FRAMES) {
            this.submitSectionTasks(thisFrameBlockingCollector, nextFrameBlockingCollector, deferredCollector);
         } else {
            this.submitSectionTasks(nextFrameBlockingCollector, nextFrameBlockingCollector, deferredCollector);
         }

         thisFrameBlockingCollector.awaitCompletion(this.builder);
         this.lastBlockingCollector = nextFrameBlockingCollector;
      }
   }

   private void submitSectionTasks(ChunkJobCollector importantCollector, ChunkJobCollector semiImportantCollector, ChunkJobCollector deferredCollector) {
      this.submitSectionTasks(importantCollector, ChunkUpdateType.IMPORTANT_SORT, true);
      this.submitSectionTasks(semiImportantCollector, ChunkUpdateType.IMPORTANT_REBUILD, true);
      this.submitSectionTasks(deferredCollector, ChunkUpdateType.REBUILD, false);
      this.submitSectionTasks(deferredCollector, ChunkUpdateType.INITIAL_BUILD, false);
      this.submitSectionTasks(deferredCollector, ChunkUpdateType.SORT, true);
   }

   private void submitSectionTasks(ChunkJobCollector collector, ChunkUpdateType type, boolean ignoreEffortCategory) {
      ArrayDeque<RenderSection> queue = this.taskLists.get(type);

      while (!queue.isEmpty() && collector.hasBudgetFor(type.getTaskEffort(), ignoreEffortCategory)) {
         RenderSection section = queue.remove();
         if (!section.isDisposed()) {
            ChunkUpdateType pendingUpdate = section.getPendingUpdate();
            if (pendingUpdate == null || pendingUpdate == type) {
               int frame = this.lastUpdatedFrame;
               ChunkBuilderTask<? extends BuilderTaskOutput> task;
               if (type != ChunkUpdateType.SORT && type != ChunkUpdateType.IMPORTANT_SORT) {
                  task = this.createRebuildTask(section, frame);
                  if (task == null) {
                     ChunkJobResult<ChunkBuildOutput> result = ChunkJobResult.successfully(
                        new ChunkBuildOutput(section, frame, NoData.forEmptySection(section.getPosition()), BuiltSectionInfo.EMPTY, Collections.emptyMap())
                     );
                     this.buildResults.add(result);
                     section.setTaskCancellationToken(null);
                  }
               } else {
                  task = this.createSortTask(section, frame);
                  if (task == null) {
                     continue;
                  }
               }

               if (task != null) {
                  ChunkJobTyped<? extends ChunkBuilderTask<?>, ?> job = this.builder.scheduleTask(task, type.isImportant(), collector::onJobFinished);
                  collector.addSubmittedJob(job);
                  section.setTaskCancellationToken(job);
               }

               section.setLastSubmittedFrame(frame);
               section.setPendingUpdate(null);
            }
         }
      }
   }

   @Nullable
   public ChunkBuilderMeshingTask createRebuildTask(RenderSection render, int frame) {
      ChunkRenderContext context = LevelSlice.prepare(this.level, render.getPosition(), this.sectionCache);
      return context == null ? null : new ChunkBuilderMeshingTask(render, frame, this.cameraPosition, context);
   }

   public ChunkBuilderSortingTask createSortTask(RenderSection render, int frame) {
      return ChunkBuilderSortingTask.createTask(render, frame, this.cameraPosition);
   }

   public void processGFNIMovement(CameraMovement movement) {
      this.sortTriggering.triggerSections(this::scheduleSort, movement);
   }

   public void markGraphDirty() {
      this.needsGraphUpdate = true;
   }

   public boolean needsUpdate() {
      return this.needsGraphUpdate;
   }

   public ChunkBuilder getBuilder() {
      return this.builder;
   }

   public void destroy() {
      this.builder.shutdown();

      for (BuilderTaskOutput result : this.collectChunkBuildResults()) {
         result.destroy();
      }

      ObjectIterator var6 = this.sectionByPosition.values().iterator();

      while (var6.hasNext()) {
         RenderSection section = (RenderSection)var6.next();
         section.delete();
      }

      this.sectionsWithGlobalEntities.clear();
      this.resetRenderLists();

      try (CommandList commandList = RenderDevice.INSTANCE.createCommandList()) {
         this.regions.delete(commandList);
         this.chunkRenderer.delete(commandList);
      }
   }

   public int getTotalSections() {
      return this.sectionByPosition.size();
   }

   public int getVisibleChunkCount() {
      int sections = 0;

      var iterator = this.renderLists.iterator();

      while (iterator.hasNext()) {
         var renderList = iterator.next();
         sections += renderList.getSectionsWithGeometryCount();
      }

      return sections;
   }

   public void scheduleSort(long sectionPos, boolean isDirectTrigger) {
      RenderSection section = (RenderSection)this.sectionByPosition.get(sectionPos);
      if (section != null) {
         ChunkUpdateType pendingUpdate = ChunkUpdateType.SORT;
         SortBehavior.PriorityMode priorityMode = SodiumClientMod.options().debug.getSortBehavior().getPriorityMode();
         if (priorityMode == SortBehavior.PriorityMode.ALL
            || priorityMode == SortBehavior.PriorityMode.NEARBY && this.shouldPrioritizeTask(section, NEARBY_SORT_DISTANCE)) {
            pendingUpdate = ChunkUpdateType.IMPORTANT_SORT;
         }

         pendingUpdate = ChunkUpdateType.getPromotionUpdateType(section.getPendingUpdate(), pendingUpdate);
         if (pendingUpdate != null) {
            section.setPendingUpdate(pendingUpdate);
            section.prepareTrigger(isDirectTrigger);
         }
      }
   }

   public void scheduleRebuild(int x, int y, int z, boolean important) {
      RenderAsserts.validateCurrentThread();
      this.sectionCache.invalidate(x, y, z);
      RenderSection section = (RenderSection)this.sectionByPosition.get(SectionPos.asLong(x, y, z));
      if (section != null && section.isBuilt()) {
         ChunkUpdateType pendingUpdate;
         if (!allowImportantRebuilds() || !important && !this.shouldPrioritizeTask(section, NEARBY_REBUILD_DISTANCE)) {
            pendingUpdate = ChunkUpdateType.REBUILD;
         } else {
            pendingUpdate = ChunkUpdateType.IMPORTANT_REBUILD;
         }

         pendingUpdate = ChunkUpdateType.getPromotionUpdateType(section.getPendingUpdate(), pendingUpdate);
         if (pendingUpdate != null) {
            section.setPendingUpdate(pendingUpdate);
            this.needsGraphUpdate = true;
         }
      }
   }

   private boolean shouldPrioritizeTask(RenderSection section, float distance) {
      return this.cameraBlockPos != null && section.getSquaredDistance(this.cameraBlockPos) < distance;
   }

   private static boolean allowImportantRebuilds() {
      return !SodiumClientMod.options().performance.alwaysDeferChunkUpdates;
   }

   private float getEffectiveRenderDistance(FogParameters fogParameters) {
      float alpha = fogParameters.alpha();
      float distance = fogParameters.renderEnd();
      float renderDistance = this.getRenderDistance();
      return !Mth.equal(alpha, 1.0F) ? renderDistance : Math.min(renderDistance, distance + 0.5F);
   }

   private float getRenderDistance() {
      return this.renderDistance * 16.0F;
   }

   private void connectNeighborNodes(RenderSection render) {
      for (int direction = 0; direction < 6; direction++) {
         RenderSection adj = this.getRenderSection(
            render.getChunkX() + GraphDirection.x(direction),
            render.getChunkY() + GraphDirection.y(direction),
            render.getChunkZ() + GraphDirection.z(direction)
         );
         if (adj != null) {
            adj.setAdjacentNode(GraphDirection.opposite(direction), render);
            render.setAdjacentNode(direction, adj);
         }
      }
   }

   private void disconnectNeighborNodes(RenderSection render) {
      for (int direction = 0; direction < 6; direction++) {
         RenderSection adj = render.getAdjacent(direction);
         if (adj != null) {
            adj.setAdjacentNode(GraphDirection.opposite(direction), null);
            render.setAdjacentNode(direction, null);
         }
      }
   }

   private RenderSection getRenderSection(int x, int y, int z) {
      return (RenderSection)this.sectionByPosition.get(SectionPos.asLong(x, y, z));
   }

   public Collection<String> getDebugStrings() {
      List<String> list = new ArrayList<>();
      int count = 0;
      long deviceUsed = 0L;
      long deviceAllocated = 0L;

      for (RenderRegion region : this.regions.getLoadedRegions()) {
         RenderRegion.DeviceResources resources = region.getResources();
         if (resources != null) {
            GlBufferArena buffer = resources.getGeometryArena();
            deviceUsed += buffer.getDeviceUsedMemory();
            deviceAllocated += buffer.getDeviceAllocatedMemory();
            count++;
         }
      }

      list.add(String.format("Geometry Pool: %d/%d MiB (%d buffers)", MathUtil.toMib(deviceUsed), MathUtil.toMib(deviceAllocated), count));
      list.add(String.format("Transfer Queue: %s", this.regions.getStagingBuffer().toString()));
      list.add(
         String.format(
            "Chunk Builder: Permits=%02d (E %03d) | Busy=%02d | Total=%02d",
            this.builder.getScheduledJobCount(),
            this.builder.getScheduledEffort(),
            this.builder.getBusyThreadCount(),
            this.builder.getTotalThreadCount()
         )
      );
      list.add(
         String.format(
            "Chunk Queues: U=%02d (P0=%03d | P1=%03d | P2=%03d)",
            this.buildResults.size(),
            this.taskLists.get(ChunkUpdateType.IMPORTANT_REBUILD).size() + this.taskLists.get(ChunkUpdateType.IMPORTANT_SORT).size(),
            this.taskLists.get(ChunkUpdateType.REBUILD).size() + this.taskLists.get(ChunkUpdateType.SORT).size(),
            this.taskLists.get(ChunkUpdateType.INITIAL_BUILD).size()
         )
      );
      this.sortTriggering.addDebugStrings(list);
      return list;
   }

   @NotNull
   public SortedRenderLists getRenderLists() {
      return this.renderLists;
   }

   public boolean isSectionBuilt(int x, int y, int z) {
      RenderSection section = this.getRenderSection(x, y, z);
      return section != null && section.isBuilt();
   }

   public void onChunkAdded(int x, int z) {
      for (int y = this.level.getMinSectionY(); y <= this.level.getMaxSectionY(); y++) {
         this.onSectionAdded(x, y, z);
      }
   }

   public void onChunkRemoved(int x, int z) {
      for (int y = this.level.getMinSectionY(); y <= this.level.getMaxSectionY(); y++) {
         this.onSectionRemoved(x, y, z);
      }
   }

   public Collection<RenderSection> getSectionsWithGlobalEntities() {
      return ReferenceSets.unmodifiable(this.sectionsWithGlobalEntities);
   }
}
