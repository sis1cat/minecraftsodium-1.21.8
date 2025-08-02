package net.minecraft.world.level.levelgen.material;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.jetbrains.annotations.Nullable;

public interface WorldGenMaterialRule {
	@Nullable
	BlockState apply(NoiseChunk noiseChunk, int i, int j, int k);
}
