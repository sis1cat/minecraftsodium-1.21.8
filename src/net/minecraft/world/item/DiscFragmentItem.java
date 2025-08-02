package net.minecraft.world.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.component.TooltipDisplay;

public class DiscFragmentItem extends Item {
	public DiscFragmentItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(
		ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag
	) {
		consumer.accept(this.getDisplayName().withStyle(ChatFormatting.GRAY));
	}

	public MutableComponent getDisplayName() {
		return Component.translatable(this.descriptionId + ".desc");
	}
}
