package net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.bsp_tree;

import org.joml.Vector3fc;

public class LeafDoubleBSPNode extends BSPNode {
   private final int quadA;
   private final int quadB;

   LeafDoubleBSPNode(int quadA, int quadB) {
      this.quadA = quadA;
      this.quadB = quadB;
   }

   @Override
   void collectSortedQuads(BSPSortState sortState, Vector3fc cameraPos) {
      sortState.writeIndex(this.quadA);
      sortState.writeIndex(this.quadB);
   }
}
