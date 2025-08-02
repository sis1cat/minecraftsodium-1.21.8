package net.caffeinemc.mods.sodium.client.world.biome;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.util.color.BoxBlur;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ColorResolver;

public class LevelColorCache {
   private static final int NEIGHBOR_BLOCK_RADIUS = 2;
   private final LevelBiomeSlice biomeData;
   private final Reference2ReferenceOpenHashMap<ColorResolver, LevelColorCache.Slice[]> slices;
   private long populateStamp;
   private final int blendRadius;
   private final BoxBlur.ColorBuffer tempColorBuffer;
   private int minBlockX;
   private int minBlockY;
   private int minBlockZ;
   private int maxBlockX;
   private int maxBlockY;
   private int maxBlockZ;
   private final int sizeXZ;
   private final int sizeY;

   public LevelColorCache(LevelBiomeSlice biomeData, int blendRadius) {
      this.biomeData = biomeData;
      this.blendRadius = blendRadius;
      this.sizeXZ = 16 + (2 + this.blendRadius) * 2;
      this.sizeY = 20;
      this.slices = new Reference2ReferenceOpenHashMap();
      this.populateStamp = 1L;
      this.tempColorBuffer = new BoxBlur.ColorBuffer(this.sizeXZ, this.sizeXZ);
   }

   public void update(ChunkRenderContext context) {
      this.minBlockX = context.getOrigin().minBlockX() - 2;
      this.minBlockY = context.getOrigin().minBlockY() - 2;
      this.minBlockZ = context.getOrigin().minBlockZ() - 2;
      this.maxBlockX = context.getOrigin().maxBlockX() + 2;
      this.maxBlockY = context.getOrigin().maxBlockY() + 2;
      this.maxBlockZ = context.getOrigin().maxBlockZ() + 2;
      this.populateStamp++;
   }

   public int getColor(ColorResolver resolver, int blockX, int blockY, int blockZ) {
      blockX = Mth.clamp(blockX, this.minBlockX, this.maxBlockX) - this.minBlockX;
      blockY = Mth.clamp(blockY, this.minBlockY, this.maxBlockY) - this.minBlockY;
      blockZ = Mth.clamp(blockZ, this.minBlockZ, this.maxBlockZ) - this.minBlockZ;
      if (!this.slices.containsKey(resolver)) {
         this.initializeSlices(resolver);
      }

      LevelColorCache.Slice slice = ((LevelColorCache.Slice[])this.slices.get(resolver))[blockY];
      if (slice.lastPopulateStamp < this.populateStamp) {
         this.updateColorBuffers(blockY, resolver, slice);
      }

      BoxBlur.ColorBuffer buffer = slice.getBuffer();
      return buffer.get(blockX + this.blendRadius, blockZ + this.blendRadius);
   }

   private void initializeSlices(ColorResolver resolver) {
      LevelColorCache.Slice[] slice = new LevelColorCache.Slice[this.sizeY];

      for (int blockY = 0; blockY < this.sizeY; blockY++) {
         slice[blockY] = new LevelColorCache.Slice(this.sizeXZ);
      }

      this.slices.put(resolver, slice);
   }

   private void updateColorBuffers(int relY, ColorResolver resolver, LevelColorCache.Slice slice) {
      int blockY = this.minBlockY + relY;
      int minBlockZ = this.minBlockZ - this.blendRadius;
      int minBlockX = this.minBlockX - this.blendRadius;
      int maxBlockZ = this.maxBlockZ + this.blendRadius;
      int maxBlockX = this.maxBlockX + this.blendRadius;
      BoxBlur.ColorBuffer buffer = slice.buffer;

      for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
         for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
            Biome biome = (Biome)this.biomeData.getBiome(blockX, blockY, blockZ).value();
            int relBlockX = blockX - minBlockX;
            int relBlockZ = blockZ - minBlockZ;
            buffer.set(relBlockX, relBlockZ, resolver.getColor(biome, blockX, blockZ));
         }
      }

      if (this.blendRadius > 0) {
         BoxBlur.blur(buffer.data, this.tempColorBuffer.data, this.sizeXZ, this.sizeXZ, this.blendRadius);
      }

      slice.lastPopulateStamp = this.populateStamp;
   }

   private static class Slice {
      private final BoxBlur.ColorBuffer buffer;
      private long lastPopulateStamp;

      private Slice(int size) {
         this.buffer = new BoxBlur.ColorBuffer(size, size);
         this.lastPopulateStamp = 0L;
      }

      public BoxBlur.ColorBuffer getBuffer() {
         return this.buffer;
      }
   }
}
