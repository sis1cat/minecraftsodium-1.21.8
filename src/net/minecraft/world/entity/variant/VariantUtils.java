package net.minecraft.world.entity.variant;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class VariantUtils {
	public static final String TAG_VARIANT = "variant";

	public static <T> Holder<T> getDefaultOrAny(RegistryAccess registryAccess, ResourceKey<T> resourceKey) {
		Registry<T> registry = registryAccess.lookupOrThrow(resourceKey.registryKey());
		return (Holder<T>)registry.get(resourceKey).or(registry::getAny).orElseThrow();
	}

	public static <T> Holder<T> getAny(RegistryAccess registryAccess, ResourceKey<? extends Registry<T>> resourceKey) {
		return (Holder<T>)registryAccess.lookupOrThrow(resourceKey).getAny().orElseThrow();
	}

	public static <T> void writeVariant(ValueOutput valueOutput, Holder<T> holder) {
		holder.unwrapKey().ifPresent(resourceKey -> valueOutput.store("variant", ResourceLocation.CODEC, resourceKey.location()));
	}

	public static <T> Optional<Holder<T>> readVariant(ValueInput valueInput, ResourceKey<? extends Registry<T>> resourceKey) {
		return valueInput.read("variant", ResourceLocation.CODEC)
			.map(resourceLocation -> ResourceKey.create(resourceKey, resourceLocation))
			.flatMap(valueInput.lookup()::get);
	}

	public static <T extends PriorityProvider<SpawnContext, ?>> Optional<Holder.Reference<T>> selectVariantToSpawn(
		SpawnContext spawnContext, ResourceKey<Registry<T>> resourceKey
	) {
		ServerLevelAccessor serverLevelAccessor = spawnContext.level();
		Stream<Holder.Reference<T>> stream = serverLevelAccessor.registryAccess().lookupOrThrow(resourceKey).listElements();
		return PriorityProvider.pick(stream, Holder::value, serverLevelAccessor.getRandom(), spawnContext);
	}
}
