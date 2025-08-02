package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.LineVertex;
import net.caffeinemc.mods.sodium.client.render.vertex.VertexConsumerUtils;
import net.caffeinemc.mods.sodium.client.render.vertex.buffer.BufferBuilderExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public class ShapeRenderer {
	public static void renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, int i) {
		PoseStack.Pose pose = poseStack.last();
		voxelShape.forAllEdges((g, h, j, k, l, m) -> {
			Vector3f vector3f = new Vector3f((float)(k - g), (float)(l - h), (float)(m - j)).normalize();
			vertexConsumer.addVertex(pose, (float)(g + d), (float)(h + e), (float)(j + f)).setColor(i).setNormal(pose, vector3f);
			vertexConsumer.addVertex(pose, (float)(k + d), (float)(l + e), (float)(m + f)).setColor(i).setNormal(pose, vector3f);
		});
	}

	public static void renderLineBox(PoseStack poseStack, VertexConsumer vertexConsumer, AABB aABB, float f, float g, float h, float i) {
		renderLineBox(poseStack, vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i, f, g, h);
	}

	public static void renderLineBox(
		PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		renderLineBox(poseStack, vertexConsumer, d, e, f, g, h, i, j, k, l, m, j, k, l);
	}

	public static void renderLineBox(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		float j,
		float k,
		float l,
		float m,
		float n,
		float o,
		float p
	) {

		VertexBufferWriter writer = VertexConsumerUtils.convertOrLog(vertexConsumer);
		if (writer != null) {
			Matrix4f position = poseStack.last().pose();
			Matrix3f normal = poseStack.last().normal();
			float x1f = (float)d;
			float y1f = (float)e;
			float z1f = (float)f;
			float x2f = (float)g;
			float y2f = (float)h;
			float z2f = (float)i;
			int color = ColorABGR.pack(j, k, l, m);
			float v1x = Math.fma(position.m00(), x1f, Math.fma(position.m10(), y1f, Math.fma(position.m20(), z1f, position.m30())));
			float v1y = Math.fma(position.m01(), x1f, Math.fma(position.m11(), y1f, Math.fma(position.m21(), z1f, position.m31())));
			float v1z = Math.fma(position.m02(), x1f, Math.fma(position.m12(), y1f, Math.fma(position.m22(), z1f, position.m32())));
			float v2x = Math.fma(position.m00(), x2f, Math.fma(position.m10(), y1f, Math.fma(position.m20(), z1f, position.m30())));
			float v2y = Math.fma(position.m01(), x2f, Math.fma(position.m11(), y1f, Math.fma(position.m21(), z1f, position.m31())));
			float v2z = Math.fma(position.m02(), x2f, Math.fma(position.m12(), y1f, Math.fma(position.m22(), z1f, position.m32())));
			float v3x = Math.fma(position.m00(), x1f, Math.fma(position.m10(), y2f, Math.fma(position.m20(), z1f, position.m30())));
			float v3y = Math.fma(position.m01(), x1f, Math.fma(position.m11(), y2f, Math.fma(position.m21(), z1f, position.m31())));
			float v3z = Math.fma(position.m02(), x1f, Math.fma(position.m12(), y2f, Math.fma(position.m22(), z1f, position.m32())));
			float v4x = Math.fma(position.m00(), x1f, Math.fma(position.m10(), y1f, Math.fma(position.m20(), z2f, position.m30())));
			float v4y = Math.fma(position.m01(), x1f, Math.fma(position.m11(), y1f, Math.fma(position.m21(), z2f, position.m31())));
			float v4z = Math.fma(position.m02(), x1f, Math.fma(position.m12(), y1f, Math.fma(position.m22(), z2f, position.m32())));
			float v5x = Math.fma(position.m00(), x2f, Math.fma(position.m10(), y2f, Math.fma(position.m20(), z1f, position.m30())));
			float v5y = Math.fma(position.m01(), x2f, Math.fma(position.m11(), y2f, Math.fma(position.m21(), z1f, position.m31())));
			float v5z = Math.fma(position.m02(), x2f, Math.fma(position.m12(), y2f, Math.fma(position.m22(), z1f, position.m32())));
			float v6x = Math.fma(position.m00(), x1f, Math.fma(position.m10(), y2f, Math.fma(position.m20(), z2f, position.m30())));
			float v6y = Math.fma(position.m01(), x1f, Math.fma(position.m11(), y2f, Math.fma(position.m21(), z2f, position.m31())));
			float v6z = Math.fma(position.m02(), x1f, Math.fma(position.m12(), y2f, Math.fma(position.m22(), z2f, position.m32())));
			float v7x = Math.fma(position.m00(), x2f, Math.fma(position.m10(), y1f, Math.fma(position.m20(), z2f, position.m30())));
			float v7y = Math.fma(position.m01(), x2f, Math.fma(position.m11(), y1f, Math.fma(position.m21(), z2f, position.m31())));
			float v7z = Math.fma(position.m02(), x2f, Math.fma(position.m12(), y1f, Math.fma(position.m22(), z2f, position.m32())));
			float v8x = Math.fma(position.m00(), x2f, Math.fma(position.m10(), y2f, Math.fma(position.m20(), z2f, position.m30())));
			float v8y = Math.fma(position.m01(), x2f, Math.fma(position.m11(), y2f, Math.fma(position.m21(), z2f, position.m31())));
			float v8z = Math.fma(position.m02(), x2f, Math.fma(position.m12(), y2f, Math.fma(position.m22(), z2f, position.m32())));
			if (vertexConsumer instanceof BufferBuilderExtension ext) {
				ext.sodium$duplicateVertex();
			}

			writeLineVertices(writer, v1x, v1y, v1z, ColorABGR.pack(j, o, p, m), NormI8.pack(normal.m00(), normal.m01(), normal.m02()));
			writeLineVertices(writer, v2x, v2y, v2z, ColorABGR.pack(j, o, p, m), NormI8.pack(normal.m00(), normal.m01(), normal.m02()));
			writeLineVertices(writer, v1x, v1y, v1z, ColorABGR.pack(n, k, p, m), NormI8.pack(normal.m10(), normal.m11(), normal.m12()));
			writeLineVertices(writer, v3x, v3y, v3z, ColorABGR.pack(n, k, p, m), NormI8.pack(normal.m10(), normal.m11(), normal.m12()));
			writeLineVertices(writer, v1x, v1y, v1z, ColorABGR.pack(n, o, l, m), NormI8.pack(normal.m20(), normal.m21(), normal.m22()));
			writeLineVertices(writer, v4x, v4y, v4z, ColorABGR.pack(n, o, l, m), NormI8.pack(normal.m20(), normal.m21(), normal.m22()));
			writeLineVertices(writer, v2x, v2y, v2z, color, NormI8.pack(normal.m10(), normal.m11(), normal.m12()));
			writeLineVertices(writer, v5x, v5y, v5z, color, NormI8.pack(normal.m10(), normal.m11(), normal.m12()));
			writeLineVertices(writer, v5x, v5y, v5z, color, NormI8.pack(-normal.m00(), -normal.m01(), -normal.m02()));
			writeLineVertices(writer, v3x, v3y, v3z, color, NormI8.pack(-normal.m00(), -normal.m01(), -normal.m02()));
			writeLineVertices(writer, v3x, v3y, v3z, color, NormI8.pack(normal.m20(), normal.m21(), normal.m22()));
			writeLineVertices(writer, v6x, v6y, v6z, color, NormI8.pack(normal.m20(), normal.m21(), normal.m22()));
			writeLineVertices(writer, v6x, v6y, v6z, color, NormI8.pack(-normal.m10(), -normal.m11(), -normal.m12()));
			writeLineVertices(writer, v4x, v4y, v4z, color, NormI8.pack(-normal.m10(), -normal.m11(), -normal.m12()));
			writeLineVertices(writer, v4x, v4y, v4z, color, NormI8.pack(normal.m00(), normal.m01(), normal.m02()));
			writeLineVertices(writer, v7x, v7y, v7z, color, NormI8.pack(normal.m00(), normal.m01(), normal.m02()));
			writeLineVertices(writer, v7x, v7y, v7z, color, NormI8.pack(-normal.m20(), -normal.m21(), -normal.m22()));
			writeLineVertices(writer, v2x, v2y, v2z, color, NormI8.pack(-normal.m20(), -normal.m21(), -normal.m22()));
			writeLineVertices(writer, v6x, v6y, v6z, color, NormI8.pack(normal.m00(), normal.m01(), normal.m02()));
			writeLineVertices(writer, v8x, v8y, v8z, color, NormI8.pack(normal.m00(), normal.m01(), normal.m02()));
			writeLineVertices(writer, v7x, v7y, v7z, color, NormI8.pack(normal.m10(), normal.m11(), normal.m12()));
			writeLineVertices(writer, v8x, v8y, v8z, color, NormI8.pack(normal.m10(), normal.m11(), normal.m12()));
			writeLineVertices(writer, v5x, v5y, v5z, color, NormI8.pack(normal.m20(), normal.m21(), normal.m22()));
			writeLineVertex(writer, v8x, v8y, v8z, color, NormI8.pack(normal.m20(), normal.m21(), normal.m22()));
			return;
		}

		PoseStack.Pose pose = poseStack.last();
		float q = (float)d;
		float r = (float)e;
		float s = (float)f;
		float t = (float)g;
		float u = (float)h;
		float v = (float)i;
		vertexConsumer.addVertex(pose, q, r, s).setColor(j, o, p, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, r, s).setColor(j, o, p, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, r, s).setColor(n, k, p, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, u, s).setColor(n, k, p, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, r, s).setColor(n, o, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, q, r, v).setColor(n, o, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, t, r, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, s).setColor(j, k, l, m).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, u, s).setColor(j, k, l, m).setNormal(pose, -1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, u, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, q, u, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, q, u, v).setColor(j, k, l, m).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, r, v).setColor(j, k, l, m).setNormal(pose, 0.0F, -1.0F, 0.0F);
		vertexConsumer.addVertex(pose, q, r, v).setColor(j, k, l, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, r, v).setColor(j, k, l, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, r, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexConsumer.addVertex(pose, t, r, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, -1.0F);
		vertexConsumer.addVertex(pose, q, u, v).setColor(j, k, l, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, v).setColor(j, k, l, m).setNormal(pose, 1.0F, 0.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, r, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 1.0F, 0.0F);
		vertexConsumer.addVertex(pose, t, u, s).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
		vertexConsumer.addVertex(pose, t, u, v).setColor(j, k, l, m).setNormal(pose, 0.0F, 0.0F, 1.0F);
	}

	private static void writeLineVertices(VertexBufferWriter writer, float x, float y, float z, int color, int normal) {
		MemoryStack stack = MemoryStack.stackPush();

		try {
			long buffer = stack.nmalloc(40);
			long ptr = buffer;

			for (int i = 0; i < 2; i++) {
				LineVertex.put(ptr, x, y, z, color, normal);
				ptr += 20L;
			}

			writer.push(stack, buffer, 2, LineVertex.FORMAT);
		} catch (Throwable var13) {
			if (stack != null) {
				try {
					stack.close();
				} catch (Throwable var12) {
					var13.addSuppressed(var12);
				}
			}

			throw var13;
		}

		if (stack != null) {
			stack.close();
		}
	}

	private static void writeLineVertex(VertexBufferWriter writer, float x, float y, float z, int color, int normal) {
		MemoryStack stack = MemoryStack.stackPush();

		try {
			long buffer = stack.nmalloc(20);
			LineVertex.put(buffer, x, y, z, color, normal);
			writer.push(stack, buffer, 1, LineVertex.FORMAT);
		} catch (Throwable var10) {
			if (stack != null) {
				try {
					stack.close();
				} catch (Throwable var9) {
					var10.addSuppressed(var9);
				}
			}

			throw var10;
		}

		if (stack != null) {
			stack.close();
		}
	}

	public static void addChainedFilledBoxVertices(
		PoseStack poseStack, VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m
	) {
		addChainedFilledBoxVertices(poseStack, vertexConsumer, (float)d, (float)e, (float)f, (float)g, (float)h, (float)i, j, k, l, m);
	}

	public static void addChainedFilledBoxVertices(
		PoseStack poseStack, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, float n, float o
	) {
		Matrix4f matrix4f = poseStack.last().pose();
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
		vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
	}

	public static void renderFace(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		Direction direction,
		float f,
		float g,
		float h,
		float i,
		float j,
		float k,
		float l,
		float m,
		float n,
		float o
	) {
		Matrix4f matrix4f = poseStack.last().pose();
		switch (direction) {
			case DOWN:
				vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
				break;
			case UP:
				vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
				break;
			case NORTH:
				vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
				break;
			case SOUTH:
				vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
				break;
			case WEST:
				vertexConsumer.addVertex(matrix4f, f, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, g, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, f, j, h).setColor(l, m, n, o);
				break;
			case EAST:
				vertexConsumer.addVertex(matrix4f, i, g, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, h).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, j, k).setColor(l, m, n, o);
				vertexConsumer.addVertex(matrix4f, i, g, k).setColor(l, m, n, o);
		}
	}

	public static void renderVector(PoseStack poseStack, VertexConsumer vertexConsumer, Vector3f vector3f, Vec3 vec3, int i) {
		PoseStack.Pose pose = poseStack.last();
		vertexConsumer.addVertex(pose, vector3f).setColor(i).setNormal(pose, (float)vec3.x, (float)vec3.y, (float)vec3.z);
		vertexConsumer.addVertex(pose, (float)(vector3f.x() + vec3.x), (float)(vector3f.y() + vec3.y), (float)(vector3f.z() + vec3.z))
			.setColor(i)
			.setNormal(pose, (float)vec3.x, (float)vec3.y, (float)vec3.z);
	}
}
