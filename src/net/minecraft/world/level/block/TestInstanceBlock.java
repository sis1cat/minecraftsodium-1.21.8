package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TestInstanceBlock extends BaseEntityBlock implements GameMasterBlock {
	public static final MapCodec<TestInstanceBlock> CODEC = simpleCodec(TestInstanceBlock::new);

	public TestInstanceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TestInstanceBlockEntity(blockPos, blockState);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.getBlockEntity(blockPos) instanceof TestInstanceBlockEntity testInstanceBlockEntity) {
			if (!player.canUseGameMasterBlocks()) {
				return InteractionResult.PASS;
			} else {
				if (player.level().isClientSide) {
					player.openTestInstanceBlock(testInstanceBlockEntity);
				}

				return InteractionResult.SUCCESS;
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	protected MapCodec<TestInstanceBlock> codec() {
		return CODEC;
	}
}
