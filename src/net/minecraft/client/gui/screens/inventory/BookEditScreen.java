package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;

@Environment(EnvType.CLIENT)
public class BookEditScreen extends Screen {
	public static final int TEXT_WIDTH = 114;
	public static final int TEXT_HEIGHT = 126;
	public static final int IMAGE_WIDTH = 192;
	public static final int IMAGE_HEIGHT = 192;
	public static final int BACKGROUND_TEXTURE_WIDTH = 256;
	public static final int BACKGROUND_TEXTURE_HEIGHT = 256;
	private static final Component TITLE = Component.translatable("book.edit.title");
	private final Player owner;
	private final ItemStack book;
	private final BookSignScreen signScreen;
	private int currentPage;
	private final List<String> pages = Lists.<String>newArrayList();
	private PageButton forwardButton;
	private PageButton backButton;
	private final InteractionHand hand;
	private Component numberOfPages = CommonComponents.EMPTY;
	private MultiLineEditBox page;

	public BookEditScreen(Player player, ItemStack itemStack, InteractionHand interactionHand, WritableBookContent writableBookContent) {
		super(TITLE);
		this.owner = player;
		this.book = itemStack;
		this.hand = interactionHand;
		writableBookContent.getPages(Minecraft.getInstance().isTextFilteringEnabled()).forEach(this.pages::add);
		if (this.pages.isEmpty()) {
			this.pages.add("");
		}

		this.signScreen = new BookSignScreen(this, player, interactionHand, this.pages);
	}

	private int getNumPages() {
		return this.pages.size();
	}

	@Override
	protected void init() {
		int i = (this.width - 192) / 2;
		int j = 2;
		int k = 8;
		this.page = MultiLineEditBox.builder()
			.setShowDecorations(false)
			.setTextColor(-16777216)
			.setCursorColor(-16777216)
			.setShowBackground(false)
			.setTextShadow(false)
			.setX((this.width - 114) / 2 - 8)
			.setY(28)
			.build(this.font, 122, 134, CommonComponents.EMPTY);
		this.page.setCharacterLimit(1024);
		this.page.setLineLimit(126 / 9);
		this.page.setValueListener(string -> this.pages.set(this.currentPage, string));
		this.addRenderableWidget(this.page);
		this.updatePageContent();
		this.numberOfPages = this.getPageNumberMessage();
		this.backButton = this.addRenderableWidget(new PageButton(i + 43, 159, false, button -> this.pageBack(), true));
		this.forwardButton = this.addRenderableWidget(new PageButton(i + 116, 159, true, button -> this.pageForward(), true));
		this.addRenderableWidget(
			Button.builder(Component.translatable("book.signButton"), button -> this.minecraft.setScreen(this.signScreen))
				.bounds(this.width / 2 - 100, 196, 98, 20)
				.build()
		);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			this.minecraft.setScreen(null);
			this.saveChanges();
		}).bounds(this.width / 2 + 2, 196, 98, 20).build());
		this.updateButtonVisibility();
	}

	@Override
	protected void setInitialFocus() {
		this.setInitialFocus(this.page);
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), this.getPageNumberMessage());
	}

	private Component getPageNumberMessage() {
		return Component.translatable("book.pageIndicator", this.currentPage + 1, this.getNumPages());
	}

	private void pageBack() {
		if (this.currentPage > 0) {
			this.currentPage--;
			this.updatePageContent();
		}

		this.updateButtonVisibility();
	}

	private void pageForward() {
		if (this.currentPage < this.getNumPages() - 1) {
			this.currentPage++;
		} else {
			this.appendPageToBook();
			if (this.currentPage < this.getNumPages() - 1) {
				this.currentPage++;
			}
		}

		this.updatePageContent();
		this.updateButtonVisibility();
	}

	private void updatePageContent() {
		this.page.setValue((String)this.pages.get(this.currentPage), true);
		this.numberOfPages = this.getPageNumberMessage();
	}

	private void updateButtonVisibility() {
		this.backButton.visible = this.currentPage > 0;
	}

	private void eraseEmptyTrailingPages() {
		ListIterator<String> listIterator = this.pages.listIterator(this.pages.size());

		while (listIterator.hasPrevious() && ((String)listIterator.previous()).isEmpty()) {
			listIterator.remove();
		}
	}

	private void saveChanges() {
		this.eraseEmptyTrailingPages();
		this.updateLocalCopy();
		int i = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().getSelectedSlot() : 40;
		this.minecraft.getConnection().send(new ServerboundEditBookPacket(i, this.pages, Optional.empty()));
	}

	private void updateLocalCopy() {
		this.book.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(this.pages.stream().map(Filterable::passThrough).toList()));
	}

	private void appendPageToBook() {
		if (this.getNumPages() < 100) {
			this.pages.add("");
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		switch (i) {
			case 266:
				this.backButton.onPress();
				return true;
			case 267:
				this.forwardButton.onPress();
				return true;
			default:
				return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		int k = (this.width - 192) / 2;
		int l = 2;
		int m = this.font.width(this.numberOfPages);
		guiGraphics.drawString(this.font, this.numberOfPages, k - m + 192 - 44, 18, -16777216, false);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderTransparentBackground(guiGraphics);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION, (this.width - 192) / 2, 2, 0.0F, 0.0F, 192, 192, 256, 256);
	}
}
