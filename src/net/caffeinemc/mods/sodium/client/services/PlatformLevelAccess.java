package net.caffeinemc.mods.sodium.client.services;

import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import net.caffeinemc.mods.sodium.fabric.level.FabricLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.Nullable;

public interface PlatformLevelAccess {
   PlatformLevelAccess INSTANCE = new FabricLevelAccess();

   static PlatformLevelAccess getInstance() {
      return INSTANCE;
   }

   @Nullable
   Object getBlockEntityData(BlockEntity var1);

   @Nullable
   SodiumAuxiliaryLightManager getLightManager(LevelChunk var1, SectionPos var2);
}
