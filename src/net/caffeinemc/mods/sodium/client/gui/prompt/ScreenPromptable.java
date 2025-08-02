package net.caffeinemc.mods.sodium.client.gui.prompt;

import net.caffeinemc.mods.sodium.client.util.Dim2i;
import org.jetbrains.annotations.Nullable;

public interface ScreenPromptable {
   void setPrompt(@Nullable ScreenPrompt var1);

   @Nullable
   ScreenPrompt getPrompt();

   Dim2i getDimensions();
}
