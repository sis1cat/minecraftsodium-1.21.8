package net.caffeinemc.mods.sodium.api.blockentity;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus.AvailableSince;
import org.jetbrains.annotations.ApiStatus.Experimental;

@FunctionalInterface
@Experimental
@AvailableSince("0.6.0")
public interface BlockEntityRenderPredicate<T extends BlockEntity> {
   boolean shouldRender(BlockGetter var1, BlockPos var2, T var3);
}
