package net.minecraft.client.renderer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

@Environment(EnvType.CLIENT)
public record PostChainConfig(Map<ResourceLocation, PostChainConfig.InternalTarget> internalTargets, List<PostChainConfig.Pass> passes) {
	public static final Codec<PostChainConfig> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				Codec.unboundedMap(ResourceLocation.CODEC, PostChainConfig.InternalTarget.CODEC)
					.optionalFieldOf("targets", Map.of())
					.forGetter(PostChainConfig::internalTargets),
				PostChainConfig.Pass.CODEC.listOf().optionalFieldOf("passes", List.of()).forGetter(PostChainConfig::passes)
			)
			.apply(instance, PostChainConfig::new)
	);

	@Environment(EnvType.CLIENT)
	public sealed interface Input permits PostChainConfig.TextureInput, PostChainConfig.TargetInput {
		Codec<PostChainConfig.Input> CODEC = Codec.xor(PostChainConfig.TextureInput.CODEC, PostChainConfig.TargetInput.CODEC)
			.xmap(either -> either.map(Function.identity(), Function.identity()), input -> {
				return switch (input) {
					case PostChainConfig.TextureInput textureInput -> Either.left(textureInput);
					case PostChainConfig.TargetInput targetInput -> Either.right(targetInput);
					default -> throw new MatchException(null, null);
				};
			});

		String samplerName();

		Set<ResourceLocation> referencedTargets();
	}

	@Environment(EnvType.CLIENT)
	public record InternalTarget(Optional<Integer> width, Optional<Integer> height, boolean persistent, int clearColor) {
		public static final Codec<PostChainConfig.InternalTarget> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("width").forGetter(PostChainConfig.InternalTarget::width),
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("height").forGetter(PostChainConfig.InternalTarget::height),
					Codec.BOOL.optionalFieldOf("persistent", false).forGetter(PostChainConfig.InternalTarget::persistent),
					ExtraCodecs.ARGB_COLOR_CODEC.optionalFieldOf("clear_color", 0).forGetter(PostChainConfig.InternalTarget::clearColor)
				)
				.apply(instance, PostChainConfig.InternalTarget::new)
		);
	}

	@Environment(EnvType.CLIENT)
	public record Pass(
		ResourceLocation vertexShaderId,
		ResourceLocation fragmentShaderId,
		List<PostChainConfig.Input> inputs,
		ResourceLocation outputTarget,
		Map<String, List<UniformValue>> uniforms
	) {
		private static final Codec<List<PostChainConfig.Input>> INPUTS_CODEC = PostChainConfig.Input.CODEC.listOf().validate(list -> {
			Set<String> set = new ObjectArraySet<>(list.size());

			for (PostChainConfig.Input input : list) {
				if (!set.add(input.samplerName())) {
					return DataResult.error(() -> "Encountered repeated sampler name: " + input.samplerName());
				}
			}

			return DataResult.success(list);
		});
		private static final Codec<Map<String, List<UniformValue>>> UNIFORM_BLOCKS_CODEC = Codec.unboundedMap(Codec.STRING, UniformValue.CODEC.listOf());
		public static final Codec<PostChainConfig.Pass> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("vertex_shader").forGetter(PostChainConfig.Pass::vertexShaderId),
					ResourceLocation.CODEC.fieldOf("fragment_shader").forGetter(PostChainConfig.Pass::fragmentShaderId),
					INPUTS_CODEC.optionalFieldOf("inputs", List.of()).forGetter(PostChainConfig.Pass::inputs),
					ResourceLocation.CODEC.fieldOf("output").forGetter(PostChainConfig.Pass::outputTarget),
					UNIFORM_BLOCKS_CODEC.optionalFieldOf("uniforms", Map.of()).forGetter(PostChainConfig.Pass::uniforms)
				)
				.apply(instance, PostChainConfig.Pass::new)
		);

		public Stream<ResourceLocation> referencedTargets() {
			Stream<ResourceLocation> stream = this.inputs.stream().flatMap(input -> input.referencedTargets().stream());
			return Stream.concat(stream, Stream.of(this.outputTarget));
		}
	}

	@Environment(EnvType.CLIENT)
	public record TargetInput(String samplerName, ResourceLocation targetId, boolean useDepthBuffer, boolean bilinear) implements PostChainConfig.Input {
		public static final Codec<PostChainConfig.TargetInput> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.STRING.fieldOf("sampler_name").forGetter(PostChainConfig.TargetInput::samplerName),
					ResourceLocation.CODEC.fieldOf("target").forGetter(PostChainConfig.TargetInput::targetId),
					Codec.BOOL.optionalFieldOf("use_depth_buffer", false).forGetter(PostChainConfig.TargetInput::useDepthBuffer),
					Codec.BOOL.optionalFieldOf("bilinear", false).forGetter(PostChainConfig.TargetInput::bilinear)
				)
				.apply(instance, PostChainConfig.TargetInput::new)
		);

		@Override
		public Set<ResourceLocation> referencedTargets() {
			return Set.of(this.targetId);
		}
	}

	@Environment(EnvType.CLIENT)
	public record TextureInput(String samplerName, ResourceLocation location, int width, int height, boolean bilinear) implements PostChainConfig.Input {
		public static final Codec<PostChainConfig.TextureInput> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.STRING.fieldOf("sampler_name").forGetter(PostChainConfig.TextureInput::samplerName),
					ResourceLocation.CODEC.fieldOf("location").forGetter(PostChainConfig.TextureInput::location),
					ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(PostChainConfig.TextureInput::width),
					ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(PostChainConfig.TextureInput::height),
					Codec.BOOL.optionalFieldOf("bilinear", false).forGetter(PostChainConfig.TextureInput::bilinear)
				)
				.apply(instance, PostChainConfig.TextureInput::new)
		);

		@Override
		public Set<ResourceLocation> referencedTargets() {
			return Set.of();
		}
	}
}
