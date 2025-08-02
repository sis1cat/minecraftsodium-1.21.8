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

package net.fabricmc.fabric.api.renderer.v1.mesh;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.random.WeightedList.Flat;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.model.BakedQuad;


public interface QuadEmitter extends MutableQuadView {
	@Override
	QuadEmitter pos(int vertexIndex, float x, float y, float z);

	@Override
	default QuadEmitter pos(int vertexIndex, Vector3f pos) {
		MutableQuadView.super.pos(vertexIndex, pos);
		return this;
	}

	@Override
	default QuadEmitter pos(int vertexIndex, Vector3fc pos) {
		MutableQuadView.super.pos(vertexIndex, pos);
		return this;
	}

	@Override
	QuadEmitter color(int vertexIndex, int color);

	@Override
	default QuadEmitter color(int c0, int c1, int c2, int c3) {
		MutableQuadView.super.color(c0, c1, c2, c3);
		return this;
	}

	@Override
	QuadEmitter uv(int vertexIndex, float u, float v);

	@Override
	default QuadEmitter uv(int vertexIndex, Vector2f uv) {
		MutableQuadView.super.uv(vertexIndex, uv);
		return this;
	}

	@Override
	default QuadEmitter uv(int vertexIndex, Vector2fc uv) {
		MutableQuadView.super.uv(vertexIndex, uv);
		return this;
	}

	@Override
	default QuadEmitter spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
		MutableQuadView.super.spriteBake(sprite, bakeFlags);
		return this;
	}

	default QuadEmitter uvUnitSquare() {
		uv(0, 0, 0);
		uv(1, 0, 1);
		uv(2, 1, 1);
		uv(3, 1, 0);
		return this;
	}

	@Override
	QuadEmitter lightmap(int vertexIndex, int lightmap);

	@Override
	default QuadEmitter lightmap(int l0, int l1, int l2, int l3) {
		MutableQuadView.super.lightmap(l0, l1, l2, l3);
		return this;
	}

	@Override
	QuadEmitter normal(int vertexIndex, float x, float y, float z);

	@Override
	default QuadEmitter normal(int vertexIndex, Vector3f normal) {
		MutableQuadView.super.normal(vertexIndex, normal);
		return this;
	}

	@Override
	default QuadEmitter normal(int vertexIndex, Vector3fc normal) {
		MutableQuadView.super.normal(vertexIndex, normal);
		return this;
	}

	@Override
	QuadEmitter nominalFace(@Nullable Direction face);

	@Override
	QuadEmitter cullFace(@Nullable Direction face);

	@Override
	QuadEmitter renderLayer(@Nullable ChunkSectionLayer renderLayer);

	@Override
	QuadEmitter emissive(boolean emissive);

	@Override
	QuadEmitter diffuseShade(boolean shade);

	@Override
	QuadEmitter ambientOcclusion(TriState ao);

	@Override
	QuadEmitter glint(@Nullable ItemStackRenderState.FoilType glint);

	@Override
	QuadEmitter shadeMode(ShadeMode mode);

	@Override
	QuadEmitter tintIndex(int tintIndex);

	@Override
	QuadEmitter tag(int tag);

	@Override
	QuadEmitter copyFrom(QuadView quad);

	@Override
	QuadEmitter fromVanilla(int[] vertexData, int startIndex);

	@Override
	QuadEmitter fromBakedQuad(BakedQuad quad);


	float CULL_FACE_EPSILON = 0.00001f;


	default QuadEmitter square(Direction nominalFace, float left, float bottom, float right, float top, float depth) {
		if (Math.abs(depth) < CULL_FACE_EPSILON) {
			cullFace(nominalFace);
			depth = 0; // avoid any inconsistency for face quads
		} else {
			cullFace(null);
		}

		nominalFace(nominalFace);
		switch (nominalFace) {
		case UP:
			depth = 1 - depth;
			top = 1 - top;
			bottom = 1 - bottom;

		case DOWN:
			pos(0, left, depth, top);
			pos(1, left, depth, bottom);
			pos(2, right, depth, bottom);
			pos(3, right, depth, top);
			break;

		case EAST:
			depth = 1 - depth;
			left = 1 - left;
			right = 1 - right;

		case WEST:
			pos(0, depth, top, left);
			pos(1, depth, bottom, left);
			pos(2, depth, bottom, right);
			pos(3, depth, top, right);
			break;

		case SOUTH:
			depth = 1 - depth;
			left = 1 - left;
			right = 1 - right;

		case NORTH:
			pos(0, 1 - left, top, depth);
			pos(1, 1 - left, bottom, depth);
			pos(2, 1 - right, bottom, depth);
			pos(3, 1 - right, top, depth);
			break;
		}

		return this;
	}


	void pushTransform(QuadTransform transform);


	void popTransform();

	/**
	 * In static mesh building, causes quad to be appended to the mesh being built. In a dynamic render context, create
	 * a new quad to be output to rendering. In both cases, current instance is reset to default values.
	 */
	QuadEmitter emit();
}
