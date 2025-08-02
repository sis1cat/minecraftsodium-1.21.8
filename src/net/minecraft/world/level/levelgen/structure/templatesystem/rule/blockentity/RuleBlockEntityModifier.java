package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public interface RuleBlockEntityModifier {
	Codec<RuleBlockEntityModifier> CODEC = BuiltInRegistries.RULE_BLOCK_ENTITY_MODIFIER
		.byNameCodec()
		.dispatch(RuleBlockEntityModifier::getType, RuleBlockEntityModifierType::codec);

	@Nullable
	CompoundTag apply(RandomSource randomSource, @Nullable CompoundTag compoundTag);

	RuleBlockEntityModifierType<?> getType();
}
