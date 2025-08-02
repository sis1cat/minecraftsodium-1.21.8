package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public interface SuspiciousEffectHolder {
	SuspiciousStewEffects getSuspiciousEffects();

	static List<SuspiciousEffectHolder> getAllEffectHolders() {
		return (List<SuspiciousEffectHolder>)BuiltInRegistries.ITEM
			.stream()
			.map(SuspiciousEffectHolder::tryGet)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Nullable
	static SuspiciousEffectHolder tryGet(ItemLike itemLike) {
		if (itemLike.asItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SuspiciousEffectHolder suspiciousEffectHolder) {
			return suspiciousEffectHolder;
		} else {
			return itemLike.asItem() instanceof SuspiciousEffectHolder suspiciousEffectHolder2 ? suspiciousEffectHolder2 : null;
		}
	}
}
