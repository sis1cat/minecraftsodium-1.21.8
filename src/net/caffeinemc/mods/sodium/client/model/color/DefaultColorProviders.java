package net.caffeinemc.mods.sodium.client.model.color;

import java.util.Arrays;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.blender.BlendedColorProvider;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos.MutableBlockPos;

public class DefaultColorProviders {
   public static ColorProvider<BlockState> adapt(BlockColor color) {
      return new DefaultColorProviders.VanillaAdapter(color);
   }

   public static class FoliageColorProvider<T> extends BlendedColorProvider<T> {
      public static final ColorProvider<BlockState> BLOCKS = new DefaultColorProviders.FoliageColorProvider<>();

      private FoliageColorProvider() {
      }

      @Override
      protected int getColor(LevelSlice slice, T state, BlockPos pos) {
         return 0xFF000000 | BiomeColors.getAverageGrassColor(slice, pos);
      }
   }

   public static class GrassColorProvider<T> extends BlendedColorProvider<T> {
      public static final ColorProvider<BlockState> BLOCKS = new DefaultColorProviders.GrassColorProvider<>();

      private GrassColorProvider() {
      }

      @Override
      protected int getColor(LevelSlice slice, T state, BlockPos pos) {
         return 0xFF000000 | BiomeColors.getAverageFoliageColor(slice, pos);
      }
   }

   private static class VanillaAdapter implements ColorProvider<BlockState> {
      private final BlockColor  color;

      private VanillaAdapter(BlockColor color) {
         this.color = color;
      }

      public void getColors(LevelSlice slice, BlockPos pos, MutableBlockPos scratchPos, BlockState state, ModelQuadView quad, int[] output) {
         Arrays.fill(output, 0xFF000000 | this.color.getColor(state, slice, pos, quad.getTintIndex()));
      }
   }
}
