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

package net.fabricmc.fabric.api.blockview.v2;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public interface FabricBlockView {

	@Nullable
	default Object getBlockEntityRenderData(BlockPos pos) {
		BlockEntity blockEntity = ((BlockGetter) this).getBlockEntity(pos);
		return blockEntity == null ? null : blockEntity.getRenderData();
	}


	default boolean hasBiomes() {
		return false;
	}


	@UnknownNullability
	default Holder<Biome> getBiomeFabric(BlockPos pos) {
		return null;
	}
}
