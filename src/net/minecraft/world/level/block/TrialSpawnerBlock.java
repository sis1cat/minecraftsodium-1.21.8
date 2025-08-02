package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class TrialSpawnerBlock extends BaseEntityBlock {
	public static final MapCodec<TrialSpawnerBlock> CODEC = simpleCodec(TrialSpawnerBlock::new);
	public static final EnumProperty<TrialSpawnerState> STATE = BlockStateProperties.TRIAL_SPAWNER_STATE;
	public static final BooleanProperty OMINOUS = BlockStateProperties.OMINOUS;

	@Override
	public MapCodec<TrialSpawnerBlock> codec() {
		return CODEC;
	}

	public TrialSpawnerBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(STATE, TrialSpawnerState.INACTIVE).setValue(OMINOUS, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STATE, OMINOUS);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new TrialSpawnerBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level instanceof ServerLevel serverLevel
			? createTickerHelper(
				blockEntityType,
				BlockEntityType.TRIAL_SPAWNER,
				(levelx, blockPos, blockStatex, trialSpawnerBlockEntity) -> trialSpawnerBlockEntity.getTrialSpawner()
					.tickServer(serverLevel, blockPos, (Boolean)blockStatex.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false))
			)
			: createTickerHelper(
				blockEntityType,
				BlockEntityType.TRIAL_SPAWNER,
				(levelx, blockPos, blockStatex, trialSpawnerBlockEntity) -> trialSpawnerBlockEntity.getTrialSpawner()
					.tickClient(levelx, blockPos, (Boolean)blockStatex.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false))
			);
	}
}
