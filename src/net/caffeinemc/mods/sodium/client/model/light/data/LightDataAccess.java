package net.caffeinemc.mods.sodium.client.model.light.data;

import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.FilterMask.Type;
import net.minecraft.core.BlockPos.MutableBlockPos;

public abstract class LightDataAccess {
   private final MutableBlockPos pos = new MutableBlockPos();
   protected BlockAndTintGetter level;

   public int get(int x, int y, int z, Direction d1, Direction d2) {
      return this.get(x + d1.getStepX() + d2.getStepX(), y + d1.getStepY() + d2.getStepY(), z + d1.getStepZ() + d2.getStepZ());
   }

   public int get(int x, int y, int z, Direction dir) {
      return this.get(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ());
   }

   public int get(BlockPos pos, Direction dir) {
      return this.get(pos.getX(), pos.getY(), pos.getZ(), dir);
   }

   public int get(BlockPos pos) {
      return this.get(pos.getX(), pos.getY(), pos.getZ());
   }

   public abstract int get(int var1, int var2, int var3);

   protected int compute(int x, int y, int z) {
      BlockPos pos = this.pos.set(x, y, z);
      BlockAndTintGetter level = this.level;
      BlockState state = level.getBlockState(pos);
      boolean em = state.emissiveRendering(level, pos);
      boolean op = state.isViewBlocking(level, pos) && state.getLightBlock() != 0;
      boolean fo = state.isSolidRender();
      boolean fc = state.isCollisionShapeFullBlock(level, pos);
      int lu = PlatformBlockAccess.getInstance().getLightEmission(state, level, pos);
      int bl;
      int sl;
      if (fo && lu == 0) {
         bl = 0;
         sl = 0;
      } else if (em) {
         bl = level.getBrightness(LightLayer.BLOCK, pos);
         sl = level.getBrightness(LightLayer.SKY, pos);
      } else {
         int light = LevelRenderer.getLightColor(LevelRenderer.BrightnessGetter.DEFAULT, level, state, pos);
         bl = LightTexture.block(light);
         sl = LightTexture.sky(light);
      }

      float ao;
      if (lu == 0) {
         ao = state.getShadeBrightness(level, pos);
      } else {
         ao = 1.0F;
      }

      return packFC(fc) | packFO(fo) | packOP(op) | packEM(em) | packAO(ao) | packLU(lu) | packSL(sl) | packBL(bl);
   }

   public static int packBL(int blockLight) {
      return blockLight & 15;
   }

   public static int unpackBL(int word) {
      return word & 15;
   }

   public static int packSL(int skyLight) {
      return (skyLight & 15) << 4;
   }

   public static int unpackSL(int word) {
      return word >>> 4 & 15;
   }

   public static int packLU(int luminance) {
      return (luminance & 15) << 8;
   }

   public static int unpackLU(int word) {
      return word >>> 8 & 15;
   }

   public static int packAO(float ao) {
      int aoi = (int)(ao * 4096.0F);
      return (aoi & 65535) << 12;
   }

   public static float unpackAO(int word) {
      int aoi = word >>> 12 & 65535;
      return aoi * 2.4414062E-4F;
   }

   public static int packEM(boolean emissive) {
      return (emissive ? 1 : 0) << 28;
   }

   public static boolean unpackEM(int word) {
      return (word >>> 28 & 1) != 0;
   }

   public static int packOP(boolean opaque) {
      return (opaque ? 1 : 0) << 29;
   }

   public static boolean unpackOP(int word) {
      return (word >>> 29 & 1) != 0;
   }

   public static int packFO(boolean opaque) {
      return (opaque ? 1 : 0) << 30;
   }

   public static boolean unpackFO(int word) {
      return (word >>> 30 & 1) != 0;
   }

   public static int packFC(boolean fullCube) {
      return (fullCube ? 1 : 0) << 31;
   }

   public static boolean unpackFC(int word) {
      return (word >>> 31 & 1) != 0;
   }

   public static int getLightmap(int word) {
      return LightTexture.pack(Math.max(unpackBL(word), unpackLU(word)), unpackSL(word));
   }

   public static int getEmissiveLightmap(int word) {
      return unpackEM(word) ? 15728880 : getLightmap(word);
   }

   public BlockAndTintGetter getLevel() {
      return this.level;
   }
}
