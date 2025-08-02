package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class TippedArrowItem extends ArrowItem {
	public TippedArrowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance() {
		ItemStack itemStack = super.getDefaultInstance();
		itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));
		return itemStack;
	}

	@Override
	public Component getName(ItemStack itemStack) {
		PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
		return potionContents != null ? potionContents.getName(this.descriptionId + ".effect.") : super.getName(itemStack);
	}
}
