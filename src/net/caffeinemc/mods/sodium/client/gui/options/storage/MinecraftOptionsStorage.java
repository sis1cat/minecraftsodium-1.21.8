package net.caffeinemc.mods.sodium.client.gui.options.storage;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.commands.TitleCommand;

public class MinecraftOptionsStorage implements OptionStorage<Options> {
   private final Minecraft minecraft = Minecraft.getInstance();

   public Options getData() {
      return this.minecraft.options;
   }

   @Override
   public void save() {
      this.getData().save();
      SodiumClientMod.logger().info("Flushed changes to Minecraft configuration");
   }
}
