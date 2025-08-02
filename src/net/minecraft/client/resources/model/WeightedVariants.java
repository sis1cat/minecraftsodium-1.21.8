package net.minecraft.client.resources.model;

import java.util.List;
import java.util.function.Predicate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WeightedVariants implements BlockStateModel {
	private final WeightedList<BlockStateModel> list;
	private final TextureAtlasSprite particleIcon;

	public WeightedVariants(WeightedList<BlockStateModel> weightedList) {
		this.list = weightedList;
		BlockStateModel blockStateModel = (BlockStateModel)((Weighted)weightedList.unwrap().getFirst()).value();
		this.particleIcon = blockStateModel.particleIcon();
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return this.particleIcon;
	}

	@Override
	public void collectParts(RandomSource randomSource, List<BlockModelPart> list) {
		this.list.getRandomOrThrow(randomSource).collectParts(randomSource, list);
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(WeightedList<BlockStateModel.Unbaked> entries) implements BlockStateModel.Unbaked {
		@Override
		public BlockStateModel bake(ModelBaker modelBaker) {
			return new WeightedVariants(this.entries.map(unbaked -> unbaked.bake(modelBaker)));
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			this.entries.unwrap().forEach(weighted -> ((BlockStateModel.Unbaked)weighted.value()).resolveDependencies(resolver));
		}
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		list.getRandomOrThrow(random).emitQuads(emitter, blockView, pos, state, random, cullTest);
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random) {
		return list.getRandomOrThrow(random).createGeometryKey(blockView, pos, state, random);
	}

	@Override
	public TextureAtlasSprite particleSprite(BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		return list.unwrap().getFirst().value().particleSprite(blockView, pos, state);
	}

}
