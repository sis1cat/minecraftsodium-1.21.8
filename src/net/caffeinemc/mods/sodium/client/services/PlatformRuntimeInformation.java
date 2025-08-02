package net.caffeinemc.mods.sodium.client.services;

import net.minecraft.client.Minecraft;

import java.net.URI;
import java.nio.file.Path;

public interface PlatformRuntimeInformation {
   PlatformRuntimeInformation INSTANCE = new PlatformRuntimeInformation() {
      @Override
      public boolean isDevelopmentEnvironment() {
         return false;
      }

      @Override
      public Path getGameDirectory() {
         return Minecraft.getInstance().gameDirectory.toPath();
      }

      @Override
      public Path getConfigDirectory() {
         return Path.of(Minecraft.getInstance().gameDirectory.getPath().concat("/config"));
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
      public boolean isModInLoadingList(String var1) {
         return false;
      }

      @Override
      public boolean usesAlphaMultiplication() {
         return false;
      }
   };

   static PlatformRuntimeInformation getInstance() {
      return INSTANCE;
   }

   boolean isDevelopmentEnvironment();

   Path getGameDirectory();

   Path getConfigDirectory();

   boolean platformHasEarlyLoadingScreen();

   boolean platformUsesRefmap();

   boolean isModInLoadingList(String var1);

   boolean usesAlphaMultiplication();
}
