package net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data;

import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortType;
import net.minecraft.core.SectionPos;

public class NoData extends TranslucentData {
   private final SortType reason;

   private NoData(SectionPos sectionPos, SortType reason) {
      super(sectionPos);
      this.reason = reason;
   }

   @Override
   public SortType getSortType() {
      return this.reason;
   }

   public static NoData forEmptySection(SectionPos sectionPos) {
      return new NoData(sectionPos, SortType.EMPTY_SECTION);
   }

   public static NoData forNoTranslucent(SectionPos sectionPos) {
      return new NoData(sectionPos, SortType.NO_TRANSLUCENT);
   }
}
