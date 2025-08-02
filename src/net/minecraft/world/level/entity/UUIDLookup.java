package net.minecraft.world.level.entity;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public interface UUIDLookup<IdentifiedType extends UniquelyIdentifyable> {
	@Nullable
	IdentifiedType getEntity(UUID uUID);
}
