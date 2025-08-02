package net.caffeinemc.mods.sodium.client.gui.options;

import java.util.Collection;
import net.caffeinemc.mods.sodium.client.gui.options.control.Control;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.network.chat.Component;

public interface Option<T> {
   Component getName();

   Component getTooltip();

   OptionImpact getImpact();

   Control<T> getControl();

   T getValue();

   void setValue(T var1);

   void reset();

   OptionStorage<?> getStorage();

   boolean isAvailable();

   boolean hasChanged();

   void applyChanges();

   Collection<OptionFlag> getFlags();
}
