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

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.core.Direction;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Collection of utilities for model implementations.
 */
public final class ModelHelper {

	private static final Direction[] FACES = Arrays.copyOf(Direction.values(), 7);


	public static final int NULL_FACE_ID = 6;

	private ModelHelper() { }


	public static int toFaceIndex(@Nullable Direction face) {
		return face == null ? NULL_FACE_ID : face.get3DDataValue();
	}


	@Nullable
	public static Direction faceFromIndex(int faceIndex) {
		return FACES[faceIndex];
	}

	public static List<BakedQuad>[] toQuadLists(Mesh mesh) {
		SpriteFinder finder = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).spriteFinder();

		@SuppressWarnings("unchecked")
		final ImmutableList.Builder<BakedQuad>[] builders = new ImmutableList.Builder[7];

		for (int i = 0; i < 7; i++) {
			builders[i] = ImmutableList.builder();
		}

		mesh.forEach(q -> {
			Direction cullFace = q.cullFace();
			builders[cullFace == null ? NULL_FACE_ID : cullFace.get3DDataValue()].add(q.toBakedQuad(finder.find(q)));
		});

		@SuppressWarnings("unchecked")
		List<BakedQuad>[] result = new List[7];

		for (int i = 0; i < 7; i++) {
			result[i] = builders[i].build();
		}

		return result;
	}
}
