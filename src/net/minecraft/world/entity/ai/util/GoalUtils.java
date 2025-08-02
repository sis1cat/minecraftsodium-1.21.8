package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class GoalUtils {
	public static boolean hasGroundPathNavigation(Mob mob) {
		return mob.getNavigation().canNavigateGround();
	}

	public static boolean mobRestricted(PathfinderMob pathfinderMob, int i) {
		return pathfinderMob.hasHome() && pathfinderMob.getHomePosition().closerToCenterThan(pathfinderMob.position(), pathfinderMob.getHomeRadius() + i + 1);
	}

	public static boolean isOutsideLimits(BlockPos blockPos, PathfinderMob pathfinderMob) {
		return pathfinderMob.level().isOutsideBuildHeight(blockPos.getY());
	}

	public static boolean isRestricted(boolean bl, PathfinderMob pathfinderMob, BlockPos blockPos) {
		return bl && !pathfinderMob.isWithinHome(blockPos);
	}

	public static boolean isNotStable(PathNavigation pathNavigation, BlockPos blockPos) {
		return !pathNavigation.isStableDestination(blockPos);
	}

	public static boolean isWater(PathfinderMob pathfinderMob, BlockPos blockPos) {
		return pathfinderMob.level().getFluidState(blockPos).is(FluidTags.WATER);
	}

	public static boolean hasMalus(PathfinderMob pathfinderMob, BlockPos blockPos) {
		return pathfinderMob.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic(pathfinderMob, blockPos)) != 0.0F;
	}

	public static boolean isSolid(PathfinderMob pathfinderMob, BlockPos blockPos) {
		return pathfinderMob.level().getBlockState(blockPos).isSolid();
	}
}
