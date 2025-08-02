package net.minecraft.client.model.geom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.render.immediate.model.EntityRenderer;
import net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.caffeinemc.mods.sodium.client.render.vertex.VertexConsumerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public final class ModelPart {
	public static final float DEFAULT_SCALE = 1.0F;
	public float x;
	public float y;
	public float z;
	public float xRot;
	public float yRot;
	public float zRot;
	public float xScale = 1.0F;
	public float yScale = 1.0F;
	public float zScale = 1.0F;
	public boolean visible = true;
	public boolean skipDraw;
	private final List<ModelPart.Cube> cubes;
	private final Map<String, ModelPart> children;
	private PartPose initialPose = PartPose.ZERO;

	public ModelPart(List<ModelPart.Cube> list, Map<String, ModelPart> map) {
		this.cubes = list;
		this.children = map;
	}

	public PartPose storePose() {
		return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
	}

	public PartPose getInitialPose() {
		return this.initialPose;
	}

	public void setInitialPose(PartPose partPose) {
		this.initialPose = partPose;
	}

	public void resetPose() {
		this.loadPose(this.initialPose);
	}

	public void loadPose(PartPose partPose) {
		this.x = partPose.x();
		this.y = partPose.y();
		this.z = partPose.z();
		this.xRot = partPose.xRot();
		this.yRot = partPose.yRot();
		this.zRot = partPose.zRot();
		this.xScale = partPose.xScale();
		this.yScale = partPose.yScale();
		this.zScale = partPose.zScale();
	}

	public void copyFrom(ModelPart modelPart) {
		this.xScale = modelPart.xScale;
		this.yScale = modelPart.yScale;
		this.zScale = modelPart.zScale;
		this.xRot = modelPart.xRot;
		this.yRot = modelPart.yRot;
		this.zRot = modelPart.zRot;
		this.x = modelPart.x;
		this.y = modelPart.y;
		this.z = modelPart.z;
	}

	public boolean hasChild(String string) {
		return this.children.containsKey(string);
	}

	public ModelPart getChild(String string) {
		ModelPart modelPart = (ModelPart)this.children.get(string);
		if (modelPart == null) {
			throw new NoSuchElementException("Can't find part " + string);
		} else {
			return modelPart;
		}
	}

	public void setPos(float f, float g, float h) {
		this.x = f;
		this.y = g;
		this.z = h;
	}

	public void setRotation(float f, float g, float h) {
		this.xRot = f;
		this.yRot = g;
		this.zRot = h;
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
		this.render(poseStack, vertexConsumer, i, j, -1);
	}

	public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
		if (this.visible) {
			if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
				poseStack.pushPose();
				this.translateAndRotate(poseStack);
				if (!this.skipDraw) {
					this.compile(poseStack.last(), vertexConsumer, i, j, k);
				}

				for (ModelPart modelPart : this.children.values()) {
					modelPart.render(poseStack, vertexConsumer, i, j, k);
				}

				poseStack.popPose();
			}
		}
	}

	public void rotateBy(Quaternionf quaternionf) {
		Matrix3f matrix3f = new Matrix3f().rotationZYX(this.zRot, this.yRot, this.xRot);
		Matrix3f matrix3f2 = matrix3f.rotate(quaternionf);
		Vector3f vector3f = matrix3f2.getEulerAnglesZYX(new Vector3f());
		this.setRotation(vector3f.x, vector3f.y, vector3f.z);
	}

	public void getExtentsForGui(PoseStack poseStack, Set<Vector3f> set) {
		this.visit(poseStack, (pose, string, i, cube) -> {
			for (ModelPart.Polygon polygon : cube.polygons) {
				for (ModelPart.Vertex vertex : polygon.vertices()) {
					float f = vertex.pos().x() / 16.0F;
					float g = vertex.pos().y() / 16.0F;
					float h = vertex.pos().z() / 16.0F;
					Vector3f vector3f = pose.pose().transformPosition(f, g, h, new Vector3f());
					set.add(vector3f);
				}
			}
		});
	}

	public void visit(PoseStack poseStack, ModelPart.Visitor visitor) {
		this.visit(poseStack, visitor, "");
	}

	private void visit(PoseStack poseStack, ModelPart.Visitor visitor, String string) {
		if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
			poseStack.pushPose();
			this.translateAndRotate(poseStack);
			PoseStack.Pose pose = poseStack.last();

			for (int i = 0; i < this.cubes.size(); i++) {
				visitor.visit(pose, string, i, (ModelPart.Cube)this.cubes.get(i));
			}

			String string2 = string + "/";
			this.children.forEach((string2x, modelPart) -> modelPart.visit(poseStack, visitor, string2 + string2x));
			poseStack.popPose();
		}
	}

	public void translateAndRotate(PoseStack matrixStack) {

		if (this.x != 0.0F || this.y != 0.0F || this.z != 0.0F) {
			matrixStack.translate(this.x * (1.0f / 16.0f), this.y * (1.0f / 16.0f), this.z * (1.0f / 16.0f));
		}

		if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
			MatrixHelper.rotateZYX(matrixStack.last(), this.zRot, this.yRot, this.xRot);
		}

		if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
			matrixStack.scale(this.xScale, this.yScale, this.zScale);
		}

		/*poseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
		if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
			poseStack.mulPose(new Quaternionf().rotationZYX(this.zRot, this.yRot, this.xRot));
		}

		if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
			poseStack.scale(this.xScale, this.yScale, this.zScale);
		}*/
	}

	private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, int k) {
		for (ModelPart.Cube cube : this.cubes) {
			cube.compile(pose, vertexConsumer, i, j, k);
		}
	}

	public ModelPart.Cube getRandomCube(RandomSource randomSource) {
		return (ModelPart.Cube)this.cubes.get(randomSource.nextInt(this.cubes.size()));
	}

	public boolean isEmpty() {
		return this.cubes.isEmpty();
	}

	public void offsetPos(Vector3f vector3f) {
		this.x = this.x + vector3f.x();
		this.y = this.y + vector3f.y();
		this.z = this.z + vector3f.z();
	}

	public void offsetRotation(Vector3f vector3f) {
		this.xRot = this.xRot + vector3f.x();
		this.yRot = this.yRot + vector3f.y();
		this.zRot = this.zRot + vector3f.z();
	}

	public void offsetScale(Vector3f vector3f) {
		this.xScale = this.xScale + vector3f.x();
		this.yScale = this.yScale + vector3f.y();
		this.zScale = this.zScale + vector3f.z();
	}

	public List<ModelPart> getAllParts() {
		List<ModelPart> list = new ArrayList();
		list.add(this);
		this.addAllChildren((string, modelPart) -> list.add(modelPart));
		return List.copyOf(list);
	}

	public Function<String, ModelPart> createPartLookup() {
		Map<String, ModelPart> map = new HashMap();
		map.put("root", this);
		this.addAllChildren(map::putIfAbsent);
		return map::get;
	}

	private void addAllChildren(BiConsumer<String, ModelPart> biConsumer) {
		for (Entry<String, ModelPart> entry : this.children.entrySet()) {
			biConsumer.accept((String)entry.getKey(), (ModelPart)entry.getValue());
		}

		for (ModelPart modelPart : this.children.values()) {
			modelPart.addAllChildren(biConsumer);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Cube {
		public final ModelPart.Polygon[] polygons;
		public final float minX;
		public final float minY;
		public final float minZ;
		public final float maxX;
		public final float maxY;
		public final float maxZ;
		private ModelCuboid sodium$cuboid;

		public Cube(int i, int j, float f, float g, float h, float k, float l, float m, float n, float o, float p, boolean bl, float q, float r, Set<Direction> set) {
			this.minX = f;
			this.sodium$cuboid = new ModelCuboid(i, j, f, g, h, k, l, m, n, o, p, bl, q, r, set);
			this.minY = g;
			this.minZ = h;
			this.maxX = f + k;
			this.maxY = g + l;
			this.maxZ = h + m;
			this.polygons = new ModelPart.Polygon[set.size()];
			float s = f + k;
			float t = g + l;
			float u = h + m;
			f -= n;
			g -= o;
			h -= p;
			s += n;
			t += o;
			u += p;
			if (bl) {
				float v = s;
				s = f;
				f = v;
			}

			ModelPart.Vertex vertex = new ModelPart.Vertex(f, g, h, 0.0F, 0.0F);
			ModelPart.Vertex vertex2 = new ModelPart.Vertex(s, g, h, 0.0F, 8.0F);
			ModelPart.Vertex vertex3 = new ModelPart.Vertex(s, t, h, 8.0F, 8.0F);
			ModelPart.Vertex vertex4 = new ModelPart.Vertex(f, t, h, 8.0F, 0.0F);
			ModelPart.Vertex vertex5 = new ModelPart.Vertex(f, g, u, 0.0F, 0.0F);
			ModelPart.Vertex vertex6 = new ModelPart.Vertex(s, g, u, 0.0F, 8.0F);
			ModelPart.Vertex vertex7 = new ModelPart.Vertex(s, t, u, 8.0F, 8.0F);
			ModelPart.Vertex vertex8 = new ModelPart.Vertex(f, t, u, 8.0F, 0.0F);
			float w = i;
			float x = i + m;
			float y = i + m + k;
			float z = i + m + k + k;
			float aa = i + m + k + m;
			float ab = i + m + k + m + k;
			float ac = j;
			float ad = j + m;
			float ae = j + m + l;
			int af = 0;
			if (set.contains(Direction.DOWN)) {
				this.polygons[af++] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex6, vertex5, vertex, vertex2}, x, ac, y, ad, q, r, bl, Direction.DOWN);
			}

			if (set.contains(Direction.UP)) {
				this.polygons[af++] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex3, vertex4, vertex8, vertex7}, y, ad, z, ac, q, r, bl, Direction.UP);
			}

			if (set.contains(Direction.WEST)) {
				this.polygons[af++] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex, vertex5, vertex8, vertex4}, w, ad, x, ae, q, r, bl, Direction.WEST);
			}

			if (set.contains(Direction.NORTH)) {
				this.polygons[af++] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex2, vertex, vertex4, vertex3}, x, ad, y, ae, q, r, bl, Direction.NORTH);
			}

			if (set.contains(Direction.EAST)) {
				this.polygons[af++] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex6, vertex2, vertex3, vertex7}, y, ad, aa, ae, q, r, bl, Direction.EAST);
			}

			if (set.contains(Direction.SOUTH)) {
				this.polygons[af] = new ModelPart.Polygon(new ModelPart.Vertex[]{vertex5, vertex6, vertex7, vertex8}, aa, ad, ab, ae, q, r, bl, Direction.SOUTH);
			}
		}

		public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, int k) {

			VertexBufferWriter writer = VertexConsumerUtils.convertOrLog(vertexConsumer);
			if (writer != null) {
				EntityRenderer.renderCuboid(pose, writer, this.sodium$cuboid, i, j, ColorARGB.toABGR(k));
				return;
			}

			Matrix4f matrix4f = pose.pose();
			Vector3f vector3f = new Vector3f();

			for (ModelPart.Polygon polygon : this.polygons) {
				Vector3f vector3f2 = pose.transformNormal(polygon.normal, vector3f);
				float f = vector3f2.x();
				float g = vector3f2.y();
				float h = vector3f2.z();

				for (ModelPart.Vertex vertex : polygon.vertices) {
					float l = vertex.pos.x() / 16.0F;
					float m = vertex.pos.y() / 16.0F;
					float n = vertex.pos.z() / 16.0F;
					Vector3f vector3f3 = matrix4f.transformPosition(l, m, n, vector3f);
					vertexConsumer.addVertex(vector3f3.x(), vector3f3.y(), vector3f3.z(), k, vertex.u, vertex.v, j, i, f, g, h);
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public record Polygon(ModelPart.Vertex[] vertices, Vector3f normal) {

		public Polygon(ModelPart.Vertex[] vertexs, float f, float g, float h, float i, float j, float k, boolean bl, Direction direction) {
			this(vertexs, direction.step());
			float l = 0.0F / j;
			float m = 0.0F / k;
			vertexs[0] = vertexs[0].remap(h / j - l, g / k + m);
			vertexs[1] = vertexs[1].remap(f / j + l, g / k + m);
			vertexs[2] = vertexs[2].remap(f / j + l, i / k - m);
			vertexs[3] = vertexs[3].remap(h / j - l, i / k - m);
			if (bl) {
				int n = vertexs.length;

				for (int o = 0; o < n / 2; o++) {
					ModelPart.Vertex vertex = vertexs[o];
					vertexs[o] = vertexs[n - 1 - o];
					vertexs[n - 1 - o] = vertex;
				}
			}

			if (bl) {
				this.normal.mul(-1.0F, 1.0F, 1.0F);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public record Vertex(Vector3f pos, float u, float v) {

		public Vertex(float f, float g, float h, float i, float j) {
			this(new Vector3f(f, g, h), i, j);
		}

		public ModelPart.Vertex remap(float f, float g) {
			return new ModelPart.Vertex(this.pos, f, g);
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Visitor {
		void visit(PoseStack.Pose pose, String string, int i, ModelPart.Cube cube);
	}
}
