package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record Weapon(int itemDamagePerAttack, float disableBlockingForSeconds) {
	public static final float AXE_DISABLES_BLOCKING_FOR_SECONDS = 5.0F;
	public static final Codec<Weapon> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("item_damage_per_attack", 1).forGetter(Weapon::itemDamagePerAttack),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("disable_blocking_for_seconds", 0.0F).forGetter(Weapon::disableBlockingForSeconds)
			)
			.apply(instance, Weapon::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, Weapon> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, Weapon::itemDamagePerAttack, ByteBufCodecs.FLOAT, Weapon::disableBlockingForSeconds, Weapon::new
	);

	public Weapon(int i) {
		this(i, 0.0F);
	}
}
