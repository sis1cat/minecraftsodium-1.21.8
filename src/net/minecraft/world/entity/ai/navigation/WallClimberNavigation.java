package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

public class WallClimberNavigation extends GroundPathNavigation {
	@Nullable
	private BlockPos pathToPosition;

	public WallClimberNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	public Path createPath(BlockPos blockPos, int i) {
		this.pathToPosition = blockPos;
		return super.createPath(blockPos, i);
	}

	@Override
	public Path createPath(Entity entity, int i) {
		this.pathToPosition = entity.blockPosition();
		return super.createPath(entity, i);
	}

	@Override
	public boolean moveTo(Entity entity, double d) {
		Path path = this.createPath(entity, 0);
		if (path != null) {
			return this.moveTo(path, d);
		} else {
			this.pathToPosition = entity.blockPosition();
			this.speedModifier = d;
			return true;
		}
	}

	@Override
	public void tick() {
		if (!this.isDone()) {
			super.tick();
		} else {
			if (this.pathToPosition != null) {
				if (!this.pathToPosition.closerToCenterThan(this.mob.position(), this.mob.getBbWidth())
					&& (
						!(this.mob.getY() > this.pathToPosition.getY())
							|| !BlockPos.containing(this.pathToPosition.getX(), this.mob.getY(), this.pathToPosition.getZ())
								.closerToCenterThan(this.mob.position(), this.mob.getBbWidth())
					)) {
					this.mob.getMoveControl().setWantedPosition(this.pathToPosition.getX(), this.pathToPosition.getY(), this.pathToPosition.getZ(), this.speedModifier);
				} else {
					this.pathToPosition = null;
				}
			}
		}
	}
}
