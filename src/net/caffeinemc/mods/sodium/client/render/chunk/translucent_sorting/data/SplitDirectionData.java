package net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data;

import net.minecraft.core.SectionPos;

public abstract class SplitDirectionData extends PresentTranslucentData {
   private final int[] vertexCounts;

   public SplitDirectionData(SectionPos sectionPos, int[] vertexCounts, int quadCount) {
      super(sectionPos, quadCount);
      this.vertexCounts = vertexCounts;
   }

   @Override
   public int[] getVertexCounts() {
      return this.vertexCounts;
   }
}
