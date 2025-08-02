package net.caffeinemc.mods.sodium.fabric;

import java.nio.file.Path;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.fabricmc.loader.api.FabricLoader;

public class FabricRuntimeInformation implements PlatformRuntimeInformation {
   @Override
   public boolean isDevelopmentEnvironment() {
      return FabricLoader.getInstance().isDevelopmentEnvironment();
   }

   @Override
   public Path getGameDirectory() {
      return FabricLoader.getInstance().getGameDir();
   }

   @Override
   public Path getConfigDirectory() {
      return FabricLoader.getInstance().getConfigDir();
   }

   @Override
   public boolean platformHasEarlyLoadingScreen() {
      return false;
   }

   @Override
   public boolean platformUsesRefmap() {
      return true;
   }

   @Override
   public boolean isModInLoadingList(String modId) {
      return FabricLoader.getInstance().isModLoaded(modId);
   }

   @Override
   public boolean usesAlphaMultiplication() {
      return false;
   }
}
