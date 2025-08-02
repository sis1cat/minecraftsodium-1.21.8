package net.caffeinemc.mods.sodium.client.world.cloned;

import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClonedChunkSectionCache {
   private static final int MAX_CACHE_SIZE = 512;
   private static final long MAX_CACHE_DURATION = TimeUnit.SECONDS.toNanos(5L);
   private final Level level;
   private final Long2ReferenceLinkedOpenHashMap<ClonedChunkSection> positionToEntry = new Long2ReferenceLinkedOpenHashMap();
   private long time;

   public ClonedChunkSectionCache(Level level) {
      this.level = level;
      this.time = getMonotonicTimeSource();
   }

   public void cleanup() {
      this.time = getMonotonicTimeSource();
      this.positionToEntry.values().removeIf(entry -> this.time > entry.getLastUsedTimestamp() + MAX_CACHE_DURATION);
   }

   @Nullable
   public ClonedChunkSection acquire(int x, int y, int z) {
      long pos = SectionPos.asLong(x, y, z);
      ClonedChunkSection section = (ClonedChunkSection)this.positionToEntry.getAndMoveToLast(pos);
      if (section == null) {
         section = this.clone(x, y, z);

         while (this.positionToEntry.size() >= 512) {
            this.positionToEntry.removeFirst();
         }

         this.positionToEntry.putAndMoveToLast(pos, section);
      }

      section.setLastUsedTimestamp(this.time);
      return section;
   }

   @NotNull
   private ClonedChunkSection clone(int x, int y, int z) {
      LevelChunk chunk = this.level.getChunk(x, z);
      if (chunk == null) {
         throw new RuntimeException("Chunk is not loaded at: " + SectionPos.asLong(x, y, z));
      } else {
         LevelChunkSection section = null;
         if (!this.level.isOutsideBuildHeight(SectionPos.sectionToBlockCoord(y))) {
            section = chunk.getSections()[this.level.getSectionIndexFromSectionY(y)];
         }

         return new ClonedChunkSection(this.level, chunk, section, SectionPos.of(x, y, z));
      }
   }

   public void invalidate(int x, int y, int z) {
      this.positionToEntry.remove(SectionPos.asLong(x, y, z));
   }

   private static long getMonotonicTimeSource() {
      return System.nanoTime();
   }
}
