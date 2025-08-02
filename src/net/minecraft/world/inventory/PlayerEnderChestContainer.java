package net.minecraft.world.inventory;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class PlayerEnderChestContainer extends SimpleContainer {
	@Nullable
	private EnderChestBlockEntity activeChest;

	public PlayerEnderChestContainer() {
		super(27);
	}

	public void setActiveChest(EnderChestBlockEntity enderChestBlockEntity) {
		this.activeChest = enderChestBlockEntity;
	}

	public boolean isActiveChest(EnderChestBlockEntity enderChestBlockEntity) {
		return this.activeChest == enderChestBlockEntity;
	}

	public void fromSlots(ValueInput.TypedInputList<ItemStackWithSlot> typedInputList) {
		for (int i = 0; i < this.getContainerSize(); i++) {
			this.setItem(i, ItemStack.EMPTY);
		}

		for (ItemStackWithSlot itemStackWithSlot : typedInputList) {
			if (itemStackWithSlot.isValidInContainer(this.getContainerSize())) {
				this.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
			}
		}
	}

	public void storeAsSlots(ValueOutput.TypedOutputList<ItemStackWithSlot> typedOutputList) {
		for (int i = 0; i < this.getContainerSize(); i++) {
			ItemStack itemStack = this.getItem(i);
			if (!itemStack.isEmpty()) {
				typedOutputList.add(new ItemStackWithSlot(i, itemStack));
			}
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.activeChest != null && !this.activeChest.stillValid(player) ? false : super.stillValid(player);
	}

	@Override
	public void startOpen(Player player) {
		if (this.activeChest != null) {
			this.activeChest.startOpen(player);
		}

		super.startOpen(player);
	}

	@Override
	public void stopOpen(Player player) {
		if (this.activeChest != null) {
			this.activeChest.stopOpen(player);
		}

		super.stopOpen(player);
		this.activeChest = null;
	}
}
