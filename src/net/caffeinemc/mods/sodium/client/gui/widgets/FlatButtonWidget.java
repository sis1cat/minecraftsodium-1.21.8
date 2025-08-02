package net.caffeinemc.mods.sodium.client.gui.widgets;

import java.util.Objects;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.navigation.CommonInputs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlatButtonWidget extends AbstractWidget implements Renderable {
   private final Dim2i dim;
   private final Runnable action;
   @NotNull
   private FlatButtonWidget.Style style = FlatButtonWidget.Style.defaults();
   private boolean selected;
   private boolean enabled = true;
   private boolean visible = true;
   private Component label;

   public FlatButtonWidget(Dim2i dim, Component label, Runnable action) {
      this.dim = dim;
      this.label = label;
      this.action = action;
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
      if (this.visible) {
         this.hovered = this.dim.containsCursor(mouseX, mouseY);
         int backgroundColor = this.enabled ? (this.hovered ? this.style.bgHovered : this.style.bgDefault) : this.style.bgDisabled;
         int textColor = this.enabled ? this.style.textDefault : this.style.textDisabled;
         int strWidth = this.font.width(this.label);
         this.drawRect(graphics, this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), backgroundColor);
         this.drawString(graphics, this.label, this.dim.getCenterX() - strWidth / 2, this.dim.getCenterY() - 4, textColor);
         if (this.enabled && this.selected) {
            this.drawRect(graphics, this.dim.x(), this.dim.getLimitY() - 1, this.dim.getLimitX(), this.dim.getLimitY(), -7019309);
         }

         if (this.enabled && this.isFocused()) {
            this.drawBorder(graphics, this.dim.x(), this.dim.y(), this.dim.getLimitX(), this.dim.getLimitY(), -1);
         }
      }
   }

   public void setStyle(@NotNull FlatButtonWidget.Style style) {
      Objects.requireNonNull(style);
      this.style = style;
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.enabled || !this.visible) {
         return false;
      } else if (button == 0 && this.dim.containsCursor(mouseX, mouseY)) {
         this.doAction();
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.isFocused()) {
         return false;
      } else if (CommonInputs.selected(keyCode)) {
         this.doAction();
         return true;
      } else {
         return false;
      }
   }

   private void doAction() {
      this.action.run();
      this.playClickSound();
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public void setLabel(Component text) {
      this.label = text;
   }

   public Component getLabel() {
      return this.label;
   }

   @Nullable
   @Override
   public ComponentPath nextFocusPath(FocusNavigationEvent event) {
      return this.enabled && this.visible ? super.nextFocusPath(event) : null;
   }

   @Override
   public boolean isMouseOver(double x, double y) {
      return this.dim.containsCursor(x, y);
   }

   public ScreenRectangle getRectangle() {
      return new ScreenRectangle(this.dim.x(), this.dim.y(), this.dim.width(), this.dim.height());
   }

   public static class Style {
      public int bgHovered;
      public int bgDefault;
      public int bgDisabled;
      public int textDefault;
      public int textDisabled;

      public static FlatButtonWidget.Style defaults() {
         FlatButtonWidget.Style style = new FlatButtonWidget.Style();
         style.bgHovered = -536870912;
         style.bgDefault = -1879048192;
         style.bgDisabled = 1610612736;
         style.textDefault = -1;
         style.textDisabled = -1862270977;
         return style;
      }
   }
}
