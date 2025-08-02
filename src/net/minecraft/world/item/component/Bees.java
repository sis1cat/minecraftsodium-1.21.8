package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;

public record Bees(List<BeehiveBlockEntity.Occupant> bees) implements TooltipProvider {
	public static final Codec<Bees> CODEC = BeehiveBlockEntity.Occupant.LIST_CODEC.xmap(Bees::new, Bees::bees);
	public static final StreamCodec<ByteBuf, Bees> STREAM_CODEC = BeehiveBlockEntity.Occupant.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Bees::new, Bees::bees);
	public static final Bees EMPTY = new Bees(List.of());

	@Override
	public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
		consumer.accept(Component.translatable("container.beehive.bees", this.bees.size(), 3).withStyle(ChatFormatting.GRAY));
	}
}
