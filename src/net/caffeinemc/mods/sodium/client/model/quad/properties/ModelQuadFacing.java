package net.caffeinemc.mods.sodium.client.model.quad.properties;

import java.util.Arrays;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public enum ModelQuadFacing {
   POS_X,
   POS_Y,
   POS_Z,
   NEG_X,
   NEG_Y,
   NEG_Z,
   UNASSIGNED;

   public static final ModelQuadFacing[] VALUES = values();
   public static final int COUNT = VALUES.length;
   public static final int DIRECTIONS = VALUES.length - 1;
   public static final int NONE = 0;
   public static final int ALL = (1 << COUNT) - 1;
   public static final Vector3fc[] ALIGNED_NORMALS = new Vector3fc[]{
      new Vector3f(1.0F, 0.0F, 0.0F),
      new Vector3f(0.0F, 1.0F, 0.0F),
      new Vector3f(0.0F, 0.0F, 1.0F),
      new Vector3f(-1.0F, 0.0F, 0.0F),
      new Vector3f(0.0F, -1.0F, 0.0F),
      new Vector3f(0.0F, 0.0F, -1.0F)
   };
   public static final int[] PACKED_ALIGNED_NORMALS = Arrays.stream(ALIGNED_NORMALS).mapToInt(NormI8::pack).toArray();
   public static final int OPPOSING_X = 1 << POS_X.ordinal() | 1 << NEG_X.ordinal();
   public static final int OPPOSING_Y = 1 << POS_Y.ordinal() | 1 << NEG_Y.ordinal();
   public static final int OPPOSING_Z = 1 << POS_Z.ordinal() | 1 << NEG_Z.ordinal();
   public static final int UNASSIGNED_MASK = 1 << UNASSIGNED.ordinal();

   public static ModelQuadFacing fromDirection(Direction dir) {
      return switch (dir) {
         case DOWN -> NEG_Y;
         case UP -> POS_Y;
         case NORTH -> NEG_Z;
         case SOUTH -> POS_Z;
         case WEST -> NEG_X;
         case EAST -> POS_X;
         default -> throw new MatchException(null, null);
      };
   }

   public ModelQuadFacing getOpposite() {
      return switch (this) {
         case POS_X -> NEG_X;
         case POS_Y -> NEG_Y;
         case POS_Z -> NEG_Z;
         case NEG_X -> POS_X;
         case NEG_Y -> POS_Y;
         case NEG_Z -> POS_Z;
         default -> UNASSIGNED;
      };
   }

   public int getSign() {
      return switch (this) {
         case POS_X, POS_Y, POS_Z -> 1;
         case NEG_X, NEG_Y, NEG_Z -> -1;
         default -> 0;
      };
   }

   public int getAxis() {
      return switch (this) {
         case POS_X, NEG_X -> 0;
         case POS_Y, NEG_Y -> 1;
         case POS_Z, NEG_Z -> 2;
         default -> -1;
      };
   }

   public boolean isAligned() {
      return this != UNASSIGNED;
   }

   public Vector3fc getAlignedNormal() {
      if (!this.isAligned()) {
         throw new IllegalStateException("Cannot get aligned normal for unassigned facing");
      } else {
         return ALIGNED_NORMALS[this.ordinal()];
      }
   }

   public int getPackedAlignedNormal() {
      if (!this.isAligned()) {
         throw new IllegalStateException("Cannot get packed aligned normal for unassigned facing");
      } else {
         return PACKED_ALIGNED_NORMALS[this.ordinal()];
      }
   }

   public static ModelQuadFacing fromNormal(float x, float y, float z) {
      if (Math.isFinite(x) && Math.isFinite(y) && Math.isFinite(z)) {
         for (Direction face : DirectionUtil.ALL_DIRECTIONS) {
            Vector3f step = face.step();
            if (Mth.equal(Math.fma(x, step.x(), Math.fma(y, step.y(), z * step.z())), 1.0F)) {
               return fromDirection(face);
            }
         }

         return UNASSIGNED;
      } else {
         return UNASSIGNED;
      }
   }

   public static ModelQuadFacing fromPackedNormal(int normal) {
      return fromNormal(NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal));
   }

   public static boolean bitmapIsOpposingAligned(int bitmap) {
      return bitmap == OPPOSING_X || bitmap == OPPOSING_Y || bitmap == OPPOSING_Z;
   }

   public static boolean bitmapHasUnassigned(int bitmap) {
      return (bitmap & UNASSIGNED_MASK) != 0;
   }
}
