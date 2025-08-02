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

import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.TransparentBlock;


public interface FluidRenderHandlerRegistry {
	FluidRenderHandlerRegistry INSTANCE = new FluidRenderHandlerRegistryImpl();


	@Nullable
	FluidRenderHandler get(Fluid fluid);


	@Nullable
	FluidRenderHandler getOverride(Fluid fluid);

	void register(Fluid fluid, FluidRenderHandler renderer);

	default void register(Fluid still, Fluid flow, FluidRenderHandler renderer) {
		register(still, renderer);
		register(flow, renderer);
	}

	void setBlockTransparency(Block block, boolean transparent);

	boolean isBlockTransparent(Block block);
}
