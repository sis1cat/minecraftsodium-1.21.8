package net.minecraft.client.resources.metadata.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ExtraCodecs;

@Environment(EnvType.CLIENT)
public record AnimationMetadataSection(
	Optional<List<AnimationFrame>> frames, Optional<Integer> frameWidth, Optional<Integer> frameHeight, int defaultFrameTime, boolean interpolatedFrames
) {
	public static final Codec<AnimationMetadataSection> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				AnimationFrame.CODEC.listOf().optionalFieldOf("frames").forGetter(AnimationMetadataSection::frames),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("width").forGetter(AnimationMetadataSection::frameWidth),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("height").forGetter(AnimationMetadataSection::frameHeight),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("frametime", 1).forGetter(AnimationMetadataSection::defaultFrameTime),
				Codec.BOOL.optionalFieldOf("interpolate", false).forGetter(AnimationMetadataSection::interpolatedFrames)
			)
			.apply(instance, AnimationMetadataSection::new)
	);
	public static final MetadataSectionType<AnimationMetadataSection> TYPE = new MetadataSectionType<>("animation", CODEC);

	public FrameSize calculateFrameSize(int i, int j) {
		if (this.frameWidth.isPresent()) {
			return this.frameHeight.isPresent()
				? new FrameSize((Integer)this.frameWidth.get(), (Integer)this.frameHeight.get())
				: new FrameSize((Integer)this.frameWidth.get(), j);
		} else if (this.frameHeight.isPresent()) {
			return new FrameSize(i, (Integer)this.frameHeight.get());
		} else {
			int k = Math.min(i, j);
			return new FrameSize(k, k);
		}
	}
}
