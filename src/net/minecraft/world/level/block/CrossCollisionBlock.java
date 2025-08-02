package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class CrossCollisionBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty NORTH = PipeBlock.NORTH;
	public static final BooleanProperty EAST = PipeBlock.EAST;
	public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
	public static final BooleanProperty WEST = PipeBlock.WEST;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = (Map<Direction, BooleanProperty>)PipeBlock.PROPERTY_BY_DIRECTION
		.entrySet()
		.stream()
		.filter(entry -> ((Direction)entry.getKey()).getAxis().isHorizontal())
		.collect(Util.toMap());
	private final Function<BlockState, VoxelShape> collisionShapes;
	private final Function<BlockState, VoxelShape> shapes;

	protected CrossCollisionBlock(float f, float g, float h, float i, float j, BlockBehaviour.Properties properties) {
		super(properties);
		this.collisionShapes = this.makeShapes(f, j, h, 0.0F, j);
		this.shapes = this.makeShapes(f, g, h, 0.0F, i);
	}

	@Override
	protected abstract MapCodec<? extends CrossCollisionBlock> codec();

	protected Function<BlockState, VoxelShape> makeShapes(float f, float g, float h, float i, float j) {
		VoxelShape voxelShape = Block.column(f, 0.0, g);
		Map<Direction, VoxelShape> map = Shapes.rotateHorizontal(Block.boxZ(h, i, j, 0.0, 8.0));
		return this.getShapeForEachState(blockState -> {
			VoxelShape voxelShape2 = voxelShape;

			for (Entry<Direction, BooleanProperty> entry : PROPERTY_BY_DIRECTION.entrySet()) {
				if ((Boolean)blockState.getValue((Property)entry.getValue())) {
					voxelShape2 = Shapes.or(voxelShape2, (VoxelShape)map.get(entry.getKey()));
				}
			}

			return voxelShape2;
		}, new Property[]{WATERLOGGED});
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState blockState) {
		return !(Boolean)blockState.getValue(WATERLOGGED);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.shapes.apply(blockState);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)this.collisionShapes.apply(blockState);
	}

	@Override
	protected FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		switch (rotation) {
			case CLOCKWISE_180:
				return blockState.setValue(NORTH, (Boolean)blockState.getValue(SOUTH))
					.setValue(EAST, (Boolean)blockState.getValue(WEST))
					.setValue(SOUTH, (Boolean)blockState.getValue(NORTH))
					.setValue(WEST, (Boolean)blockState.getValue(EAST));
			case COUNTERCLOCKWISE_90:
				return blockState.setValue(NORTH, (Boolean)blockState.getValue(EAST))
					.setValue(EAST, (Boolean)blockState.getValue(SOUTH))
					.setValue(SOUTH, (Boolean)blockState.getValue(WEST))
					.setValue(WEST, (Boolean)blockState.getValue(NORTH));
			case CLOCKWISE_90:
				return blockState.setValue(NORTH, (Boolean)blockState.getValue(WEST))
					.setValue(EAST, (Boolean)blockState.getValue(NORTH))
					.setValue(SOUTH, (Boolean)blockState.getValue(EAST))
					.setValue(WEST, (Boolean)blockState.getValue(SOUTH));
			default:
				return blockState;
		}
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		switch (mirror) {
			case LEFT_RIGHT:
				return blockState.setValue(NORTH, (Boolean)blockState.getValue(SOUTH)).setValue(SOUTH, (Boolean)blockState.getValue(NORTH));
			case FRONT_BACK:
				return blockState.setValue(EAST, (Boolean)blockState.getValue(WEST)).setValue(WEST, (Boolean)blockState.getValue(EAST));
			default:
				return super.mirror(blockState, mirror);
		}
	}
}
