package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TintedParticleLeavesBlock extends LeavesBlock {
	public static final MapCodec<TintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter(tintedParticleLeavesBlock -> tintedParticleLeavesBlock.leafParticleChance),
				propertiesCodec()
			)
			.apply(instance, TintedParticleLeavesBlock::new)
	);

	public TintedParticleLeavesBlock(float f, BlockBehaviour.Properties properties) {
		super(f, properties);
	}

	@Override
	protected void spawnFallingLeavesParticle(Level level, BlockPos blockPos, RandomSource randomSource) {
		ColorParticleOption colorParticleOption = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, level.getClientLeafTintColor(blockPos));
		ParticleUtils.spawnParticleBelow(level, blockPos, randomSource, colorParticleOption);
	}

	@Override
	public MapCodec<? extends TintedParticleLeavesBlock> codec() {
		return CODEC;
	}
}
