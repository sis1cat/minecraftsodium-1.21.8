package net.caffeinemc.mods.sodium.client.model.quad.properties;

public enum ModelQuadWinding {
   CLOCKWISE(new int[]{0, 1, 2, 2, 3, 0}),
   COUNTERCLOCKWISE(new int[]{0, 3, 2, 1, 0, 2});

   private final int[] indices;

   private ModelQuadWinding(int[] indices) {
      this.indices = indices;
   }

   public int[] getIndices() {
      return this.indices;
   }
}
