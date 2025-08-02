package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.GraphicsWorkarounds;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatExtensions;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
@DontObfuscate
public class VertexFormat implements VertexFormatExtensions {
	public static final int UNKNOWN_ELEMENT = -1;
	private static final boolean USE_STAGING_BUFFER_WORKAROUND = Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64();
	@Nullable
	private static GpuBuffer UPLOAD_STAGING_BUFFER;
	private final List<VertexFormatElement> elements;
	private final List<String> names;
	private final int vertexSize;
	private final int elementsMask;
	private final int[] offsetsByElement = new int[32];
	@Nullable
	private GpuBuffer immediateDrawVertexBuffer;
	@Nullable
	private GpuBuffer immediateDrawIndexBuffer;
	private int sodium$globalId;

	VertexFormat(List<VertexFormatElement> list, List<String> list2, IntList intList, int i) {
		this.elements = list;
		this.names = list2;
		this.vertexSize = i;
		this.elementsMask = list.stream().mapToInt(VertexFormatElement::mask).reduce(0, (ix, jx) -> ix | jx);

		for (int j = 0; j < this.offsetsByElement.length; j++) {
			VertexFormatElement vertexFormatElement = VertexFormatElement.byId(j);
			int k = vertexFormatElement != null ? list.indexOf(vertexFormatElement) : -1;
			this.offsetsByElement[j] = k != -1 ? intList.getInt(k) : -1;
		}
		this.sodium$globalId = VertexFormatRegistry.instance().allocateGlobalId((VertexFormat)this);
	}

	public int sodium$getGlobalId() {
		return this.sodium$globalId;
	}

	public static VertexFormat.Builder builder() {
		return new VertexFormat.Builder();
	}

	public String toString() {
		return "VertexFormat" + this.names;
	}

	public int getVertexSize() {
		return this.vertexSize;
	}

	public List<VertexFormatElement> getElements() {
		return this.elements;
	}

	public List<String> getElementAttributeNames() {
		return this.names;
	}

	public int[] getOffsetsByElement() {
		return this.offsetsByElement;
	}

	public int getOffset(VertexFormatElement vertexFormatElement) {
		return this.offsetsByElement[vertexFormatElement.id()];
	}

	public boolean contains(VertexFormatElement vertexFormatElement) {
		return (this.elementsMask & vertexFormatElement.mask()) != 0;
	}

	public int getElementsMask() {
		return this.elementsMask;
	}

	public String getElementName(VertexFormatElement vertexFormatElement) {
		int i = this.elements.indexOf(vertexFormatElement);
		if (i == -1) {
			throw new IllegalArgumentException(vertexFormatElement + " is not contained in format");
		} else {
			return (String)this.names.get(i);
		}
	}

	public boolean equals(Object object) {
		return this == object
			? true
			: object instanceof VertexFormat vertexFormat
				&& this.elementsMask == vertexFormat.elementsMask
				&& this.vertexSize == vertexFormat.vertexSize
				&& this.names.equals(vertexFormat.names)
				&& Arrays.equals(this.offsetsByElement, vertexFormat.offsetsByElement);
	}

	public int hashCode() {
		return this.elementsMask * 31 + Arrays.hashCode(this.offsetsByElement);
	}

	private static GpuBuffer uploadToBuffer(@Nullable GpuBuffer gpuBuffer, ByteBuffer byteBuffer, int i, Supplier<String> supplier) {
		GpuDevice gpuDevice = RenderSystem.getDevice();
		if (gpuBuffer == null) {
			gpuBuffer = gpuDevice.createBuffer(supplier, i, byteBuffer);
		} else {
			CommandEncoder commandEncoder = gpuDevice.createCommandEncoder();
			if (gpuBuffer.size() < byteBuffer.remaining()) {
				gpuBuffer.close();
				gpuBuffer = gpuDevice.createBuffer(supplier, i, byteBuffer);
			} else {
				commandEncoder.writeToBuffer(gpuBuffer.slice(), byteBuffer);
			}
		}

		return gpuBuffer;
	}

	private GpuBuffer uploadToBufferWithWorkaround(@Nullable GpuBuffer gpuBuffer, ByteBuffer byteBuffer, int i, Supplier<String> supplier) {
		GpuDevice gpuDevice = RenderSystem.getDevice();
		if (USE_STAGING_BUFFER_WORKAROUND) {
			if (gpuBuffer == null) {
				gpuBuffer = gpuDevice.createBuffer(supplier, i, byteBuffer);
			} else {
				CommandEncoder commandEncoder = gpuDevice.createCommandEncoder();
				if (gpuBuffer.size() < byteBuffer.remaining()) {
					gpuBuffer.close();
					gpuBuffer = gpuDevice.createBuffer(supplier, i, byteBuffer);
				} else {
					UPLOAD_STAGING_BUFFER = uploadToBuffer(UPLOAD_STAGING_BUFFER, byteBuffer, i, supplier);
					commandEncoder.copyToBuffer(UPLOAD_STAGING_BUFFER.slice(0, byteBuffer.remaining()), gpuBuffer.slice(0, byteBuffer.remaining()));
				}
			}

			return gpuBuffer;
		} else if (GraphicsWorkarounds.get(gpuDevice).alwaysCreateFreshImmediateBuffer()) {
			if (gpuBuffer != null) {
				gpuBuffer.close();
			}

			return gpuDevice.createBuffer(supplier, i, byteBuffer);
		} else {
			return uploadToBuffer(gpuBuffer, byteBuffer, i, supplier);
		}
	}

	public GpuBuffer uploadImmediateVertexBuffer(ByteBuffer byteBuffer) {
		this.immediateDrawVertexBuffer = this.uploadToBufferWithWorkaround(
			this.immediateDrawVertexBuffer, byteBuffer, 40, () -> "Immediate vertex buffer for " + this
		);
		return this.immediateDrawVertexBuffer;
	}

	public GpuBuffer uploadImmediateIndexBuffer(ByteBuffer byteBuffer) {
		this.immediateDrawIndexBuffer = this.uploadToBufferWithWorkaround(this.immediateDrawIndexBuffer, byteBuffer, 72, () -> "Immediate index buffer for " + this);
		return this.immediateDrawIndexBuffer;
	}

	@Environment(EnvType.CLIENT)
	@DontObfuscate
	public static class Builder {
		private final ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
		private final IntList offsets = new IntArrayList();
		private int offset;

		Builder() {
		}

		public VertexFormat.Builder add(String string, VertexFormatElement vertexFormatElement) {
			this.elements.put(string, vertexFormatElement);
			this.offsets.add(this.offset);
			this.offset = this.offset + vertexFormatElement.byteSize();
			return this;
		}

		public VertexFormat.Builder padding(int i) {
			this.offset += i;
			return this;
		}

		public VertexFormat build() {
			ImmutableMap<String, VertexFormatElement> immutableMap = this.elements.buildOrThrow();
			ImmutableList<VertexFormatElement> immutableList = immutableMap.values().asList();
			ImmutableList<String> immutableList2 = immutableMap.keySet().asList();
			return new VertexFormat(immutableList, immutableList2, this.offsets, this.offset);
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum IndexType {
		SHORT(2),
		INT(4);

		public final int bytes;

		private IndexType(final int j) {
			this.bytes = j;
		}

		public static VertexFormat.IndexType least(int i) {
			return (i & -65536) != 0 ? INT : SHORT;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Mode {
		LINES(2, 2, false),
		LINE_STRIP(2, 1, true),
		DEBUG_LINES(2, 2, false),
		DEBUG_LINE_STRIP(2, 1, true),
		TRIANGLES(3, 3, false),
		TRIANGLE_STRIP(3, 1, true),
		TRIANGLE_FAN(3, 1, true),
		QUADS(4, 4, false);

		public final int primitiveLength;
		public final int primitiveStride;
		public final boolean connectedPrimitives;

		private Mode(final int j, final int k, final boolean bl) {
			this.primitiveLength = j;
			this.primitiveStride = k;
			this.connectedPrimitives = bl;
		}

		public int indexCount(int i) {
			return switch (this) {
				case LINES, QUADS -> i / 4 * 6;
				case LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> i;
				default -> 0;
			};
		}
	}
}
