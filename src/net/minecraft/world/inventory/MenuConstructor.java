package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MenuConstructor {
	@Nullable
	AbstractContainerMenu createMenu(int i, Inventory inventory, Player player);
}
