package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public class SpriteCoordinateExpander implements VertexConsumer, VertexBufferWriter {
	private final VertexConsumer delegate;
	private final TextureAtlasSprite sprite;
	private final boolean canUseIntrinsics;
	private final float minU;
	private final float minV;
	private final float maxU;
	private final float maxV;

	public SpriteCoordinateExpander(VertexConsumer vertexConsumer, TextureAtlasSprite textureAtlasSprite) {
		this.delegate = vertexConsumer;
		this.sprite = textureAtlasSprite;
		this.minU = sprite.getU0();
		this.minV = sprite.getV0();
		this.maxU = sprite.getU1();
		this.maxV = sprite.getV1();
		this.canUseIntrinsics = VertexBufferWriter.tryOf(this.delegate) != null;
	}

	@Override
	public boolean canUseIntrinsics() {
		return this.canUseIntrinsics;
	}

	@Override
	public VertexConsumer addVertex(float f, float g, float h) {
		return this.delegate.addVertex(f, g, h);
	}

	@Override
	public VertexConsumer setColor(int i, int j, int k, int l) {
		return this.delegate.setColor(i, j, k, l);
	}

	@Override
	public VertexConsumer setUv(float f, float g) {
		return this.delegate.setUv(this.sprite.getU(f), this.sprite.getV(g));
	}

	@Override
	public VertexConsumer setUv1(int i, int j) {
		return this.delegate.setUv1(i, j);
	}

	@Override
	public VertexConsumer setUv2(int i, int j) {
		return this.delegate.setUv2(i, j);
	}

	@Override
	public VertexConsumer setNormal(float f, float g, float h) {
		return this.delegate.setNormal(f, g, h);
	}

	@Override
	public void addVertex(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o, float p) {
		this.delegate.addVertex(f, g, h, i, this.sprite.getU(j), this.sprite.getV(k), l, m, n, o, p);
	}

	@Override
	public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
		transform(ptr, count, format, this.minU, this.minV, this.maxU, this.maxV);
		VertexBufferWriter.of(this.delegate).push(stack, ptr, count, format);
	}

	private static void transform(long ptr, int count, VertexFormat format, float minU, float minV, float maxU, float maxV) {
		long stride = format.getVertexSize();
		long offsetUV = format.getOffset(VertexFormatElement.UV0);
		float w = maxU - minU;
		float h = maxV - minV;

		for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
			float u = TextureAttribute.getU(ptr + offsetUV);
			float v = TextureAttribute.getV(ptr + offsetUV);
			float ut = minU + w * u;
			float vt = minV + h * v;
			TextureAttribute.put(ptr + offsetUV, ut, vt);
			ptr += stride;
		}
	}

}
