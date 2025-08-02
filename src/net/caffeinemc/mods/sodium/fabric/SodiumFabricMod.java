package net.caffeinemc.mods.sodium.fabric;

import java.util.function.Consumer;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.render.frapi.SodiumRenderer;
import net.caffeinemc.mods.sodium.client.util.FlawlessFrames;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class SodiumFabricMod implements ClientModInitializer {
   public void onInitializeClient() {
      //ModContainer mod = (ModContainer)FabricLoader.getInstance().getModContainer("sodium").orElseThrow(NullPointerException::new);
      SodiumClientMod.onInitialization("Adapted version for vanilla");
      FabricLoader.getInstance().getEntrypoints("frex_flawless_frames", Consumer.class).forEach(api -> api.accept(FlawlessFrames.getProvider()));
      Renderer.register(SodiumRenderer.INSTANCE);
   }
}
