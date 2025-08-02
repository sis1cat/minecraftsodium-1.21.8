package net.caffeinemc.mods.sodium.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.InputType;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractWidget implements Renderable, GuiEventListener, NarratableEntry {
   protected final Font font = Minecraft.getInstance().font;
   protected boolean focused;
   protected boolean hovered;

   protected AbstractWidget() {
   }

   protected void drawString(GuiGraphics graphics, String text, int x, int y, int color) {
      graphics.drawString(this.font, text, x, y, color);
   }

   protected void drawString(GuiGraphics graphics, Component text, int x, int y, int color) {
      graphics.drawString(this.font, text, x, y, color);
   }

   public boolean isHovered() {
      return this.hovered;
   }

   protected void drawRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
      graphics.fill(x1, y1, x2, y2, color);
   }

   protected void playClickSound() {
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
   }

   protected int getStringWidth(FormattedText text) {
      return this.font.width(text);
   }

   @NotNull
   public NarrationPriority narrationPriority() {
      if (this.focused) {
         return NarrationPriority.FOCUSED;
      } else {
         return this.hovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
      }
   }

   public void updateNarration(NarrationElementOutput builder) {
      if (this.focused) {
         builder.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
      } else if (this.hovered) {
         builder.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
      }
   }

   @Nullable
   public ComponentPath nextFocusPath(FocusNavigationEvent event) {
      return !this.isFocused() ? ComponentPath.leaf(this) : null;
   }

   public boolean isFocused() {
      return this.focused;
   }

   public void setFocused(boolean focused) {
      if (!focused) {
         this.focused = false;
      } else {
         InputType inputType = Minecraft.getInstance().getLastInputType();
         if (inputType == InputType.KEYBOARD_TAB || inputType == InputType.KEYBOARD_ARROW) {
            this.focused = true;
         }
      }
   }

   protected void drawBorder(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
      graphics.fill(x1, y1, x2, y1 + 1, color);
      graphics.fill(x1, y2 - 1, x2, y2, color);
      graphics.fill(x1, y1, x1 + 1, y2, color);
      graphics.fill(x2 - 1, y1, x2, y2, color);
   }

   public abstract boolean isMouseOver(double var1, double var3);
}
