package net.minecraft.world.entity.animal.wolf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;

public record WolfVariant(WolfVariant.AssetInfo assetInfo, SpawnPrioritySelectors spawnConditions) implements PriorityProvider<SpawnContext, SpawnCondition> {
	public static final Codec<WolfVariant> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				WolfVariant.AssetInfo.CODEC.fieldOf("assets").forGetter(WolfVariant::assetInfo),
				SpawnPrioritySelectors.CODEC.fieldOf("spawn_conditions").forGetter(WolfVariant::spawnConditions)
			)
			.apply(instance, WolfVariant::new)
	);
	public static final Codec<WolfVariant> NETWORK_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(WolfVariant.AssetInfo.CODEC.fieldOf("assets").forGetter(WolfVariant::assetInfo)).apply(instance, WolfVariant::new)
	);
	public static final Codec<Holder<WolfVariant>> CODEC = RegistryFixedCodec.create(Registries.WOLF_VARIANT);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WOLF_VARIANT);

	private WolfVariant(WolfVariant.AssetInfo assetInfo) {
		this(assetInfo, SpawnPrioritySelectors.EMPTY);
	}

	@Override
	public List<PriorityProvider.Selector<SpawnContext, SpawnCondition>> selectors() {
		return this.spawnConditions.selectors();
	}

	public record AssetInfo(ClientAsset wild, ClientAsset tame, ClientAsset angry) {
		public static final Codec<WolfVariant.AssetInfo> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					ClientAsset.CODEC.fieldOf("wild").forGetter(WolfVariant.AssetInfo::wild),
					ClientAsset.CODEC.fieldOf("tame").forGetter(WolfVariant.AssetInfo::tame),
					ClientAsset.CODEC.fieldOf("angry").forGetter(WolfVariant.AssetInfo::angry)
				)
				.apply(instance, WolfVariant.AssetInfo::new)
		);
	}
}
