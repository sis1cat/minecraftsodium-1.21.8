package net.caffeinemc.mods.sodium.client.gui.options.control;

import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ControlElement<T> extends AbstractWidget {
   protected final Option<T> option;
   protected final Dim2i dim;

   public ControlElement(Option<T> option, Dim2i dim) {
      this.option = option;
      this.dim = dim;
   }

   public int getContentWidth() {
      return this.option.getControl().getMaxWidth();
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
      String name = this.option.getName().getString();
      if (this.option.isAvailable() && this.option.hasChanged()) {
         name = name + " *";
      }

      if (this.hovered || this.isFocused()) {
         name = this.truncateLabelToFit(name);
      }

      String label;
      if (this.option.isAvailable()) {
         if (this.option.hasChanged()) {
            label = ChatFormatting.ITALIC + name;
         } else {
            label = ChatFormatting.WHITE + name;
         }
      } else {
         label = "" + ChatFormatting.GRAY + ChatFormatting.STRIKETHROUGH + name;
      }

      this.hovered = this.dim.containsCursor(mouseX, mouseY);
      this.drawRect(graphics, this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), this.hovered ? -536870912 : -1879048192);
      this.drawString(graphics, label, this.dim.x() + 6, this.dim.getCenterY() - 4, -1);
      if (this.isFocused()) {
         this.drawBorder(graphics, this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), -1);
      }
   }

   @NotNull
   private String truncateLabelToFit(String name) {
      String suffix = "...";
      int suffixWidth = this.font.width(suffix);
      int nameFontWidth = this.font.width(name);
      int targetWidth = this.dim.width() - this.getContentWidth() - 20;
      if (nameFontWidth > targetWidth) {
         targetWidth -= suffixWidth;
         int maxLabelChars = name.length() - 3;
         int minLabelChars = 1;

         while (maxLabelChars - minLabelChars > 1) {
            int mid = (maxLabelChars + minLabelChars) / 2;
            String midName = name.substring(0, mid);
            int midWidth = this.font.width(midName);
            if (midWidth > targetWidth) {
               maxLabelChars = mid;
            } else {
               minLabelChars = mid;
            }
         }

         name = name.substring(0, minLabelChars).trim() + suffix;
      }

      return name;
   }

   public Option<T> getOption() {
      return this.option;
   }

   public Dim2i getDimensions() {
      return this.dim;
   }

   @Nullable
   @Override
   public ComponentPath nextFocusPath(FocusNavigationEvent event) {
      return !this.option.isAvailable() ? null : super.nextFocusPath(event);
   }

   public ScreenRectangle getRectangle() {
      return new ScreenRectangle(this.dim.x(), this.dim.y(), this.dim.width(), this.dim.height());
   }

   @Override
   public boolean isMouseOver(double x, double y) {
      return this.dim.containsCursor(x, y);
   }
}
