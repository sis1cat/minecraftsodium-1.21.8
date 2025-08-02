package net.caffeinemc.mods.sodium.client.services;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;

public class SodiumModelDataContainer {
   private final Long2ObjectMap<SodiumModelData> modelDataMap;
   private final boolean isEmpty;

   public SodiumModelDataContainer(Long2ObjectMap<SodiumModelData> modelDataMap) {
      this.modelDataMap = modelDataMap;
      this.isEmpty = modelDataMap.isEmpty();
   }

   public SodiumModelData getModelData(BlockPos pos) {
      return (SodiumModelData)this.modelDataMap.getOrDefault(pos.asLong(), SodiumModelData.EMPTY);
   }

   public boolean isEmpty() {
      return this.isEmpty;
   }
}
