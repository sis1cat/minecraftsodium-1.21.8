package net.minecraft.world.entity.variant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;

public record BiomeCheck(HolderSet<Biome> requiredBiomes) implements SpawnCondition {
	public static final MapCodec<BiomeCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(BiomeCheck::requiredBiomes))
			.apply(instance, BiomeCheck::new)
	);

	public boolean test(SpawnContext spawnContext) {
		return this.requiredBiomes.contains(spawnContext.biome());
	}

	@Override
	public MapCodec<BiomeCheck> codec() {
		return MAP_CODEC;
	}
}
