package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public record InstrumentComponent(EitherHolder<Instrument> instrument) implements TooltipProvider {
	public static final Codec<InstrumentComponent> CODEC = EitherHolder.codec(Registries.INSTRUMENT, Instrument.CODEC)
		.xmap(InstrumentComponent::new, InstrumentComponent::instrument);
	public static final StreamCodec<RegistryFriendlyByteBuf, InstrumentComponent> STREAM_CODEC = EitherHolder.streamCodec(
			Registries.INSTRUMENT, Instrument.STREAM_CODEC
		)
		.map(InstrumentComponent::new, InstrumentComponent::instrument);

	public InstrumentComponent(Holder<Instrument> holder) {
		this(new EitherHolder<>(holder));
	}

	@Deprecated
	public InstrumentComponent(ResourceKey<Instrument> resourceKey) {
		this(new EitherHolder<>(resourceKey));
	}

	@Override
	public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
		HolderLookup.Provider provider = tooltipContext.registries();
		if (provider != null) {
			Optional<Holder<Instrument>> optional = this.unwrap(provider);
			if (optional.isPresent()) {
				MutableComponent mutableComponent = ((Instrument)((Holder)optional.get()).value()).description().copy();
				ComponentUtils.mergeStyles(mutableComponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
				consumer.accept(mutableComponent);
			}
		}
	}

	public Optional<Holder<Instrument>> unwrap(HolderLookup.Provider provider) {
		return this.instrument.unwrap(provider);
	}
}
