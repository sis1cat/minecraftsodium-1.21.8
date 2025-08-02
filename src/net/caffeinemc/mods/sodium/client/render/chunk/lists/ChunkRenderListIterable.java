package net.caffeinemc.mods.sodium.client.render.chunk.lists;

import java.util.Iterator;

public interface ChunkRenderListIterable {
   Iterator<ChunkRenderList> iterator(boolean var1);

   default Iterator<ChunkRenderList> iterator() {
      return this.iterator(false);
   }
}
