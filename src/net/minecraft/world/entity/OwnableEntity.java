package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface OwnableEntity {
	@Nullable
	EntityReference<LivingEntity> getOwnerReference();

	Level level();

	@Nullable
	default LivingEntity getOwner() {
		return (LivingEntity)EntityReference.get(this.getOwnerReference(), this.level(), LivingEntity.class);
	}

	@Nullable
	default LivingEntity getRootOwner() {
		Set<Object> set = new ObjectArraySet<>();
		LivingEntity livingEntity = this.getOwner();
		set.add(this);

		while (livingEntity instanceof OwnableEntity) {
			OwnableEntity ownableEntity = (OwnableEntity)livingEntity;
			LivingEntity livingEntity2 = ownableEntity.getOwner();
			if (set.contains(livingEntity2)) {
				return null;
			}

			set.add(livingEntity);
			livingEntity = ownableEntity.getOwner();
		}

		return livingEntity;
	}
}
