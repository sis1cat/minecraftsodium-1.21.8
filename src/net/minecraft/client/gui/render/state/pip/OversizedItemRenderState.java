package net.minecraft.client.gui.render.state.pip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(EnvType.CLIENT)
public record OversizedItemRenderState(GuiItemRenderState guiItemRenderState, int x0, int y0, int x1, int y1) implements PictureInPictureRenderState {
	@Override
	public float scale() {
		return 16.0F;
	}

	@Override
	public Matrix3x2f pose() {
		return this.guiItemRenderState.pose();
	}

	@Nullable
	@Override
	public ScreenRectangle scissorArea() {
		return this.guiItemRenderState.scissorArea();
	}

	@Nullable
	@Override
	public ScreenRectangle bounds() {
		return this.guiItemRenderState.bounds();
	}
}
