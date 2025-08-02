package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public interface CollisionContext {
	static CollisionContext empty() {
		return EntityCollisionContext.EMPTY;
	}

	static CollisionContext of(Entity entity) {
		return (CollisionContext)(switch (entity) {
			case AbstractMinecart abstractMinecart -> AbstractMinecart.useExperimentalMovement(abstractMinecart.level())
				? new MinecartCollisionContext(abstractMinecart, false)
				: new EntityCollisionContext(entity, false, false);
			default -> new EntityCollisionContext(entity, false, false);
		});
	}

	static CollisionContext of(Entity entity, boolean bl) {
		return new EntityCollisionContext(entity, bl, false);
	}

	static CollisionContext placementContext(@Nullable Player player) {
		return new EntityCollisionContext(
			player != null ? player.isDescending() : false,
			true,
			player != null ? player.getY() : -Double.MAX_VALUE,
			player instanceof LivingEntity ? player.getMainHandItem() : ItemStack.EMPTY,
			player instanceof LivingEntity ? fluidState -> player.canStandOnFluid(fluidState) : fluidState -> false,
			player
		);
	}

	static CollisionContext withPosition(@Nullable Entity entity, double d) {
		return new EntityCollisionContext(
			entity != null ? entity.isDescending() : false,
			true,
			entity != null ? d : -Double.MAX_VALUE,
			entity instanceof LivingEntity livingEntityx ? livingEntityx.getMainHandItem() : ItemStack.EMPTY,
			entity instanceof LivingEntity livingEntity ? fluidState -> livingEntity.canStandOnFluid(fluidState) : fluidState -> false,
			entity
		);
	}

	boolean isDescending();

	boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl);

	boolean isHoldingItem(Item item);

	boolean canStandOnFluid(FluidState fluidState, FluidState fluidState2);

	VoxelShape getCollisionShape(BlockState blockState, CollisionGetter collisionGetter, BlockPos blockPos);

	default boolean isPlacement() {
		return false;
	}
}
