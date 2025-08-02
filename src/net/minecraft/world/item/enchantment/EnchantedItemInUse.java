package net.minecraft.world.item.enchantment;

import java.util.function.Consumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record EnchantedItemInUse(ItemStack itemStack, @Nullable EquipmentSlot inSlot, @Nullable LivingEntity owner, Consumer<Item> onBreak) {
	public EnchantedItemInUse(ItemStack itemStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity) {
		this(itemStack, equipmentSlot, livingEntity, item -> livingEntity.onEquippedItemBroken(item, equipmentSlot));
	}
}
