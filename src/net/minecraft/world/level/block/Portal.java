package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jetbrains.annotations.Nullable;

public interface Portal {
	default int getPortalTransitionTime(ServerLevel serverLevel, Entity entity) {
		return 0;
	}

	@Nullable
	TeleportTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos);

	default Portal.Transition getLocalTransition() {
		return Portal.Transition.NONE;
	}

	public static enum Transition {
		CONFUSION,
		NONE;
	}
}
