package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.*;

import java.util.Optional;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ARGB;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public class OutlineBufferSource implements MultiBufferSource {
	private final BufferSource bufferSource;
	private final BufferSource outlineBufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(1536));
	private int teamR = 255;
	private int teamG = 255;
	private int teamB = 255;
	private int teamA = 255;

	public OutlineBufferSource(BufferSource bufferSource) {
		this.bufferSource = bufferSource;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		if (renderType.isOutline()) {
			VertexConsumer vertexConsumer = this.outlineBufferSource.getBuffer(renderType);
			return new EntityOutlineGenerator(vertexConsumer, this.teamR, this.teamG, this.teamB, this.teamA);
		} else {
			VertexConsumer vertexConsumer = this.bufferSource.getBuffer(renderType);
			Optional<RenderType> optional = renderType.outline();
			if (optional.isPresent()) {
				VertexConsumer vertexConsumer2 = this.outlineBufferSource.getBuffer((RenderType)optional.get());
				EntityOutlineGenerator entityOutlineGenerator = new EntityOutlineGenerator(
					vertexConsumer2, this.teamR, this.teamG, this.teamB, this.teamA
				);
				return VertexMultiConsumer.create(entityOutlineGenerator, vertexConsumer);
			} else {
				return vertexConsumer;
			}
		}
	}

	public void setColor(int i, int j, int k, int l) {
		this.teamR = i;
		this.teamG = j;
		this.teamB = k;
		this.teamA = l;
	}

	public void endOutlineBatch() {
		this.outlineBufferSource.endBatch();
	}

	@Environment(EnvType.CLIENT)
	static class EntityOutlineGenerator implements VertexConsumer, VertexBufferWriter {

		private final VertexConsumer delegate;
		private final int colors;
		private final boolean canUseIntrinsics;

		public EntityOutlineGenerator(VertexConsumer delegate, int colors) {
			this.delegate = delegate;
			this.colors = colors;
			this.canUseIntrinsics = VertexBufferWriter.tryOf(this.delegate) != null;
		}

		public EntityOutlineGenerator(VertexConsumer vertexConsumer, int i, int j, int k, int l) {
			this(vertexConsumer, ARGB.color(l, i, j, k));
		}

		@Override
		public VertexConsumer addVertex(float f, float g, float h) {
			this.delegate.addVertex(f, g, h).setColor(this.colors);
			return this;
		}

		@Override
		public VertexConsumer setColor(int i, int j, int k, int l) {
			return this;
		}

		@Override
		public VertexConsumer setUv(float f, float g) {
			this.delegate.setUv(f, g);
			return this;
		}

		@Override
		public VertexConsumer setUv1(int i, int j) {
			return this;
		}

		@Override
		public VertexConsumer setUv2(int i, int j) {
			return this;
		}

		@Override
		public VertexConsumer setNormal(float f, float g, float h) {
			return this;
		}

		@Override
		public boolean canUseIntrinsics() {
			return this.canUseIntrinsics;
		}

		@Override
		public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
			transform(ptr, count, format, this.colors);
			VertexBufferWriter.of(this.delegate).push(stack, ptr, count, format);
		}

		private static void transform(long ptr, int count, VertexFormat format, int color) {
			long stride = format.getVertexSize();
			long offsetColor = format.getOffset(VertexFormatElement.COLOR);

			for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
				ColorAttribute.set(ptr + offsetColor, ColorARGB.toABGR(color));
				ptr += stride;
			}
		}

	}
}
