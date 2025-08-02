package net.caffeinemc.mods.sodium.fabric.level;

import net.caffeinemc.mods.sodium.client.services.PlatformLevelAccess;
import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.Nullable;

public class FabricLevelAccess implements PlatformLevelAccess {
   @Nullable
   @Override
   public Object getBlockEntityData(BlockEntity blockEntity) {
      return Integer.MAX_VALUE;
   }

   @Nullable
   @Override
   public SodiumAuxiliaryLightManager getLightManager(LevelChunk chunk, SectionPos pos) {
      return null;
   }
}
