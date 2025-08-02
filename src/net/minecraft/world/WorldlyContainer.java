package net.minecraft.world;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface WorldlyContainer extends Container {
	int[] getSlotsForFace(Direction direction);

	boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction);

	boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction);
}
