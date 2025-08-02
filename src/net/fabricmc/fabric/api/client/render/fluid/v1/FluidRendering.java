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

package net.fabricmc.fabric.api.client.render.fluid.v1;

import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class FluidRendering {
	private FluidRendering() {
	}
	public static void render(FluidRenderHandler handler, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, double x, double y, double z, DefaultRenderer defaultRenderer) {
		FluidRenderingImpl.render(handler, world, pos, vertexConsumer, blockState, fluidState, z, y, z, defaultRenderer);
	}

	public interface DefaultRenderer {

		default void render(FluidRenderHandler handler, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, double d, double e, double f) {
			FluidRenderingImpl.renderVanillaDefault(handler, world, pos, vertexConsumer, blockState, fluidState, d, e, f);
		}
	}
}
