package net.caffeinemc.mods.sodium.client.services;

import net.caffeinemc.mods.sodium.fabric.FabricMixinOverrides;

import java.util.List;

public interface PlatformMixinOverrides {
   PlatformMixinOverrides INSTANCE = new FabricMixinOverrides();

   static PlatformMixinOverrides getInstance() {
      return INSTANCE;
   }

   List<PlatformMixinOverrides.MixinOverride> applyModOverrides();

   public record MixinOverride(String modId, String option, boolean enabled) {
   }
}
