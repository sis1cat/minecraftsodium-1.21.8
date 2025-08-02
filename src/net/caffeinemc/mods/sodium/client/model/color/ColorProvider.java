package net.caffeinemc.mods.sodium.client.model.color;

import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;

public interface ColorProvider<T> {
   void getColors(LevelSlice var1, BlockPos var2, MutableBlockPos var3, T var4, ModelQuadView var5, int[] var6);
}
