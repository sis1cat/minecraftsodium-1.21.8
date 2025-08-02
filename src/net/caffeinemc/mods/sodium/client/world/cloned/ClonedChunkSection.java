package net.caffeinemc.mods.sodium.client.world.cloned;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import java.util.Map.Entry;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformLevelAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformModelAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.caffeinemc.mods.sodium.client.services.SodiumModelDataContainer;
import net.caffeinemc.mods.sodium.client.util.iterator.WrappedIterator;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.PalettedContainerROExtension;
import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.PalettedContainer.Strategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClonedChunkSection {
   private static final DataLayer DEFAULT_SKY_LIGHT_ARRAY = new DataLayer(15);
   private static final DataLayer DEFAULT_BLOCK_LIGHT_ARRAY = new DataLayer(0);
   private static final PalettedContainer<BlockState> DEFAULT_STATE_CONTAINER = new PalettedContainer<>(
      Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), Strategy.SECTION_STATES
   );
   private final SectionPos pos;
   @Nullable
   private final Int2ReferenceMap<BlockEntity> blockEntityMap;
   @Nullable
   private final Int2ReferenceMap<Object> blockEntityRenderDataMap;
   @Nullable
   private final DataLayer[] lightDataArrays;
   @Nullable
   private final SodiumAuxiliaryLightManager auxLightManager;
   @Nullable
   private final PalettedContainerRO<BlockState> blockData;
   @Nullable
   private final PalettedContainerRO<Holder<Biome>> biomeData;
   private final SodiumModelDataContainer modelMap;
   private long lastUsedTimestamp = Long.MAX_VALUE;

   public ClonedChunkSection(Level level, LevelChunk chunk, @Nullable LevelChunkSection section, SectionPos pos) {
      this.pos = pos;
      PalettedContainerRO<BlockState> blockData = null;
      PalettedContainerRO<Holder<Biome>> biomeData = null;
      Int2ReferenceMap<BlockEntity> blockEntityMap = null;
      Int2ReferenceMap<Object> blockEntityRenderDataMap = null;
      SodiumModelDataContainer modelMap = PlatformModelAccess.getInstance().getModelDataContainer(level, pos);
      this.auxLightManager = PlatformLevelAccess.INSTANCE.getLightManager(chunk, pos);
      if (section != null) {
         if (!section.hasOnlyAir()) {
            if (!level.isDebug()) {
               blockData = PalettedContainerROExtension.clone(section.getStates());
            } else {
               blockData = constructDebugWorldContainer(pos);
            }

            blockEntityMap = tryCopyBlockEntities(chunk, pos);
            if (blockEntityMap != null && PlatformBlockAccess.getInstance().platformHasBlockData()) {
               blockEntityRenderDataMap = copyBlockEntityRenderData(level, blockEntityMap);
            }
         }

         biomeData = PalettedContainerROExtension.clone(section.getBiomes());
      }

      this.blockData = blockData;
      this.biomeData = biomeData;
      this.modelMap = modelMap;
      this.blockEntityMap = blockEntityMap;
      this.blockEntityRenderDataMap = blockEntityRenderDataMap;
      this.lightDataArrays = copyLightData(level, pos);
   }

   @NotNull
   private static PalettedContainer<BlockState> constructDebugWorldContainer(SectionPos pos) {
      if (pos.getY() != 3 && pos.getY() != 4) {
         return DEFAULT_STATE_CONTAINER;
      } else {
         PalettedContainer<BlockState> container = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), Strategy.SECTION_STATES);
         if (pos.getY() == 3) {
            BlockState barrier = Blocks.BARRIER.defaultBlockState();

            for (int z = 0; z < 16; z++) {
               for (int x = 0; x < 16; x++) {
                  container.getAndSetUnchecked(x, 12, z, barrier);
               }
            }
         } else if (pos.getY() == 4) {
            for (int z = 0; z < 16; z++) {
               for (int x = 0; x < 16; x++) {
                  container.getAndSetUnchecked(
                     x, 6, z, DebugLevelSource.getBlockStateFor(SectionPos.sectionToBlockCoord(pos.getX(), x), SectionPos.sectionToBlockCoord(pos.getZ(), z))
                  );
               }
            }
         }

         return container;
      }
   }

   @NotNull
   private static DataLayer[] copyLightData(Level level, SectionPos pos) {
      DataLayer[] arrays = new DataLayer[2];
      arrays[LightLayer.BLOCK.ordinal()] = copyLightArray(level, LightLayer.BLOCK, pos);
      if (level.dimensionType().hasSkyLight()) {
         arrays[LightLayer.SKY.ordinal()] = copyLightArray(level, LightLayer.SKY, pos);
      }

      return arrays;
   }

   @NotNull
   private static DataLayer copyLightArray(Level level, LightLayer type, SectionPos pos) {
      DataLayer array = level.getLightEngine().getLayerListener(type).getDataLayerData(pos);
      if (array == null) {
         array = switch (type) {
            case SKY -> DEFAULT_SKY_LIGHT_ARRAY;
            case BLOCK -> DEFAULT_BLOCK_LIGHT_ARRAY;
         };
      }

      return array;
   }

   @Nullable
   private static Int2ReferenceMap<BlockEntity> tryCopyBlockEntities(LevelChunk chunk, SectionPos chunkCoord) {
      try {
         return copyBlockEntities(chunk, chunkCoord);
      } catch (WrappedIterator.Exception var3) {
         if (PlatformRuntimeInformation.getInstance().isModInLoadingList("entityculling")) {
            throw new RuntimeException(
               "Failed to iterate block entities! This is *very likely* the fault of the Entity Culling mod, and cannot be fixed by Sodium. See here for more details: https://link.caffeinemc.net/help/sodium/mod-issue/entity-culling/gh-2985",
               var3
            );
         } else {
            throw new RuntimeException(
               "Failed to iterate block entities! This is *very likely* the fault of another misbehaving mod, not Sodium. Please check your mods list.", var3
            );
         }
      }
   }

   @Nullable
   private static Int2ReferenceMap<BlockEntity> copyBlockEntities(LevelChunk chunk, SectionPos chunkCoord) {
      BoundingBox box = new BoundingBox(
         chunkCoord.minBlockX(),
         chunkCoord.minBlockY(),
         chunkCoord.minBlockZ(),
         chunkCoord.maxBlockX(),
         chunkCoord.maxBlockY(),
         chunkCoord.maxBlockZ()
      );
      Int2ReferenceOpenHashMap<BlockEntity> blockEntities = null;
      WrappedIterator<Entry<BlockPos, BlockEntity>> it = WrappedIterator.create(chunk.getBlockEntities().entrySet());

      while (it.hasNext()) {
         Entry<BlockPos, BlockEntity> entry = it.next();
         BlockPos pos = entry.getKey();
         BlockEntity entity = entry.getValue();
         if (box.isInside(pos)) {
            if (blockEntities == null) {
               blockEntities = new Int2ReferenceOpenHashMap<>();
            }

            blockEntities.put(LevelSlice.getLocalBlockIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), entity);
         }
      }

      if (blockEntities != null) {
         blockEntities.trim();
      }

      return blockEntities;
   }

   @Nullable
   private static Int2ReferenceMap<Object> copyBlockEntityRenderData(Level level, Int2ReferenceMap<BlockEntity> blockEntities) {
      Int2ReferenceOpenHashMap<Object> blockEntityRenderDataMap = null;

       for (Int2ReferenceMap.Entry<BlockEntity> entry : Int2ReferenceMaps.fastIterable(blockEntities)) {
           Object data = PlatformLevelAccess.getInstance().getBlockEntityData(entry.getValue());
           if (data != null) {
               if (blockEntityRenderDataMap == null) {
                   blockEntityRenderDataMap = new Int2ReferenceOpenHashMap<>();
               }

               blockEntityRenderDataMap.put(entry.getIntKey(), data);
           }
       }

      if (blockEntityRenderDataMap != null) {
         blockEntityRenderDataMap.trim();
      }

      return blockEntityRenderDataMap;
   }

   public SectionPos getPosition() {
      return this.pos;
   }

   @Nullable
   public PalettedContainerRO<BlockState> getBlockData() {
      return this.blockData;
   }

   @Nullable
   public PalettedContainerRO<Holder<Biome>> getBiomeData() {
      return this.biomeData;
   }

   @Nullable
   public Int2ReferenceMap<BlockEntity> getBlockEntityMap() {
      return this.blockEntityMap;
   }

   @Nullable
   public Int2ReferenceMap<Object> getBlockEntityRenderDataMap() {
      return this.blockEntityRenderDataMap;
   }

   public SodiumModelDataContainer getModelMap() {
      return this.modelMap;
   }

   @Nullable
   public DataLayer getLightArray(LightLayer lightType) {
      return this.lightDataArrays[lightType.ordinal()];
   }

   public long getLastUsedTimestamp() {
      return this.lastUsedTimestamp;
   }

   public void setLastUsedTimestamp(long timestamp) {
      this.lastUsedTimestamp = timestamp;
   }

   public @Nullable SodiumAuxiliaryLightManager getAuxLightManager() {
      return this.auxLightManager;
   }

}
