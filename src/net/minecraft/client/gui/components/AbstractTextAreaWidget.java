package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class AbstractTextAreaWidget extends AbstractScrollArea {
	private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("widget/text_field"), ResourceLocation.withDefaultNamespace("widget/text_field_highlighted")
	);
	private static final int INNER_PADDING = 4;
	public static final int DEFAULT_TOTAL_PADDING = 8;
	private boolean showBackground = true;
	private boolean showDecorations = true;

	public AbstractTextAreaWidget(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	public AbstractTextAreaWidget(int i, int j, int k, int l, Component component, boolean bl, boolean bl2) {
		this(i, j, k, l, component);
		this.showBackground = bl;
		this.showDecorations = bl2;
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		boolean bl = this.updateScrolling(d, e, i);
		return super.mouseClicked(d, e, i) || bl;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		boolean bl = i == 265;
		boolean bl2 = i == 264;
		if (bl || bl2) {
			double d = this.scrollAmount();
			this.setScrollAmount(this.scrollAmount() + (bl ? -1 : 1) * this.scrollRate());
			if (d != this.scrollAmount()) {
				return true;
			}
		}

		return super.keyPressed(i, j, k);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.visible) {
			if (this.showBackground) {
				this.renderBackground(guiGraphics);
			}

			guiGraphics.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
			guiGraphics.pose().pushMatrix();
			guiGraphics.pose().translate(0.0F, (float)(-this.scrollAmount()));
			this.renderContents(guiGraphics, i, j, f);
			guiGraphics.pose().popMatrix();
			guiGraphics.disableScissor();
			this.renderScrollbar(guiGraphics);
			if (this.showDecorations) {
				this.renderDecorations(guiGraphics);
			}
		}
	}

	protected void renderDecorations(GuiGraphics guiGraphics) {
	}

	protected int innerPadding() {
		return 4;
	}

	protected int totalInnerPadding() {
		return this.innerPadding() * 2;
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		return this.active && this.visible && d >= this.getX() && e >= this.getY() && d < this.getRight() + 6 && e < this.getBottom();
	}

	@Override
	protected int scrollBarX() {
		return this.getRight();
	}

	@Override
	protected int contentHeight() {
		return this.getInnerHeight() + this.totalInnerPadding();
	}

	protected void renderBackground(GuiGraphics guiGraphics) {
		this.renderBorder(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	protected void renderBorder(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		ResourceLocation resourceLocation = BACKGROUND_SPRITES.get(this.isActive(), this.isFocused());
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, i, j, k, l);
	}

	protected boolean withinContentAreaTopBottom(int i, int j) {
		return j - this.scrollAmount() >= this.getY() && i - this.scrollAmount() <= this.getY() + this.height;
	}

	protected abstract int getInnerHeight();

	protected abstract void renderContents(GuiGraphics guiGraphics, int i, int j, float f);

	protected int getInnerLeft() {
		return this.getX() + this.innerPadding();
	}

	protected int getInnerTop() {
		return this.getY() + this.innerPadding();
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}
}
