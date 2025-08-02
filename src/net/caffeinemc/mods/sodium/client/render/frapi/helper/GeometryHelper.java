package net.caffeinemc.mods.sodium.client.render.frapi.helper;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.core.Direction.Axis;
import org.joml.Vector3fc;

public abstract class GeometryHelper {
   public static final int FLAG_BIT_COUNT = 3;

   private GeometryHelper() {
   }

   public static boolean isQuadParallelToFace(Direction face, QuadView quad) {
      int i = face.getAxis().ordinal();
      float val = quad.posByIndex(0, i);
      return Mth.equal(val, quad.posByIndex(1, i))
         && Mth.equal(val, quad.posByIndex(2, i))
         && Mth.equal(val, quad.posByIndex(3, i));
   }

   public static Direction lightFace(QuadView quad) {
      Vector3fc normal = quad.faceNormal();

      return switch (longestAxis(normal)) {
         case X -> normal.x() > 0.0F ? Direction.EAST : Direction.WEST;
         case Y -> normal.y() > 0.0F ? Direction.UP : Direction.DOWN;
         case Z -> normal.z() > 0.0F ? Direction.SOUTH : Direction.NORTH;
         default -> Direction.UP;
      };
   }

   public static Axis longestAxis(Vector3fc vec) {
      return longestAxis(vec.x(), vec.y(), vec.z());
   }

   public static Axis longestAxis(float normalX, float normalY, float normalZ) {
      Axis result = Axis.Y;
      float longest = Math.abs(normalY);
      float a = Math.abs(normalX);
      if (a > longest) {
         result = Axis.X;
         longest = a;
      }

      return Math.abs(normalZ) > longest ? Axis.Z : result;
   }
}
