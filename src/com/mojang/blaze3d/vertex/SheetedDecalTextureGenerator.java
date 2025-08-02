package com.mojang.blaze3d.vertex;

import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class SheetedDecalTextureGenerator implements VertexConsumer, VertexBufferWriter {
	private final VertexConsumer delegate;
	private final Matrix4f cameraInversePose;
	private final Matrix3f normalInversePose;
	private final float textureScale;
	private final Vector3f worldPos = new Vector3f();
	private final Vector3f normal = new Vector3f();
	private float x;
	private float y;
	private float z;
	private final boolean canUseIntrinsics;

	public SheetedDecalTextureGenerator(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f) {
		this.delegate = vertexConsumer;
		this.cameraInversePose = new Matrix4f(pose.pose()).invert();
		this.normalInversePose = new Matrix3f(pose.normal()).invert();
		this.textureScale = f;
		this.canUseIntrinsics = VertexBufferWriter.tryOf(this.delegate) != null;
	}

	@Override
	public boolean canUseIntrinsics() {
		return this.canUseIntrinsics;
	}


	@Override
	public VertexConsumer addVertex(float f, float g, float h) {
		this.x = f;
		this.y = g;
		this.z = h;
		this.delegate.addVertex(f, g, h);
		return this;
	}

	@Override
	public VertexConsumer setColor(int i, int j, int k, int l) {
		this.delegate.setColor(-1);
		return this;
	}

	@Override
	public VertexConsumer setUv(float f, float g) {
		return this;
	}

	@Override
	public VertexConsumer setUv1(int i, int j) {
		this.delegate.setUv1(i, j);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int i, int j) {
		this.delegate.setUv2(i, j);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float f, float g, float h) {
		this.delegate.setNormal(f, g, h);
		Vector3f vector3f = this.normalInversePose.transform(f, g, h, this.normal);
		Direction direction = Direction.getApproximateNearest(vector3f.x(), vector3f.y(), vector3f.z());
		Vector3f vector3f2 = this.cameraInversePose.transformPosition(this.x, this.y, this.z, this.worldPos);
		vector3f2.rotateY((float) Math.PI);
		vector3f2.rotateX((float) (-Math.PI / 2));
		vector3f2.rotate(direction.getRotation());
		this.delegate.setUv(-vector3f2.x() * this.textureScale, -vector3f2.y() * this.textureScale);
		return this;
	}

	@Override
	public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
		transform(ptr, count, format, this.normalInversePose, this.cameraInversePose, this.textureScale);
		VertexBufferWriter.of(this.delegate).push(stack, ptr, count, format);
	}

	private static void transform(long ptr, int count, VertexFormat format, Matrix3f inverseNormalMatrix, Matrix4f inverseTextureMatrix, float textureScale) {
		long stride = format.getVertexSize();
		int offsetPosition = format.getOffset(VertexFormatElement.POSITION);
		int offsetColor = format.getOffset(VertexFormatElement.COLOR);
		int offsetNormal = format.getOffset(VertexFormatElement.NORMAL);
		int offsetTexture = format.getOffset(VertexFormatElement.UV0);
		int color = ColorABGR.pack(1.0F, 1.0F, 1.0F, 1.0F);
		Vector3f normal = new Vector3f(Float.NaN);
		Vector4f position = new Vector4f(Float.NaN);

		for (int vertexIndex = 0; vertexIndex < count; vertexIndex++) {
			position.x = MemoryUtil.memGetFloat(ptr + offsetPosition);
			position.y = MemoryUtil.memGetFloat(ptr + offsetPosition + 4L);
			position.z = MemoryUtil.memGetFloat(ptr + offsetPosition + 8L);
			position.w = 1.0F;
			int packedNormal = MemoryUtil.memGetInt(ptr + offsetNormal);
			normal.x = NormI8.unpackX(packedNormal);
			normal.y = NormI8.unpackY(packedNormal);
			normal.z = NormI8.unpackZ(packedNormal);
			Vector3f transformedNormal = inverseNormalMatrix.transform(normal);
			Direction direction = Direction.getApproximateNearest(transformedNormal.x(), transformedNormal.y(), transformedNormal.z());
			Vector4f transformedTexture = inverseTextureMatrix.transform(position);
			transformedTexture.rotateY((float) Math.PI);
			transformedTexture.rotateX((float) (-Math.PI / 2));
			transformedTexture.rotate(direction.getRotation());
			float textureU = -transformedTexture.x() * textureScale;
			float textureV = -transformedTexture.y() * textureScale;
			ColorAttribute.set(ptr + offsetColor, color);
			TextureAttribute.put(ptr + offsetTexture, textureU, textureV);
			ptr += stride;
		}
	}

}
