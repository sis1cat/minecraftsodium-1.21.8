package net.caffeinemc.mods.sodium.client.world;

import net.minecraft.world.level.Level;

public interface BiomeSeedProvider {
   static long getBiomeZoomSeed(Level level) {
      return ((BiomeSeedProvider)level).sodium$getBiomeZoomSeed();
   }

   long sodium$getBiomeZoomSeed();
}
