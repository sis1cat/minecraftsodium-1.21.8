package net.minecraft.world.entity.animal;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.Biome;

public class ChickenVariants {
	public static final ResourceKey<ChickenVariant> TEMPERATE = createKey(TemperatureVariants.TEMPERATE);
	public static final ResourceKey<ChickenVariant> WARM = createKey(TemperatureVariants.WARM);
	public static final ResourceKey<ChickenVariant> COLD = createKey(TemperatureVariants.COLD);
	public static final ResourceKey<ChickenVariant> DEFAULT = TEMPERATE;

	private static ResourceKey<ChickenVariant> createKey(ResourceLocation resourceLocation) {
		return ResourceKey.create(Registries.CHICKEN_VARIANT, resourceLocation);
	}

	public static void bootstrap(BootstrapContext<ChickenVariant> bootstrapContext) {
		register(bootstrapContext, TEMPERATE, ChickenVariant.ModelType.NORMAL, "temperate_chicken", SpawnPrioritySelectors.fallback(0));
		register(bootstrapContext, WARM, ChickenVariant.ModelType.NORMAL, "warm_chicken", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
		register(bootstrapContext, COLD, ChickenVariant.ModelType.COLD, "cold_chicken", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
	}

	private static void register(
		BootstrapContext<ChickenVariant> bootstrapContext,
		ResourceKey<ChickenVariant> resourceKey,
		ChickenVariant.ModelType modelType,
		String string,
		TagKey<Biome> tagKey
	) {
		HolderSet<Biome> holderSet = bootstrapContext.lookup(Registries.BIOME).getOrThrow(tagKey);
		register(bootstrapContext, resourceKey, modelType, string, SpawnPrioritySelectors.single(new BiomeCheck(holderSet), 1));
	}

	private static void register(
		BootstrapContext<ChickenVariant> bootstrapContext,
		ResourceKey<ChickenVariant> resourceKey,
		ChickenVariant.ModelType modelType,
		String string,
		SpawnPrioritySelectors spawnPrioritySelectors
	) {
		ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("entity/chicken/" + string);
		bootstrapContext.register(resourceKey, new ChickenVariant(new ModelAndTexture<>(modelType, resourceLocation), spawnPrioritySelectors));
	}
}
