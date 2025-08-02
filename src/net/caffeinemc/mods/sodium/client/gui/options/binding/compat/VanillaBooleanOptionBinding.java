package net.caffeinemc.mods.sodium.client.gui.options.binding.compat;

import net.caffeinemc.mods.sodium.client.gui.options.binding.OptionBinding;
import net.minecraft.client.Options;
import net.minecraft.server.commands.TitleCommand;
import net.minecraft.client.OptionInstance;

public class VanillaBooleanOptionBinding implements OptionBinding<Options, Boolean> {
   private final OptionInstance<Boolean> option;

   public VanillaBooleanOptionBinding(OptionInstance<Boolean> option) {
      this.option = option;
   }

   public void setValue(Options storage, Boolean value) {
      this.option.set(value);
   }

   public Boolean getValue(Options storage) {
      return (Boolean)this.option.get();
   }
}
