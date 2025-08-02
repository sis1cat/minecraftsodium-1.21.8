package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public record PotionsPredicate(HolderSet<Potion> potions) implements SingleComponentItemPredicate<PotionContents> {
	public static final Codec<PotionsPredicate> CODEC = RegistryCodecs.homogeneousList(Registries.POTION).xmap(PotionsPredicate::new, PotionsPredicate::potions);

	@Override
	public DataComponentType<PotionContents> componentType() {
		return DataComponents.POTION_CONTENTS;
	}

	public boolean matches(PotionContents potionContents) {
		Optional<Holder<Potion>> optional = potionContents.potion();
		return !optional.isEmpty() && this.potions.contains((Holder<Potion>)optional.get());
	}

	public static DataComponentPredicate potions(HolderSet<Potion> holderSet) {
		return new PotionsPredicate(holderSet);
	}
}
