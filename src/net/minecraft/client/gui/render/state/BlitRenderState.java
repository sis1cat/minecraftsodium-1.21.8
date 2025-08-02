package net.minecraft.client.gui.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(EnvType.CLIENT)
public record BlitRenderState(
	RenderPipeline pipeline,
	TextureSetup textureSetup,
	Matrix3x2f pose,
	int x0,
	int y0,
	int x1,
	int y1,
	float u0,
	float u1,
	float v0,
	float v1,
	int color,
	@Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
	public BlitRenderState(
		RenderPipeline renderPipeline,
		TextureSetup textureSetup,
		Matrix3x2f matrix3x2f,
		int i,
		int j,
		int k,
		int l,
		float f,
		float g,
		float h,
		float m,
		int n,
		@Nullable ScreenRectangle screenRectangle
	) {
		this(renderPipeline, textureSetup, matrix3x2f, i, j, k, l, f, g, h, m, n, screenRectangle, getBounds(i, j, k, l, matrix3x2f, screenRectangle));
	}

	@Override
	public void buildVertices(VertexConsumer vertexConsumer, float f) {
		vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), f).setUv(this.u0(), this.v0()).setColor(this.color());
		vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), f).setUv(this.u0(), this.v1()).setColor(this.color());
		vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), f).setUv(this.u1(), this.v1()).setColor(this.color());
		vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), f).setUv(this.u1(), this.v0()).setColor(this.color());
	}

	@Nullable
	private static ScreenRectangle getBounds(int i, int j, int k, int l, Matrix3x2f matrix3x2f, @Nullable ScreenRectangle screenRectangle) {
		ScreenRectangle screenRectangle2 = new ScreenRectangle(i, j, k - i, l - j).transformMaxBounds(matrix3x2f);
		return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
	}
}
