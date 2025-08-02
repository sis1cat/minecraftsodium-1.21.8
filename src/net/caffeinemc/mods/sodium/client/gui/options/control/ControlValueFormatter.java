package net.caffeinemc.mods.sodium.client.gui.options.control;

import com.mojang.blaze3d.platform.Monitor;
import net.caffeinemc.mods.sodium.client.compatibility.environment.OsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.SpreadPlayersCommand;

public interface ControlValueFormatter {
   static ControlValueFormatter guiScale() {
      return v -> v == 0 ? Component.translatable("options.guiScale.auto") : Component.literal(v + "x");
   }

   static ControlValueFormatter resolution() {
      return v -> {
         Monitor monitor = Minecraft.getInstance().getWindow().findBestMonitor();
         if (OsUtils.getOs() != OsUtils.OperatingSystem.WIN || monitor == null) {
            return Component.translatable("options.fullscreen.unavailable");
         } else {
            return 0 == v
               ? Component.translatable("options.fullscreen.current")
               : Component.literal(monitor.getMode(v - 1).toString().replace(" (24bit)", ""));
         }
      };
   }

   static ControlValueFormatter fpsLimit() {
      return v -> v == 260 ? Component.translatable("options.framerateLimit.max") : Component.translatable("options.framerate", new Object[]{v});
   }

   static ControlValueFormatter brightness() {
      return v -> {
         if (v == 0) {
            return Component.translatable("options.gamma.min");
         } else {
            return v == 100 ? Component.translatable("options.gamma.max") : Component.literal(v + "%");
         }
      };
   }

   static ControlValueFormatter biomeBlend() {
      return v -> v == 0 ? Component.translatable("gui.none") : Component.translatable("sodium.options.biome_blend.value", new Object[]{v});
   }

   Component format(int var1);

   static ControlValueFormatter translateVariable(String key) {
      return v -> Component.translatable(key, new Object[]{v});
   }

   static ControlValueFormatter percentage() {
      return v -> Component.literal(v + "%");
   }

   static ControlValueFormatter multiplier() {
      return v -> Component.literal(v + "x");
   }

   static ControlValueFormatter quantityOrDisabled(String name, String disableText) {
      return v -> Component.literal(v == 0 ? disableText : v + " " + name);
   }

   static ControlValueFormatter number() {
      return v -> Component.literal(String.valueOf(v));
   }
}
