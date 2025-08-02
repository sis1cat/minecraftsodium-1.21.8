package net.caffeinemc.mods.sodium.client.gui;

import com.google.common.collect.UnmodifiableIterator;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.console.Console;
import net.caffeinemc.mods.sodium.client.console.message.MessageLevel;
import net.caffeinemc.mods.sodium.client.data.fingerprint.HashedFingerprint;
import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.options.OptionFlag;
import net.caffeinemc.mods.sodium.client.gui.options.OptionGroup;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.options.control.Control;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPrompt;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPromptable;
import net.caffeinemc.mods.sodium.client.gui.screen.ConfigCorruptedScreen;
import net.caffeinemc.mods.sodium.client.gui.widgets.AbstractWidget;
import net.caffeinemc.mods.sodium.client.gui.widgets.FlatButtonWidget;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.client.gui.components.events.GuiEventListener;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton.State;
import net.minecraft.commands.synchronization.brigadier.LongArgumentInfo;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class SodiumOptionsGUI extends Screen implements ScreenPromptable {
   private final List<OptionPage> pages = new ArrayList<>();
   private final List<ControlElement<?>> controls = new ArrayList<>();
   private final Screen prevScreen;
   private OptionPage currentPage;
   private FlatButtonWidget applyButton;
   private FlatButtonWidget closeButton;
   private FlatButtonWidget undoButton;
   private FlatButtonWidget donateButton;
   private FlatButtonWidget hideDonateButton;
   private boolean hasPendingChanges;
   private ControlElement<?> hoveredElement;
   @Nullable
   private ScreenPrompt prompt;
   private static final List<FormattedText> DONATION_PROMPT_MESSAGE = List.of(
      FormattedText.composite(new FormattedText[]{Component.literal("Hello!")}),
      FormattedText.composite(
         new FormattedText[]{
            Component.literal("It seems that you've been enjoying "),
            Component.literal("Sodium").withColor(2616210),
            Component.literal(", the powerful and open rendering optimization mod for Minecraft.")
         }
      ),
      FormattedText.composite(
         new FormattedText[]{
            Component.literal("Mods like these are complex. They require "),
            Component.literal("thousands of hours").withColor(16739840),
            Component.literal(" of development, debugging, and tuning to create the experience that players have come to expect.")
         }
      ),
      FormattedText.composite(
         new FormattedText[]{
            Component.literal("If you'd like to show your token of appreciation, and support the development of our mod in the process, then consider "),
            Component.literal("buying us a coffee").withColor(15550926),
            Component.literal(".")
         }
      ),
      FormattedText.composite(new FormattedText[]{Component.literal("And thanks again for using our mod! We hope it helps you (and your computer.)")})
   );

   private SodiumOptionsGUI(Screen prevScreen) {
      super(Component.literal("Sodium Renderer Settings"));
      this.prevScreen = prevScreen;
      this.pages.add(SodiumGameOptionPages.general());
      this.pages.add(SodiumGameOptionPages.quality());
      this.pages.add(SodiumGameOptionPages.performance());
      this.pages.add(SodiumGameOptionPages.advanced());
      this.checkPromptTimers();
   }

   private void checkPromptTimers() {
      if (!PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()) {
         SodiumGameOptions options = SodiumClientMod.options();
         if (!options.notifications.hasSeenDonationPrompt) {
            HashedFingerprint fingerprint = null;

            try {
               fingerprint = HashedFingerprint.loadFromDisk();
            } catch (Throwable var5) {
               SodiumClientMod.logger().error("Failed to read the fingerprint from disk", var5);
            }

            if (fingerprint != null) {
               Instant now = Instant.now();
               Instant threshold = Instant.ofEpochSecond(fingerprint.timestamp()).plus(3L, ChronoUnit.DAYS);
               if (now.isAfter(threshold)) {
                  this.openDonationPrompt(options);
               }
            }
         }
      }
   }

   private void openDonationPrompt(SodiumGameOptions options) {
      ScreenPrompt prompt = new ScreenPrompt(
         this, DONATION_PROMPT_MESSAGE, 320, 190, new ScreenPrompt.Action(Component.literal("Buy us a coffee"), this::openDonationPage)
      );
      prompt.setFocused(true);
      options.notifications.hasSeenDonationPrompt = true;

      try {
         SodiumGameOptions.writeToDisk(options);
      } catch (IOException var4) {
         SodiumClientMod.logger().error("Failed to update config file", var4);
      }
   }

   public static Screen createScreen(Screen currentScreen) {
      return (Screen)(SodiumClientMod.options().isReadOnly()
         ? new ConfigCorruptedScreen(currentScreen, SodiumOptionsGUI::new)
         : new SodiumOptionsGUI(currentScreen));
   }

   public void setPage(OptionPage page) {
      this.currentPage = page;
      this.rebuildGUI();
   }

   protected void init() {
      super.init();
      this.rebuildGUI();
      if (this.prompt != null) {
         this.prompt.init();
      }
   }

   private void rebuildGUI() {
      this.controls.clear();
      this.clearWidgets();
      if (this.currentPage == null) {
         if (this.pages.isEmpty()) {
            throw new IllegalStateException("No pages are available?!");
         }

         this.currentPage = this.pages.get(0);
      }

      this.rebuildGUIPages();
      this.rebuildGUIOptions();
      this.undoButton = new FlatButtonWidget(
         new Dim2i(this.width - 211, this.height - 30, 65, 20), Component.translatable("sodium.options.buttons.undo"), this::undoChanges
      );
      this.applyButton = new FlatButtonWidget(
         new Dim2i(this.width - 142, this.height - 30, 65, 20), Component.translatable("sodium.options.buttons.apply"), this::applyChanges
      );
      this.closeButton = new FlatButtonWidget(
         new Dim2i(this.width - 73, this.height - 30, 65, 20), Component.translatable("gui.done"), this::onClose
      );
      this.donateButton = new FlatButtonWidget(
         new Dim2i(this.width - 128, 6, 100, 20), Component.translatable("sodium.options.buttons.donate"), this::openDonationPage
      );
      this.hideDonateButton = new FlatButtonWidget(new Dim2i(this.width - 26, 6, 20, 20), Component.literal("x"), this::hideDonationButton);
      if (SodiumClientMod.options().notifications.hasClearedDonationButton) {
         this.setDonationButtonVisibility(false);
      }

      this.addRenderableWidget(this.undoButton);
      this.addRenderableWidget(this.applyButton);
      this.addRenderableWidget(this.closeButton);
      this.addRenderableWidget(this.donateButton);
      this.addRenderableWidget(this.hideDonateButton);
   }

   private void setDonationButtonVisibility(boolean value) {
      this.donateButton.setVisible(value);
      this.hideDonateButton.setVisible(value);
   }

   private void hideDonationButton() {
      SodiumGameOptions options = SodiumClientMod.options();
      options.notifications.hasClearedDonationButton = true;

      try {
         SodiumGameOptions.writeToDisk(options);
      } catch (IOException var3) {
         throw new RuntimeException("Failed to save configuration", var3);
      }

      this.setDonationButtonVisibility(false);
   }

   private void rebuildGUIPages() {
      int x = 6;
      int y = 6;

      for (OptionPage page : this.pages) {
         int width = 12 + this.font.width(page.getName());
         FlatButtonWidget button = new FlatButtonWidget(new Dim2i(x, y, width, 18), page.getName(), () -> this.setPage(page));
         button.setSelected(this.currentPage == page);
         x += width + 6;
         this.addRenderableWidget(button);
      }
   }

   private void rebuildGUIOptions() {
      int x = 6;
      int y = 28;

      for (UnmodifiableIterator var3 = this.currentPage.getGroups().iterator(); var3.hasNext(); y += 4) {
         OptionGroup group = (OptionGroup)var3.next();

         for (UnmodifiableIterator var5 = group.getOptions().iterator(); var5.hasNext(); y += 18) {
            Option<?> option = (Option<?>)var5.next();
            Control<?> control = option.getControl();
            ControlElement<?> element = control.createElement(new Dim2i(x, y, 240, 18));
            this.addRenderableWidget(element);
            this.controls.add(element);
         }
      }
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
      this.updateControls();
      super.render(graphics, this.prompt != null ? -1 : mouseX, this.prompt != null ? -1 : mouseY, delta);
      if (this.hoveredElement != null) {
         this.renderOptionTooltip(graphics, this.hoveredElement);
      }

      if (this.prompt != null) {
         this.prompt.render(graphics, mouseX, mouseY, delta);
      }
   }

   private void updateControls() {
      ControlElement<?> hovered = this.getActiveControls()
         .filter(AbstractWidget::isHovered)
         .findFirst()
         .orElse(this.getActiveControls().filter(AbstractWidget::isFocused).findFirst().orElse(null));
      boolean hasChanges = this.getAllOptions().anyMatch(Option::hasChanged);

      for (OptionPage page : this.pages) {
         UnmodifiableIterator var5 = page.getOptions().iterator();

         while (var5.hasNext()) {
            Option<?> option = (Option<?>)var5.next();
            if (option.hasChanged()) {
               hasChanges = true;
            }
         }
      }

      this.applyButton.setEnabled(hasChanges);
      this.undoButton.setVisible(hasChanges);
      this.closeButton.setEnabled(!hasChanges);
      this.hasPendingChanges = hasChanges;
      this.hoveredElement = hovered;
   }

   private Stream<Option<?>> getAllOptions() {
      return this.pages.stream().flatMap(s -> s.getOptions().stream());
   }

   private Stream<ControlElement<?>> getActiveControls() {
      return this.controls.stream();
   }

   private void renderOptionTooltip(GuiGraphics graphics, ControlElement<?> element) {
      Dim2i dim = element.getDimensions();
      int textPadding = 3;
      int boxPadding = 3;
      int boxY = dim.y();
      int boxX = dim.getLimitX() + boxPadding;
      int boxWidth = Math.min(200, this.width - boxX - boxPadding);
      Option<?> option = element.getOption();
      int splitWidth = boxWidth - textPadding * 2;
      List<FormattedCharSequence> tooltip = new ArrayList<>(this.font.split(option.getTooltip(), splitWidth));
      OptionImpact impact = option.getImpact();
      if (impact != null) {
         MutableComponent impactText = Component.translatable("sodium.options.performance_impact_string", new Object[]{impact.getLocalizedName()});
         tooltip.addAll(this.font.split(impactText.withStyle(ChatFormatting.GRAY), splitWidth));
      }

      int boxHeight = tooltip.size() * 12 + boxPadding;
      int boxYLimit = boxY + boxHeight;
      int boxYCutoff = this.height - 40;
      if (boxYLimit > boxYCutoff) {
         boxY -= boxYLimit - boxYCutoff;
      }

      graphics.fillGradient(boxX, boxY, boxX + boxWidth, boxY + boxHeight, -536870912, -536870912);

      for (int i = 0; i < tooltip.size(); i++) {
         graphics.drawString(this.font, tooltip.get(i), boxX + textPadding, boxY + textPadding + i * 12, -1);
      }
   }

   private void applyChanges() {
      HashSet<OptionStorage<?>> dirtyStorages = new HashSet<>();
      EnumSet<OptionFlag> flags = EnumSet.noneOf(OptionFlag.class);
      this.getAllOptions().forEach(option -> {
         if (option.hasChanged()) {
            option.applyChanges();
            flags.addAll(option.getFlags());
            dirtyStorages.add(option.getStorage());
         }
      });
      Minecraft client = Minecraft.getInstance();
      if (client.level != null) {
         if (flags.contains(OptionFlag.REQUIRES_RENDERER_RELOAD)) {
            client.levelRenderer.allChanged();
         } else if (flags.contains(OptionFlag.REQUIRES_RENDERER_UPDATE)) {
            client.levelRenderer.needsUpdate();
         }
      }

      if (flags.contains(OptionFlag.REQUIRES_ASSET_RELOAD)) {
         client.updateMaxMipLevel((Integer)client.options.mipmapLevels().get());
         client.delayTextureReload();
      }

      if (flags.contains(OptionFlag.REQUIRES_VIDEOMODE_RELOAD)) {
         client.getWindow().changeFullscreenVideoMode();
      }

      if (flags.contains(OptionFlag.REQUIRES_GAME_RESTART)) {
         Console.instance().logMessage(MessageLevel.WARN, "sodium.console.game_restart", true, 10.0);
      }

      for (OptionStorage<?> storage : dirtyStorages) {
         storage.save();
      }
   }

   private void undoChanges() {
      this.getAllOptions().forEach(Option::reset);
   }

   private void openDonationPage() {
      Util.getPlatform().openUri("https://caffeinemc.net/donate");
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.prompt != null && this.prompt.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (this.prompt == null && keyCode == 80 && (modifiers & 1) != 0) {
         Minecraft.getInstance().setScreen(new VideoSettingsScreen(this.prevScreen, Minecraft.getInstance(), Minecraft.getInstance().options));
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.prompt != null) {
         return this.prompt.mouseClicked(mouseX, mouseY, button);
      } else {
         boolean clicked = super.mouseClicked(mouseX, mouseY, button);
         if (!clicked) {
            this.setFocused(null);
            return true;
         } else {
            return clicked;
         }
      }
   }

   public boolean shouldCloseOnEsc() {
      return !this.hasPendingChanges;
   }

   public void onClose() {
      this.minecraft.setScreen(this.prevScreen);
   }

   public List<? extends GuiEventListener> children() {
      return this.prompt == null ? super.children() : this.prompt.getWidgets();
   }

   @Override
   public void setPrompt(@Nullable ScreenPrompt prompt) {
      this.prompt = prompt;
   }

   @Nullable
   @Override
   public ScreenPrompt getPrompt() {
      return this.prompt;
   }

   @Override
   public Dim2i getDimensions() {
      return new Dim2i(0, 0, this.width, this.height);
   }
}
