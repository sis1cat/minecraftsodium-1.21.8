package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

public record FireworksPredicate(
	Optional<CollectionPredicate<FireworkExplosion, FireworkExplosionPredicate.FireworkPredicate>> explosions, MinMaxBounds.Ints flightDuration
) implements SingleComponentItemPredicate<Fireworks> {
	public static final Codec<FireworksPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				CollectionPredicate.codec(FireworkExplosionPredicate.FireworkPredicate.CODEC).optionalFieldOf("explosions").forGetter(FireworksPredicate::explosions),
				MinMaxBounds.Ints.CODEC.optionalFieldOf("flight_duration", MinMaxBounds.Ints.ANY).forGetter(FireworksPredicate::flightDuration)
			)
			.apply(instance, FireworksPredicate::new)
	);

	@Override
	public DataComponentType<Fireworks> componentType() {
		return DataComponents.FIREWORKS;
	}

	public boolean matches(Fireworks fireworks) {
		return this.explosions.isPresent() && !((CollectionPredicate)this.explosions.get()).test((Iterable)fireworks.explosions())
			? false
			: this.flightDuration.matches(fireworks.flightDuration());
	}
}
