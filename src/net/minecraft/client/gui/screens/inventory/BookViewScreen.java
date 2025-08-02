package net.minecraft.client.gui.screens.inventory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BookViewScreen extends Screen {
	public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
	public static final int PAGE_TEXT_X_OFFSET = 36;
	public static final int PAGE_TEXT_Y_OFFSET = 30;
	private static final int BACKGROUND_TEXTURE_WIDTH = 256;
	private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
	private static final Component TITLE = Component.translatable("book.view.title");
	public static final BookViewScreen.BookAccess EMPTY_ACCESS = new BookViewScreen.BookAccess(List.of());
	public static final ResourceLocation BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/book.png");
	protected static final int TEXT_WIDTH = 114;
	protected static final int TEXT_HEIGHT = 128;
	protected static final int IMAGE_WIDTH = 192;
	protected static final int IMAGE_HEIGHT = 192;
	private BookViewScreen.BookAccess bookAccess;
	private int currentPage;
	private List<FormattedCharSequence> cachedPageComponents = Collections.emptyList();
	private int cachedPage = -1;
	private Component pageMsg = CommonComponents.EMPTY;
	private PageButton forwardButton;
	private PageButton backButton;
	private final boolean playTurnSound;

	public BookViewScreen(BookViewScreen.BookAccess bookAccess) {
		this(bookAccess, true);
	}

	public BookViewScreen() {
		this(EMPTY_ACCESS, false);
	}

	private BookViewScreen(BookViewScreen.BookAccess bookAccess, boolean bl) {
		super(TITLE);
		this.bookAccess = bookAccess;
		this.playTurnSound = bl;
	}

	public void setBookAccess(BookViewScreen.BookAccess bookAccess) {
		this.bookAccess = bookAccess;
		this.currentPage = Mth.clamp(this.currentPage, 0, bookAccess.getPageCount());
		this.updateButtonVisibility();
		this.cachedPage = -1;
	}

	public boolean setPage(int i) {
		int j = Mth.clamp(i, 0, this.bookAccess.getPageCount() - 1);
		if (j != this.currentPage) {
			this.currentPage = j;
			this.updateButtonVisibility();
			this.cachedPage = -1;
			return true;
		} else {
			return false;
		}
	}

	protected boolean forcePage(int i) {
		return this.setPage(i);
	}

	@Override
	protected void init() {
		this.createMenuControls();
		this.createPageControlButtons();
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinLines(super.getNarrationMessage(), this.getPageNumberMessage(), this.bookAccess.getPage(this.currentPage));
	}

	private Component getPageNumberMessage() {
		return Component.translatable("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
	}

	protected void createMenuControls() {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, 196, 200, 20).build());
	}

	protected void createPageControlButtons() {
		int i = (this.width - 192) / 2;
		int j = 2;
		this.forwardButton = this.addRenderableWidget(new PageButton(i + 116, 159, true, button -> this.pageForward(), this.playTurnSound));
		this.backButton = this.addRenderableWidget(new PageButton(i + 43, 159, false, button -> this.pageBack(), this.playTurnSound));
		this.updateButtonVisibility();
	}

	private int getNumPages() {
		return this.bookAccess.getPageCount();
	}

	protected void pageBack() {
		if (this.currentPage > 0) {
			this.currentPage--;
		}

		this.updateButtonVisibility();
	}

	protected void pageForward() {
		if (this.currentPage < this.getNumPages() - 1) {
			this.currentPage++;
		}

		this.updateButtonVisibility();
	}

	private void updateButtonVisibility() {
		this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
		this.backButton.visible = this.currentPage > 0;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else {
			switch (i) {
				case 266:
					this.backButton.onPress();
					return true;
				case 267:
					this.forwardButton.onPress();
					return true;
				default:
					return false;
			}
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		int k = (this.width - 192) / 2;
		int l = 2;
		if (this.cachedPage != this.currentPage) {
			FormattedText formattedText = this.bookAccess.getPage(this.currentPage);
			this.cachedPageComponents = this.font.split(formattedText, 114);
			this.pageMsg = this.getPageNumberMessage();
		}

		this.cachedPage = this.currentPage;
		int m = this.font.width(this.pageMsg);
		guiGraphics.drawString(this.font, this.pageMsg, k - m + 192 - 44, 18, -16777216, false);
		int n = Math.min(128 / 9, this.cachedPageComponents.size());

		for (int o = 0; o < n; o++) {
			FormattedCharSequence formattedCharSequence = (FormattedCharSequence)this.cachedPageComponents.get(o);
			guiGraphics.drawString(this.font, formattedCharSequence, k + 36, 32 + o * 9, -16777216, false);
		}

		Style style = this.getClickedComponentStyleAt(i, j);
		if (style != null) {
			guiGraphics.renderComponentHoverEffect(this.font, style, i, j);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderTransparentBackground(guiGraphics);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_LOCATION, (this.width - 192) / 2, 2, 0.0F, 0.0F, 192, 192, 256, 256);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (i == 0) {
			Style style = this.getClickedComponentStyleAt(d, e);
			if (style != null && this.handleComponentClicked(style)) {
				return true;
			}
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	protected void handleClickEvent(Minecraft minecraft, ClickEvent clickEvent) {
		LocalPlayer localPlayer = (LocalPlayer)Objects.requireNonNull(minecraft.player, "Player not available");
		switch (clickEvent) {
			case ClickEvent.ChangePage(int var13):
				this.forcePage(var13 - 1);
				break;
			case ClickEvent.RunCommand(String var10):
				this.closeContainerOnServer();
				clickCommandAction(localPlayer, var10, null);
				break;
			default:
				defaultHandleGameClickEvent(clickEvent, minecraft, this);
		}
	}

	protected void closeContainerOnServer() {
	}

	@Nullable
	public Style getClickedComponentStyleAt(double d, double e) {
		if (this.cachedPageComponents.isEmpty()) {
			return null;
		} else {
			int i = Mth.floor(d - (this.width - 192) / 2 - 36.0);
			int j = Mth.floor(e - 2.0 - 30.0);
			if (i >= 0 && j >= 0) {
				int k = Math.min(128 / 9, this.cachedPageComponents.size());
				if (i <= 114 && j < 9 * k + k) {
					int l = j / 9;
					if (l >= 0 && l < this.cachedPageComponents.size()) {
						FormattedCharSequence formattedCharSequence = (FormattedCharSequence)this.cachedPageComponents.get(l);
						return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedCharSequence, i);
					} else {
						return null;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public record BookAccess(List<Component> pages) {
		public int getPageCount() {
			return this.pages.size();
		}

		public Component getPage(int i) {
			return i >= 0 && i < this.getPageCount() ? (Component)this.pages.get(i) : CommonComponents.EMPTY;
		}

		@Nullable
		public static BookViewScreen.BookAccess fromItem(ItemStack pStack) {
			boolean flag = Minecraft.getInstance().isTextFilteringEnabled();
			WrittenBookContent writtenbookcontent = pStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
			if (writtenbookcontent != null) {
				return new BookViewScreen.BookAccess(writtenbookcontent.getPages(flag));
			} else {
				WritableBookContent writablebookcontent = pStack.get(DataComponents.WRITABLE_BOOK_CONTENT);
				return writablebookcontent != null
						? new BookViewScreen.BookAccess(writablebookcontent.getPages(flag).map(Component::literal).map(Component.class::cast).toList())
						: null;
			}
		}
	}
}
