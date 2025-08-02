package net.minecraft.world.level.storage.loot.providers.nbt;

import java.util.Set;
import net.minecraft.nbt.Tag;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

public interface NbtProvider {
	@Nullable
	Tag get(LootContext lootContext);

	Set<ContextKey<?>> getReferencedContextParams();

	LootNbtProviderType getType();
}
