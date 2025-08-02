package net.caffeinemc.mods.sodium.client.render.chunk.map;

import net.minecraft.world.level.Level;

public interface ChunkTrackerHolder {
   static ChunkTracker get(Level level) {
      return ((ChunkTrackerHolder)level).sodium$getTracker();
   }

   ChunkTracker sodium$getTracker();
}
