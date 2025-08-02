package net.minecraft.client.renderer.texture.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface SpriteResourceLoader {
	Logger LOGGER = LogUtils.getLogger();

	static SpriteResourceLoader create(Collection<MetadataSectionType<?>> collection) {
		return (resourceLocation, resource) -> {
			ResourceMetadata resourceMetadata;
			try {
				resourceMetadata = resource.metadata().copySections(collection);
			} catch (Exception var9) {
				LOGGER.error("Unable to parse metadata from {}", resourceLocation, var9);
				return null;
			}

			NativeImage nativeImage;
			try {
				InputStream inputStream = resource.open();

				try {
					nativeImage = NativeImage.read(inputStream);
				} catch (Throwable var10) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var8) {
							var10.addSuppressed(var8);
						}
					}

					throw var10;
				}

				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException var11) {
				LOGGER.error("Using missing texture, unable to load {}", resourceLocation, var11);
				return null;
			}

			Optional<AnimationMetadataSection> optional = resourceMetadata.getSection(AnimationMetadataSection.TYPE);
			FrameSize frameSize;
			if (optional.isPresent()) {
				frameSize = ((AnimationMetadataSection)optional.get()).calculateFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
				if (!Mth.isMultipleOf(nativeImage.getWidth(), frameSize.width()) || !Mth.isMultipleOf(nativeImage.getHeight(), frameSize.height())) {
					LOGGER.error(
						"Image {} size {},{} is not multiple of frame size {},{}",
						resourceLocation,
						nativeImage.getWidth(),
						nativeImage.getHeight(),
						frameSize.width(),
						frameSize.height()
					);
					nativeImage.close();
					return null;
				}
			} else {
				frameSize = new FrameSize(nativeImage.getWidth(), nativeImage.getHeight());
			}

			return new SpriteContents(resourceLocation, frameSize, nativeImage, resourceMetadata);
		};
	}

	@Nullable
	SpriteContents loadSprite(ResourceLocation resourceLocation, Resource resource);
}
