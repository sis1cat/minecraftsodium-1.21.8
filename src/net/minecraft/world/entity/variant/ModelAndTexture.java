package net.minecraft.world.entity.variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ModelAndTexture<T>(T model, ClientAsset asset) {
	public ModelAndTexture(T object, ResourceLocation resourceLocation) {
		this(object, new ClientAsset(resourceLocation));
	}

	public static <T> MapCodec<ModelAndTexture<T>> codec(Codec<T> codec, T object) {
		return RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					codec.optionalFieldOf("model", object).forGetter(ModelAndTexture::model), ClientAsset.DEFAULT_FIELD_CODEC.forGetter(ModelAndTexture::asset)
				)
				.apply(instance, ModelAndTexture::new)
		);
	}

	public static <T> StreamCodec<RegistryFriendlyByteBuf, ModelAndTexture<T>> streamCodec(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return StreamCodec.composite(streamCodec, ModelAndTexture::model, ClientAsset.STREAM_CODEC, ModelAndTexture::asset, ModelAndTexture::new);
	}
}
