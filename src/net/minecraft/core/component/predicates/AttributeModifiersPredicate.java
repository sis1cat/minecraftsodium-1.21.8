package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record AttributeModifiersPredicate(Optional<CollectionPredicate<ItemAttributeModifiers.Entry, AttributeModifiersPredicate.EntryPredicate>> modifiers)
	implements SingleComponentItemPredicate<ItemAttributeModifiers> {
	public static final Codec<AttributeModifiersPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				CollectionPredicate.codec(AttributeModifiersPredicate.EntryPredicate.CODEC).optionalFieldOf("modifiers").forGetter(AttributeModifiersPredicate::modifiers)
			)
			.apply(instance, AttributeModifiersPredicate::new)
	);

	@Override
	public DataComponentType<ItemAttributeModifiers> componentType() {
		return DataComponents.ATTRIBUTE_MODIFIERS;
	}

	public boolean matches(ItemAttributeModifiers itemAttributeModifiers) {
		return !this.modifiers.isPresent() || ((CollectionPredicate)this.modifiers.get()).test((Iterable)itemAttributeModifiers.modifiers());
	}

	public record EntryPredicate(
		Optional<HolderSet<Attribute>> attribute,
		Optional<ResourceLocation> id,
		MinMaxBounds.Doubles amount,
		Optional<AttributeModifier.Operation> operation,
		Optional<EquipmentSlotGroup> slot
	) implements Predicate<ItemAttributeModifiers.Entry> {
		public static final Codec<AttributeModifiersPredicate.EntryPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					RegistryCodecs.homogeneousList(Registries.ATTRIBUTE).optionalFieldOf("attribute").forGetter(AttributeModifiersPredicate.EntryPredicate::attribute),
					ResourceLocation.CODEC.optionalFieldOf("id").forGetter(AttributeModifiersPredicate.EntryPredicate::id),
					MinMaxBounds.Doubles.CODEC.optionalFieldOf("amount", MinMaxBounds.Doubles.ANY).forGetter(AttributeModifiersPredicate.EntryPredicate::amount),
					AttributeModifier.Operation.CODEC.optionalFieldOf("operation").forGetter(AttributeModifiersPredicate.EntryPredicate::operation),
					EquipmentSlotGroup.CODEC.optionalFieldOf("slot").forGetter(AttributeModifiersPredicate.EntryPredicate::slot)
				)
				.apply(instance, AttributeModifiersPredicate.EntryPredicate::new)
		);

		public boolean test(ItemAttributeModifiers.Entry entry) {
			if (this.attribute.isPresent() && !((HolderSet)this.attribute.get()).contains(entry.attribute())) {
				return false;
			} else if (this.id.isPresent() && !((ResourceLocation)this.id.get()).equals(entry.modifier().id())) {
				return false;
			} else if (!this.amount.matches(entry.modifier().amount())) {
				return false;
			} else {
				return this.operation.isPresent() && this.operation.get() != entry.modifier().operation()
					? false
					: !this.slot.isPresent() || this.slot.get() == entry.slot();
			}
		}
	}
}
