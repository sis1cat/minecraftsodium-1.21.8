package net.caffeinemc.mods.sodium.client.services;

import java.util.ServiceLoader;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;

public class Services {
   public static <T> T load(Class<T> clazz) {
      T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
      SodiumClientMod.logger().debug("Loaded {} for service {}", loadedService, clazz);
      return loadedService;
   }
}
