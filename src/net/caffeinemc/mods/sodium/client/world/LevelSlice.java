package net.caffeinemc.mods.sodium.client.world;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.caffeinemc.mods.sodium.client.services.PlatformLevelRenderHooks;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.caffeinemc.mods.sodium.client.services.SodiumModelDataContainer;
import net.caffeinemc.mods.sodium.client.world.biome.LevelBiomeSlice;
import net.caffeinemc.mods.sodium.client.world.biome.LevelColorCache;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.util.Mth;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.SectionPos;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.world.level.ColorResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LevelSlice implements BlockAndTintGetter, FabricBlockView {
   private static final LightLayer[] LIGHT_TYPES = LightLayer.values();
   private static final int SECTION_BLOCK_COUNT = 4096;
   private static final int NEIGHBOR_BLOCK_RADIUS = 2;
   private static final int NEIGHBOR_CHUNK_RADIUS = Mth.roundToward(2, 16) >> 4;
   private static final int SECTION_ARRAY_LENGTH = 1 + NEIGHBOR_CHUNK_RADIUS * 2;
   private static final int SECTION_ARRAY_SIZE = SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH;
   private static final int LOCAL_XYZ_BITS = 4;
   private static final BlockState EMPTY_BLOCK_STATE = Blocks.AIR.defaultBlockState();
   private final Level level;
   private final LevelBiomeSlice biomeSlice;
   private final LevelColorCache biomeColors;
   private final BlockState[][] blockArrays;
   private final SodiumAuxiliaryLightManager[] auxLightManager;
   @Nullable
   private final DataLayer[][] lightArrays;
   @Nullable
   private final Int2ReferenceMap<BlockEntity>[] blockEntityArrays;
   @Nullable
   private final Int2ReferenceMap<Object>[] blockEntityRenderDataArrays;
   private final SodiumModelDataContainer[] modelMapArrays;
   private int originBlockX;
   private int originBlockY;
   private int originBlockZ;
   private BoundingBox volume;

   public static ChunkRenderContext prepare(Level level, SectionPos pos, ClonedChunkSectionCache cache) {
      LevelChunk chunk = level.getChunk(pos.getX(), pos.getZ());
      LevelChunkSection section = chunk.getSections()[level.getSectionIndexFromSectionY(pos.getY())];
      if (section != null && !section.hasOnlyAir()) {
         BoundingBox box = new BoundingBox(
            pos.minBlockX() - 2, pos.minBlockY() - 2, pos.minBlockZ() - 2, pos.maxBlockX() + 2, pos.maxBlockY() + 2, pos.maxBlockZ() + 2
         );
         int minChunkX = pos.getX() - NEIGHBOR_CHUNK_RADIUS;
         int minChunkY = pos.getY() - NEIGHBOR_CHUNK_RADIUS;
         int minChunkZ = pos.getZ() - NEIGHBOR_CHUNK_RADIUS;
         int maxChunkX = pos.getX() + NEIGHBOR_CHUNK_RADIUS;
         int maxChunkY = pos.getY() + NEIGHBOR_CHUNK_RADIUS;
         int maxChunkZ = pos.getZ() + NEIGHBOR_CHUNK_RADIUS;
         ClonedChunkSection[] sections = new ClonedChunkSection[SECTION_ARRAY_SIZE];

         for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
               for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
                  sections[getLocalSectionIndex(chunkX - minChunkX, chunkY - minChunkY, chunkZ - minChunkZ)] = cache.acquire(chunkX, chunkY, chunkZ);
               }
            }
         }

         List<?> renderers = PlatformLevelRenderHooks.getInstance().retrieveChunkMeshAppenders(level, pos.origin());
         return new ChunkRenderContext(pos, sections, box, renderers);
      } else {
         return null;
      }
   }

   public LevelSlice(Level level) {
      this.level = level;
      this.blockArrays = new BlockState[SECTION_ARRAY_SIZE][4096];
      this.lightArrays = new DataLayer[SECTION_ARRAY_SIZE][LIGHT_TYPES.length];
      this.blockEntityArrays = new Int2ReferenceMap[SECTION_ARRAY_SIZE];
      this.blockEntityRenderDataArrays = new Int2ReferenceMap[SECTION_ARRAY_SIZE];
      this.auxLightManager = new SodiumAuxiliaryLightManager[SECTION_ARRAY_SIZE];
      this.modelMapArrays = new SodiumModelDataContainer[SECTION_ARRAY_SIZE];
      this.biomeSlice = new LevelBiomeSlice();
      this.biomeColors = new LevelColorCache(this.biomeSlice, (Integer) Minecraft.getInstance().options.biomeBlendRadius().get());

      for (BlockState[] blockArray : this.blockArrays) {
         Arrays.fill(blockArray, EMPTY_BLOCK_STATE);
      }
   }

   public void copyData(ChunkRenderContext context) {
      this.originBlockX = SectionPos.sectionToBlockCoord(context.getOrigin().getX() - NEIGHBOR_CHUNK_RADIUS);
      this.originBlockY = SectionPos.sectionToBlockCoord(context.getOrigin().getY() - NEIGHBOR_CHUNK_RADIUS);
      this.originBlockZ = SectionPos.sectionToBlockCoord(context.getOrigin().getZ() - NEIGHBOR_CHUNK_RADIUS);
      this.volume = context.getVolume();

      for (int x = 0; x < SECTION_ARRAY_LENGTH; x++) {
         for (int y = 0; y < SECTION_ARRAY_LENGTH; y++) {
            for (int z = 0; z < SECTION_ARRAY_LENGTH; z++) {
               this.copySectionData(context, getLocalSectionIndex(x, y, z));
            }
         }
      }

      this.biomeSlice.update(this.level, context);
      this.biomeColors.update(context);
   }

   private void copySectionData(ChunkRenderContext context, int sectionIndex) {
      ClonedChunkSection section = context.getSections()[sectionIndex];
      Objects.requireNonNull(section, "Chunk section must be non-null");
      this.unpackBlockData(this.blockArrays[sectionIndex], context, section);
      this.lightArrays[sectionIndex][LightLayer.BLOCK.ordinal()] = section.getLightArray(LightLayer.BLOCK);
      this.lightArrays[sectionIndex][LightLayer.SKY.ordinal()] = section.getLightArray(LightLayer.SKY);
      this.blockEntityArrays[sectionIndex] = section.getBlockEntityMap();
      this.auxLightManager[sectionIndex] = section.getAuxLightManager();
      this.blockEntityRenderDataArrays[sectionIndex] = section.getBlockEntityRenderDataMap();
      this.modelMapArrays[sectionIndex] = section.getModelMap();
   }

   private void unpackBlockData(BlockState[] blockArray, ChunkRenderContext context, ClonedChunkSection section) {
      if (section.getBlockData() == null) {
         Arrays.fill(blockArray, EMPTY_BLOCK_STATE);
      } else {
         PalettedContainerROExtension<BlockState> container = PalettedContainerROExtension.of(section.getBlockData());
         SectionPos sectionPos = section.getPosition();
         if (sectionPos.equals(context.getOrigin())) {
            container.sodium$unpack(blockArray);
         } else {
            BoundingBox bounds = context.getVolume();
            int minBlockX = Math.max(bounds.minX(), sectionPos.minBlockX());
            int maxBlockX = Math.min(bounds.maxX(), sectionPos.maxBlockX());
            int minBlockY = Math.max(bounds.minY(), sectionPos.minBlockY());
            int maxBlockY = Math.min(bounds.maxY(), sectionPos.maxBlockY());
            int minBlockZ = Math.max(bounds.minZ(), sectionPos.minBlockZ());
            int maxBlockZ = Math.min(bounds.maxZ(), sectionPos.maxBlockZ());
            container.sodium$unpack(blockArray, minBlockX & 15, minBlockY & 15, minBlockZ & 15, maxBlockX & 15, maxBlockY & 15, maxBlockZ & 15);
         }
      }
   }

   public void reset() {
      for (int sectionIndex = 0; sectionIndex < SECTION_ARRAY_LENGTH; sectionIndex++) {
         Arrays.fill(this.lightArrays[sectionIndex], null);
         this.blockEntityArrays[sectionIndex] = null;
         this.auxLightManager[sectionIndex] = null;
         this.blockEntityRenderDataArrays[sectionIndex] = null;
      }
   }

   @NotNull
   public BlockState getBlockState(BlockPos pos) {
      return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
   }

   public BlockState getBlockState(int blockX, int blockY, int blockZ) {
      if (!this.volume.isInside(blockX, blockY, blockZ)) {
         return EMPTY_BLOCK_STATE;
      } else {
         int relBlockX = blockX - this.originBlockX;
         int relBlockY = blockY - this.originBlockY;
         int relBlockZ = blockZ - this.originBlockZ;
         return this.blockArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)][getLocalBlockIndex(
            relBlockX & 15, relBlockY & 15, relBlockZ & 15
         )];
      }
   }

   @NotNull
   public FluidState getFluidState(BlockPos pos) {
      return this.getBlockState(pos).getFluidState();
   }

   public float getShade(Direction direction, boolean shaded) {
      return this.level.getShade(direction, shaded);
   }

   @NotNull
   public LevelLightEngine getLightEngine() {
      throw new UnsupportedOperationException();
   }

   public int getBrightness(LightLayer type, BlockPos pos) {
      if (!this.volume.isInside(pos.getX(), pos.getY(), pos.getZ())) {
         return 0;
      } else {
         int relBlockX = pos.getX() - this.originBlockX;
         int relBlockY = pos.getY() - this.originBlockY;
         int relBlockZ = pos.getZ() - this.originBlockZ;
         DataLayer lightArray = this.lightArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)][type.ordinal()];
         return lightArray == null ? 0 : lightArray.get(relBlockX & 15, relBlockY & 15, relBlockZ & 15);
      }
   }

   public int getRawBrightness(BlockPos pos, int ambientDarkness) {
      if (!this.volume.isInside(pos.getX(), pos.getY(), pos.getZ())) {
         return 0;
      } else {
         int relBlockX = pos.getX() - this.originBlockX;
         int relBlockY = pos.getY() - this.originBlockY;
         int relBlockZ = pos.getZ() - this.originBlockZ;
         DataLayer[] lightArrays = this.lightArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];
         DataLayer skyLightArray = lightArrays[LightLayer.SKY.ordinal()];
         DataLayer blockLightArray = lightArrays[LightLayer.BLOCK.ordinal()];
         int localBlockX = relBlockX & 15;
         int localBlockY = relBlockY & 15;
         int localBlockZ = relBlockZ & 15;
         int skyLight = skyLightArray == null ? 0 : skyLightArray.get(localBlockX, localBlockY, localBlockZ) - ambientDarkness;
         int blockLight = blockLightArray == null ? 0 : blockLightArray.get(localBlockX, localBlockY, localBlockZ);
         return Math.max(blockLight, skyLight);
      }
   }

   public BlockEntity getBlockEntity(BlockPos pos) {
      return this.getBlockEntity(pos.getX(), pos.getY(), pos.getZ());
   }

   public BlockEntity getBlockEntity(int blockX, int blockY, int blockZ) {
      if (!this.volume.isInside(blockX, blockY, blockZ)) {
         return null;
      } else {
         int relBlockX = blockX - this.originBlockX;
         int relBlockY = blockY - this.originBlockY;
         int relBlockZ = blockZ - this.originBlockZ;
         Int2ReferenceMap<BlockEntity> blockEntities = this.blockEntityArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];
         return blockEntities == null ? null : (BlockEntity)blockEntities.get(getLocalBlockIndex(relBlockX & 15, relBlockY & 15, relBlockZ & 15));
      }
   }

   public int getBlockTint(BlockPos pos, ColorResolver resolver) {
      return this.biomeColors.getColor(resolver, pos.getX(), pos.getY(), pos.getZ());
   }

   public int getHeight() {
      return this.level.getHeight();
   }

   public int getMinY() {
      return this.level.getMinY();
   }

   public SodiumModelData getPlatformModelData(BlockPos pos) {
      if (!this.volume.isInside(pos.getX(), pos.getY(), pos.getZ())) {
         return SodiumModelData.EMPTY;
      } else {
         int relBlockX = pos.getX() - this.originBlockX;
         int relBlockY = pos.getY() - this.originBlockY;
         int relBlockZ = pos.getZ() - this.originBlockZ;
         SodiumModelDataContainer modelMap = this.modelMapArrays[getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];
         return modelMap.isEmpty() ? SodiumModelData.EMPTY : modelMap.getModelData(pos);
      }
   }

   public static int getLocalBlockIndex(int blockX, int blockY, int blockZ) {
      return blockY << 4 << 4 | blockZ << 4 | blockX;
   }

   public static int getLocalSectionIndex(int sectionX, int sectionY, int sectionZ) {
      return sectionY * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH + sectionZ * SECTION_ARRAY_LENGTH + sectionX;
   }

   @Nullable
   public Object getBlockEntityRenderData(BlockPos pos) {
      if (!this.volume.isInside(pos.getX(), pos.getY(), pos.getZ())) {
         return null;
      } else {
         int relBlockX = pos.getX() - this.originBlockX;
         int relBlockY = pos.getY() - this.originBlockY;
         int relBlockZ = pos.getZ() - this.originBlockZ;
         Int2ReferenceMap<Object> blockEntityRenderDataMap = this.blockEntityRenderDataArrays[getLocalSectionIndex(
                 relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4
         )];
         return blockEntityRenderDataMap == null ? null : blockEntityRenderDataMap.get(getLocalBlockIndex(relBlockX & 15, relBlockY & 15, relBlockZ & 15));
      }
   }

   public boolean hasBiomes() {
      return true;
   }

   public Holder<Biome> getBiomeFabric(BlockPos pos) {
      return this.biomeSlice.getBiome(pos.getX(), pos.getY(), pos.getZ());
   }

}
