package net.caffeinemc.mods.sodium.client.model.quad.properties;

import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.Direction;

public class ModelQuadFlags {
   public static final int IS_PARTIAL = 1;
   public static final int IS_PARALLEL = 2;
   public static final int IS_ALIGNED = 4;
   public static final int FLAG_BIT_COUNT = 3;

   public static boolean contains(int flags, int mask) {
      return (flags & mask) != 0;
   }

   public static int getQuadFlags(ModelQuadView quad, Direction face) {
      float minX = 32.0F;
      float minY = 32.0F;
      float minZ = 32.0F;
      float maxX = -32.0F;
      float maxY = -32.0F;
      float maxZ = -32.0F;

      for (int i = 0; i < 4; i++) {
         float x = quad.getX(i);
         float y = quad.getY(i);
         float z = quad.getZ(i);
         minX = Math.min(minX, x);
         minY = Math.min(minY, y);
         minZ = Math.min(minZ, z);
         maxX = Math.max(maxX, x);
         maxY = Math.max(maxY, y);
         maxZ = Math.max(maxZ, z);
      }
      boolean partial = switch (face.getAxis()) {
         case X -> minY >= 1.0E-4F || minZ >= 1.0E-4F || maxY <= 0.9999F || maxZ <= 0.9999F;
         case Y -> minX >= 1.0E-4F || minZ >= 1.0E-4F || maxX <= 0.9999F || maxZ <= 0.9999F;
         case Z -> minX >= 1.0E-4F || minY >= 1.0E-4F || maxX <= 0.9999F || maxY <= 0.9999F;
         default -> throw new MatchException(null, null);
      };

      boolean parallel;
      boolean var17;
      label85: {
         label84: {
            parallel = switch (face.getAxis()) {
               case X -> minX == maxX;
               case Y -> minY == maxY;
               case Z -> minZ == maxZ;
               default -> throw new MatchException(null, null);
            };
            if (parallel) {
               switch (face) {
                  case DOWN:
                     if (minY < 1.0E-4F) {
                        break label84;
                     }
                     break;
                  case UP:
                     if (maxY > 0.9999F) {
                        break label84;
                     }
                     break;
                  case NORTH:
                     if (minZ < 1.0E-4F) {
                        break label84;
                     }
                     break;
                  case SOUTH:
                     if (maxZ > 0.9999F) {
                        break label84;
                     }
                     break;
                  case WEST:
                     if (minX < 1.0E-4F) {
                        break label84;
                     }
                     break;
                  case EAST:
                     if (maxX > 0.9999F) {
                        break label84;
                     }
                     break;
                  default:
                     throw new MatchException(null, null);
               }
            }

            var17 = false;
            break label85;
         }

         var17 = true;
      }

      boolean aligned = var17;
      int flags = 0;
      if (partial) {
         flags |= 1;
      }

      if (parallel) {
         flags |= 2;
      }

      if (aligned) {
         flags |= 4;
      }

      return flags;
   }
}
