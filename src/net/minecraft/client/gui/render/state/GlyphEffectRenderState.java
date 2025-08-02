package net.minecraft.client.gui.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public record GlyphEffectRenderState(Matrix3x2f pose, BakedGlyph whiteGlyph, BakedGlyph.Effect effect, @Nullable ScreenRectangle scissorArea)
	implements GuiElementRenderState {
	@Override
	public void buildVertices(VertexConsumer vertexConsumer, float f) {
		Matrix4f matrix4f = new Matrix4f().mul(this.pose).translate(0.0F, 0.0F, f);
		this.whiteGlyph.renderEffect(this.effect, matrix4f, vertexConsumer, 15728880, true);
	}

	@Override
	public RenderPipeline pipeline() {
		return this.whiteGlyph.guiPipeline();
	}

	@Override
	public TextureSetup textureSetup() {
		return TextureSetup.singleTextureWithLightmap((GpuTextureView)Objects.requireNonNull(this.whiteGlyph.textureView()));
	}

	@Nullable
	@Override
	public ScreenRectangle bounds() {
		return null;
	}
}
