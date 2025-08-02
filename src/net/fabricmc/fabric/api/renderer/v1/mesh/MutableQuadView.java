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
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.renderer.QuadSpriteBaker;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.random.WeightedList.Flat;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;


public interface MutableQuadView extends QuadView {

	int BAKE_ROTATE_NONE = 0;

	int BAKE_ROTATE_90 = 1;


	int BAKE_ROTATE_180 = 2;


	int BAKE_ROTATE_270 = 3;


	int BAKE_LOCK_UV = 4;


	int BAKE_FLIP_U = 8;


	int BAKE_FLIP_V = 16;

	int BAKE_NORMALIZED = 32;


	MutableQuadView pos(int vertexIndex, float x, float y, float z);


	default MutableQuadView pos(int vertexIndex, Vector3f pos) {
		return pos(vertexIndex, pos.x, pos.y, pos.z);
	}

	default MutableQuadView pos(int vertexIndex, Vector3fc pos) {
		return pos(vertexIndex, pos.x(), pos.y(), pos.z());
	}


	MutableQuadView color(int vertexIndex, int color);


	default MutableQuadView color(int c0, int c1, int c2, int c3) {
		color(0, c0);
		color(1, c1);
		color(2, c2);
		color(3, c3);
		return this;
	}


	MutableQuadView uv(int vertexIndex, float u, float v);

	default MutableQuadView uv(int vertexIndex, Vector2f uv) {
		return uv(vertexIndex, uv.x, uv.y);
	}


	default MutableQuadView uv(int vertexIndex, Vector2fc uv) {
		return uv(vertexIndex, uv.x(), uv.y());
	}


	default MutableQuadView spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
		QuadSpriteBaker.bakeSprite(this, sprite, bakeFlags);
		return this;
	}

	MutableQuadView lightmap(int vertexIndex, int lightmap);


	default MutableQuadView lightmap(int l0, int l1, int l2, int l3) {
		lightmap(0, l0);
		lightmap(1, l1);
		lightmap(2, l2);
		lightmap(3, l3);
		return this;
	}


	MutableQuadView normal(int vertexIndex, float x, float y, float z);


	default MutableQuadView normal(int vertexIndex, Vector3f normal) {
		return normal(vertexIndex, normal.x, normal.y, normal.z);
	}


	default MutableQuadView normal(int vertexIndex, Vector3fc normal) {
		return normal(vertexIndex, normal.x(), normal.y(), normal.z());
	}


	MutableQuadView nominalFace(@Nullable Direction face);


	MutableQuadView cullFace(@Nullable Direction face);


	MutableQuadView renderLayer(@Nullable ChunkSectionLayer renderLayer);


	MutableQuadView emissive(boolean emissive);


	MutableQuadView diffuseShade(boolean shade);


	MutableQuadView ambientOcclusion(TriState ao);


	MutableQuadView glint(@Nullable ItemStackRenderState.FoilType glint);

	MutableQuadView shadeMode(ShadeMode mode);


	MutableQuadView tintIndex(int tintIndex);


	MutableQuadView tag(int tag);


	MutableQuadView copyFrom(QuadView quad);


	MutableQuadView fromVanilla(int[] vertexData, int startIndex);


	MutableQuadView fromBakedQuad(BakedQuad quad);
}
