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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.random.WeightedList.Flat;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.chat.FilterMask.Type;
import net.minecraft.client.renderer.block.model.BakedQuad;


public interface QuadView {
	/** Count of integers in a conventional (un-modded) block or item vertex. */
	int VANILLA_VERTEX_STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;

	/** Count of integers in a conventional (un-modded) block or item quad. */
	int VANILLA_QUAD_STRIDE = VANILLA_VERTEX_STRIDE * 4;

	/**
	 * Gets the X coordinate of the geometric position of the given vertex.
	 */
	float x(int vertexIndex);

	/**
	 * Gets the Y coordinate of the geometric position of the given vertex.
	 */
	float y(int vertexIndex);

	/**
	 * Gets the Z coordinate of the geometric position of the given vertex.
	 */
	float z(int vertexIndex);

	/**
	 * Gets the specified coordinate of the geometric position of the given vertex. Index 0 is X, 1 is Y, and 2 is Z.
	 */
	float posByIndex(int vertexIndex, int coordinateIndex);

	Vector3f copyPos(int vertexIndex, @Nullable Vector3f target);

	/**
	 * Gets the vertex color in ARGB format (0xAARRGGBB) of the given vertex.
	 */
	int color(int vertexIndex);

	/**
	 * Gets the horizontal texture coordinates of the given vertex.
	 */
	float u(int vertexIndex);

	/**
	 * Gets the vertical texture coordinates of the given vertex.
	 */
	float v(int vertexIndex);


	Vector2f copyUv(int vertexIndex, @Nullable Vector2f target);

	/**
	 * Gets the minimum lightmap value of the given vertex.
	 */
	int lightmap(int vertexIndex);


	boolean hasNormal(int vertexIndex);


	float normalX(int vertexIndex);

	float normalY(int vertexIndex);


	float normalZ(int vertexIndex);


	@Nullable
	Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target);

	/**
	 * Gets the normal vector of this quad as implied by its vertex positions. It will be invalid if the vertices are
	 * not co-planar.
	 */
	Vector3fc faceNormal();


	@NotNull
	Direction lightFace();


	@Nullable
	Direction nominalFace();


	@Nullable
	Direction cullFace();


	@Nullable
	ChunkSectionLayer renderLayer();


	boolean emissive();


	boolean diffuseShade();


	TriState ambientOcclusion();


	@Nullable
	ItemStackRenderState.FoilType glint();


	ShadeMode shadeMode();


	int tintIndex();


	int tag();


	void toVanilla(int[] target, int startIndex);


	default BakedQuad toBakedQuad(TextureAtlasSprite sprite) {
		int[] vertexData = new int[VANILLA_QUAD_STRIDE];
		toVanilla(vertexData, 0);

		// The light emission is set to 15 if the quad is emissive; otherwise, to the minimum of all four sky light
		// values and all four block light values.
		int lightEmission = 15;

		if (!emissive()) {
			for (int i = 0; i < 4; i++) {
				int lightmap = lightmap(i);

				if (lightmap == 0) {
					lightEmission = 0;
					break;
				}

				int blockLight = LightTexture.block(lightmap);
				int skyLight = LightTexture.sky(lightmap);
				lightEmission = Math.min(lightEmission, Math.min(blockLight, skyLight));
			}
		}

		return new BakedQuad(vertexData, tintIndex(), lightFace(), sprite, diffuseShade(), lightEmission);
	}
}
