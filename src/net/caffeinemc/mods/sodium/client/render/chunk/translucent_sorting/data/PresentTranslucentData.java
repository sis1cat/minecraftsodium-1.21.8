package net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data;


import net.minecraft.core.SectionPos;

public abstract class PresentTranslucentData extends TranslucentData {
   protected final int quadCount;
   private int quadHash;

   PresentTranslucentData(SectionPos sectionPos, int quadCount) {
      super(sectionPos);
      this.quadCount = quadCount;
   }

   public abstract int[] getVertexCounts();

   public abstract Sorter getSorter();

   public void setQuadHash(int hash) {
      this.quadHash = hash;
   }

   public int getQuadHash() {
      return this.quadHash;
   }

   public int getQuadCount() {
      return this.quadCount;
   }
}
