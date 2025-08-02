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

package net.fabricmc.fabric.api.renderer.v1.render;

import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.world.level.EmptyBlockAndTintGetter;


public interface FabricBlockModelRenderer {

	default void render(BlockAndTintGetter blockView, BlockStateModel model, BlockState state, BlockPos pos, PoseStack matrices, BlockVertexConsumerProvider vertexConsumers, boolean cull, long seed, int overlay) {
		Renderer.get().render((ModelBlockRenderer) this, blockView, model, state, pos, matrices, vertexConsumers, cull, seed, overlay);
	}


	static void render(PoseStack.Pose matrices, BlockVertexConsumerProvider vertexConsumers, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		Renderer.get().render(matrices, vertexConsumers, model, red, green, blue, light, overlay, blockView, pos, state);
	}
}
