package net.caffeinemc.mods.sodium.client.gui.options;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum OptionImpact implements TextProvider {
   LOW(ChatFormatting.GREEN, "sodium.option_impact.low"),
   MEDIUM(ChatFormatting.YELLOW, "sodium.option_impact.medium"),
   HIGH(ChatFormatting.GOLD, "sodium.option_impact.high"),
   VARIES(ChatFormatting.WHITE, "sodium.option_impact.varies");

   private final Component text;

   private OptionImpact(ChatFormatting formatting, String text) {
      this.text = Component.translatable(text).withStyle(formatting);
   }

   @Override
   public Component getLocalizedName() {
      return this.text;
   }
}
