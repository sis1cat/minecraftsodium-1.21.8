/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.renderer.v1.model;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.minecraft.core.Direction;
import net.minecraft.client.resources.model.ModelState;
import com.mojang.math.Transformation;
import net.minecraft.core.BlockMath;
import com.mojang.math.MatrixUtil;

public final class ModelBakeSettingsHelper {
	private static final Direction[] DIRECTIONS = Direction.values();

	private ModelBakeSettingsHelper() {
	}

	public static ModelState of(Transformation transformation, boolean uvLock) {
		Matrix4fc matrix = transformation.getMatrix();

		if (MatrixUtil.isIdentity(matrix)) {
			return BlockModelRotation.X0_Y0;
		}

		if (!uvLock) {
			return new ModelState() {
				@Override
				public Transformation transformation() {
					return transformation;
				}
			};
		}

		Map<Direction, Matrix4fc> faceTransformations = new EnumMap<>(Direction.class);
		Map<Direction, Matrix4fc> inverseFaceTransformations = new EnumMap<>(Direction.class);

		for (Direction face : DIRECTIONS) {
			Matrix4fc faceTransformation = BlockMath.getFaceTransformation(transformation, face).getMatrix();
			faceTransformations.put(face, faceTransformation);
			inverseFaceTransformations.put(face, faceTransformation.invert(new Matrix4f()));
		}

		return new ModelState() {
			@Override
			public Transformation transformation() {
				return transformation;
			}

			@Override
			public Matrix4fc faceTransformation(Direction face) {
				return faceTransformations.get(face);
			}

			@Override
			public Matrix4fc inverseFaceTransformation(Direction face) {
				return inverseFaceTransformations.get(face);
			}
		};
	}


	public static ModelState multiply(ModelState left, ModelState right) {
		// Assumes face transformations are identity if main transformation is identity
		if (MatrixUtil.isIdentity(left.transformation().getMatrix())) {
			return right;
		} else if (MatrixUtil.isIdentity(right.transformation().getMatrix())) {
			return left;
		}

		Transformation transformation = left.transformation().compose(right.transformation());

		boolean leftHasFaceTransformations = false;
		boolean rightHasFaceTransformations = false;

		// Assumes inverse face transformations are exactly inverse of regular face transformations
		for (Direction face : DIRECTIONS) {
			if (!leftHasFaceTransformations && !MatrixUtil.isIdentity(left.faceTransformation(face))) {
				leftHasFaceTransformations = true;
			}

			if (!rightHasFaceTransformations && !MatrixUtil.isIdentity(right.faceTransformation(face))) {
				rightHasFaceTransformations = true;
			}
		}

		if (leftHasFaceTransformations & rightHasFaceTransformations) {
			Map<Direction, Matrix4fc> faceTransformations = new EnumMap<>(Direction.class);
			Map<Direction, Matrix4fc> inverseFaceTransformations = new EnumMap<>(Direction.class);

			for (Direction face : DIRECTIONS) {
				faceTransformations.put(face, left.faceTransformation(face).mul(right.faceTransformation(face), new Matrix4f()));
				inverseFaceTransformations.put(face, right.inverseFaceTransformation(face).mul(left.inverseFaceTransformation(face), new Matrix4f()));
			}

			return new ModelState() {
				@Override
				public Transformation transformation () {
					return transformation;
				}

				@Override
				public Matrix4fc faceTransformation(Direction face) {
					return faceTransformations.get(face);
				}

				@Override
				public Matrix4fc inverseFaceTransformation(Direction face) {
					return inverseFaceTransformations.get(face);
				}
			};
		}

		ModelState faceTransformDelegate = leftHasFaceTransformations ? left : right;

		return new ModelState() {
			@Override
			public Transformation transformation () {
				return transformation;
			}

			@Override
			public Matrix4fc faceTransformation(Direction face) {
				return faceTransformDelegate.faceTransformation(face);
			}

			@Override
			public Matrix4fc inverseFaceTransformation(Direction face) {
				return faceTransformDelegate.inverseFaceTransformation(face);
			}
		};
	}


	public static QuadTransform asQuadTransform(ModelState settings, SpriteFinder spriteFinder) {
		Matrix4fc matrix = settings.transformation().getMatrix();

		// Assumes face transformations are identity if main transformation is identity
		if (MatrixUtil.isIdentity(matrix)) {
			return q -> true;
		}

		Matrix3f normalMatrix = matrix.normal(new Matrix3f());

		Vector4f vec4 = new Vector4f();
		Vector3f vec3 = new Vector3f();

		return quad -> {
			Direction lightFace = quad.lightFace();
			Matrix4fc reverseMatrix = settings.inverseFaceTransformation(lightFace);

			if (!MatrixUtil.isIdentity(reverseMatrix)) {
				TextureAtlasSprite sprite = spriteFinder.find(quad);

				for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
					float frameU = sprite.getUOffset(quad.u(vertexIndex));
					float frameV = sprite.getVOffset(quad.v(vertexIndex));
					vec3.set(frameU - 0.5f, frameV - 0.5f, 0.0f);
					reverseMatrix.transformPosition(vec3);
					frameU = vec3.x + 0.5f;
					frameV = vec3.y + 0.5f;
					quad.uv(vertexIndex, sprite.getU(frameU), sprite.getV(frameV));
				}
			}

			for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
				vec4.set(quad.x(vertexIndex) - 0.5f, quad.y(vertexIndex) - 0.5f, quad.z(vertexIndex) - 0.5f, 1.0f);
				vec4.mul(matrix);
				quad.pos(vertexIndex, vec4.x + 0.5f, vec4.y + 0.5f, vec4.z + 0.5f);

				if (quad.hasNormal(vertexIndex)) {
					quad.copyNormal(vertexIndex, vec3);
					vec3.mul(normalMatrix);
					vec3.normalize();
					quad.normal(vertexIndex, vec3);
				}
			}

			Direction cullFace = quad.cullFace();

			if (cullFace != null) {
				quad.cullFace(Direction.rotate(matrix, cullFace));
			}

			return true;
		};
	}
}
