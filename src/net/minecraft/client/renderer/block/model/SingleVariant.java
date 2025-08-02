package net.minecraft.client.renderer.block.model;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SingleVariant implements BlockStateModel {
	private final BlockModelPart model;

	public SingleVariant(BlockModelPart blockModelPart) {
		this.model = blockModelPart;
	}

	@Override
	public void collectParts(RandomSource randomSource, List<BlockModelPart> list) {
		list.add(this.model);
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return this.model.particleIcon();
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(Variant variant) implements BlockStateModel.Unbaked {
		public static final Codec<SingleVariant.Unbaked> CODEC = Variant.CODEC.xmap(SingleVariant.Unbaked::new, SingleVariant.Unbaked::variant);

		@Override
		public BlockStateModel bake(ModelBaker modelBaker) {
			return new SingleVariant(this.variant.bake(modelBaker));
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			this.variant.resolveDependencies(resolver);
		}
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		model.emitQuads(emitter, cullTest);
	}

	@Override
	public Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random) {
		return this;
	}

}
