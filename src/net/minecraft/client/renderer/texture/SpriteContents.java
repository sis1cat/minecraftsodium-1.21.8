package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteContentsExtension;
import net.caffeinemc.mods.sodium.client.util.NativeImageHelper;
import net.caffeinemc.mods.sodium.client.util.color.ColorSRGB;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.data.info.DatapackStructureReport;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SpriteContents implements Stitcher.Entry, AutoCloseable, SpriteContentsExtension, net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.SpriteContentsExtension {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ResourceLocation name;
	final int width;
	final int height;
	private final NativeImage originalImage;
	public NativeImage[] byMipLevel;
	@Nullable
	private final SpriteContents.AnimatedTexture animatedTexture;
	private final ResourceMetadata metadata;
	private boolean active;

	public boolean sodium$hasTransparentPixels = false;
	public boolean sodium$hasTranslucentPixels = false;


	@Override
	public void sodium$setActive(boolean value) {
		this.active = value;
	}

	@Override
	public boolean sodium$hasAnimation() {
		return this.animatedTexture != null;
	}

	@Override
	public boolean sodium$isActive() {
		return this.active;
	}

	@Override
	public boolean sodium$hasTransparentPixels() {
		return this.sodium$hasTransparentPixels;
	}

	@Override
	public boolean sodium$hasTranslucentPixels() {
		return this.sodium$hasTranslucentPixels;
	}

	private static void sodium$fillInTransparentPixelColors(NativeImage nativeImage) {
		long ppPixel = NativeImageHelper.getPointerRGBA(nativeImage);
		int pixelCount = nativeImage.getWidth() * nativeImage.getHeight();
		float r = 0.0F;
		float g = 0.0F;
		float b = 0.0F;
		float totalWeight = 0.0F;

		for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
			long pPixel = ppPixel + pixelIndex * 4L;
			int color = MemoryUtil.memGetInt(pPixel);
			int alpha = ColorABGR.unpackAlpha(color);
			if (alpha != 0) {
                r += ColorSRGB.srgbToLinear(ColorABGR.unpackRed(color)) * (float) alpha;
				g += ColorSRGB.srgbToLinear(ColorABGR.unpackGreen(color)) * (float) alpha;
				b += ColorSRGB.srgbToLinear(ColorABGR.unpackBlue(color)) * (float) alpha;
				totalWeight += (float) alpha;
			}
		}

		if (totalWeight != 0.0F) {
			r /= totalWeight;
			g /= totalWeight;
			b /= totalWeight;
			int averageColor = ColorSRGB.linearToSrgb(r, g, b, 0);

			for (int pixelIndexx = 0; pixelIndexx < pixelCount; pixelIndexx++) {
				long pPixel = ppPixel + pixelIndexx * 4L;
				int color = MemoryUtil.memGetInt(pPixel);
				int alpha = ColorABGR.unpackAlpha(color);
				if (alpha == 0) {
					MemoryUtil.memPutInt(pPixel, averageColor);
				}
			}
		}
	}

	private void scanSpriteContents(NativeImage nativeImage) {
		long ppPixel = NativeImageHelper.getPointerRGBA(nativeImage);
		int pixelCount = nativeImage.getWidth() * nativeImage.getHeight();

		for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
			int color = MemoryUtil.memGetInt(ppPixel + pixelIndex * 4L);
			int alpha = ColorABGR.unpackAlpha(color);
			if (alpha <= 25) {
				this.sodium$hasTransparentPixels = true;
			} else if (alpha < 255) {
				this.sodium$hasTranslucentPixels = true;
			}
		}

		this.sodium$hasTransparentPixels = this.sodium$hasTransparentPixels | this.sodium$hasTranslucentPixels;

	}

	public SpriteContents(ResourceLocation resourceLocation, FrameSize frameSize, NativeImage nativeImage, ResourceMetadata resourceMetadata) {
		this.name = resourceLocation;
		this.width = frameSize.width();
		this.height = frameSize.height();
		this.metadata = resourceMetadata;
		this.animatedTexture = (SpriteContents.AnimatedTexture)resourceMetadata.getSection(AnimationMetadataSection.TYPE)
			.map(animationMetadataSection -> this.createAnimatedTexture(frameSize, nativeImage.getWidth(), nativeImage.getHeight(), animationMetadataSection))
			.orElse(null);
		this.scanSpriteContents(nativeImage);
		sodium$fillInTransparentPixelColors(nativeImage);
		this.originalImage = nativeImage;
		this.byMipLevel = new NativeImage[]{this.originalImage};
	}

	public void increaseMipLevel(int i) {
		try {
			this.byMipLevel = MipmapGenerator.generateMipLevels(this.byMipLevel, i);
		} catch (Throwable var6) {
			CrashReport crashReport = CrashReport.forThrowable(var6, "Generating mipmaps for frame");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Sprite being mipmapped");
			crashReportCategory.setDetail("First frame", (CrashReportDetail<String>)(() -> {
				StringBuilder stringBuilder = new StringBuilder();
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}

				stringBuilder.append(this.originalImage.getWidth()).append("x").append(this.originalImage.getHeight());
				return stringBuilder.toString();
			}));
			CrashReportCategory crashReportCategory2 = crashReport.addCategory("Frame being iterated");
			crashReportCategory2.setDetail("Sprite name", this.name);
			crashReportCategory2.setDetail("Sprite size", (CrashReportDetail<String>)(() -> this.width + " x " + this.height));
			crashReportCategory2.setDetail("Sprite frames", (CrashReportDetail<String>)(() -> this.getFrameCount() + " frames"));
			crashReportCategory2.setDetail("Mipmap levels", i);
			throw new ReportedException(crashReport);
		}
	}

	private int getFrameCount() {
		return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
	}

	@Nullable
	private SpriteContents.AnimatedTexture createAnimatedTexture(FrameSize frameSize, int i, int j, AnimationMetadataSection animationMetadataSection) {
		int k = i / frameSize.width();
		int l = j / frameSize.height();
		int m = k * l;
		int n = animationMetadataSection.defaultFrameTime();
		List<SpriteContents.FrameInfo> list;
		if (animationMetadataSection.frames().isEmpty()) {
			list = new ArrayList(m);

			for (int o = 0; o < m; o++) {
				list.add(new SpriteContents.FrameInfo(o, n));
			}
		} else {
			List<AnimationFrame> list2 = (List<AnimationFrame>)animationMetadataSection.frames().get();
			list = new ArrayList(list2.size());

			for (AnimationFrame animationFrame : list2) {
				list.add(new SpriteContents.FrameInfo(animationFrame.index(), animationFrame.timeOr(n)));
			}

			int p = 0;
			IntSet intSet = new IntOpenHashSet();

			for (Iterator<SpriteContents.FrameInfo> iterator = list.iterator(); iterator.hasNext(); p++) {
				SpriteContents.FrameInfo frameInfo = (SpriteContents.FrameInfo)iterator.next();
				boolean bl = true;
				if (frameInfo.time <= 0) {
					LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.name, p, frameInfo.time);
					bl = false;
				}

				if (frameInfo.index < 0 || frameInfo.index >= m) {
					LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.name, p, frameInfo.index);
					bl = false;
				}

				if (bl) {
					intSet.add(frameInfo.index);
				} else {
					iterator.remove();
				}
			}

			int[] is = IntStream.range(0, m).filter(ix -> !intSet.contains(ix)).toArray();
			if (is.length > 0) {
				LOGGER.warn("Unused frames in sprite {}: {}", this.name, Arrays.toString(is));
			}
		}

		return list.size() <= 1 ? null : new SpriteContents.AnimatedTexture(this, List.copyOf(list), k, animationMetadataSection.interpolatedFrames());
	}

	public void upload(int i, int j, int k, int l, NativeImage[] nativeImages, GpuTexture gpuTexture) {
		for (int m = 0; m < this.byMipLevel.length; m++) {
			RenderSystem.getDevice()
				.createCommandEncoder()
				.writeToTexture(gpuTexture, nativeImages[m], m, 0, i >> m, j >> m, this.width >> m, this.height >> m, k >> m, l >> m);
		}
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public int height() {
		return this.height;
	}

	@Override
	public ResourceLocation name() {
		return this.name;
	}

	public IntStream getUniqueFrames() {
		return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
	}

	@Nullable
	public SpriteTicker createTicker() {
		return this.animatedTexture != null ? this.animatedTexture.createTicker() : null;
	}

	public ResourceMetadata metadata() {
		return this.metadata;
	}

	public void close() {
		for (NativeImage nativeImage : this.byMipLevel) {
			nativeImage.close();
		}
	}

	public String toString() {
		return "SpriteContents{name=" + this.name + ", frameCount=" + this.getFrameCount() + ", height=" + this.height + ", width=" + this.width + "}";
	}

	public boolean isTransparent(int i, int j, int k) {
		int l = j;
		int m = k;
		if (this.animatedTexture != null) {
			l = j + this.animatedTexture.getFrameX(i) * this.width;
			m = k + this.animatedTexture.getFrameY(i) * this.height;
		}

		return ARGB.alpha(this.originalImage.getPixel(l, m)) == 0;
	}

	public void uploadFirstFrame(int i, int j, GpuTexture gpuTexture) {
		if (this.animatedTexture != null) {
			this.animatedTexture.uploadFirstFrame(i, j, gpuTexture);
		} else {
			this.upload(i, j, 0, 0, this.byMipLevel, gpuTexture);
		}
	}

	@Environment(EnvType.CLIENT)
	public class AnimatedTexture {
		public final List<SpriteContents.FrameInfo> frames;
		public final int frameRowSize;
		private final boolean interpolateFrames;
		private final SpriteContents parent;

		AnimatedTexture(SpriteContents parent, final List<SpriteContents.FrameInfo> list, final int i, final boolean bl) {
			this.frames = list;
			this.frameRowSize = i;
			this.interpolateFrames = bl;
			this.parent = parent;
		}

		int getFrameX(int i) {
			return i % this.frameRowSize;
		}

		int getFrameY(int i) {
			return i / this.frameRowSize;
		}

		void uploadFrame(int i, int j, int k, GpuTexture gpuTexture) {
			int l = this.getFrameX(k) * SpriteContents.this.width;
			int m = this.getFrameY(k) * SpriteContents.this.height;
			SpriteContents.this.upload(i, j, l, m, SpriteContents.this.byMipLevel, gpuTexture);
		}

		public SpriteTicker createTicker() {
			return SpriteContents.this.new Ticker(this.parent, this, this.interpolateFrames ? SpriteContents.this.new InterpolationData(this.parent) : null);
		}

		public void uploadFirstFrame(int i, int j, GpuTexture gpuTexture) {
			this.uploadFrame(i, j, ((SpriteContents.FrameInfo)this.frames.get(0)).index, gpuTexture);
		}

		public IntStream getUniqueFrames() {
			return this.frames.stream().mapToInt(frameInfo -> frameInfo.index).distinct();
		}
	}

	@Environment(EnvType.CLIENT)
	public record FrameInfo(int index, int time) {
	}

	@Environment(EnvType.CLIENT)
	final class InterpolationData implements AutoCloseable {
		private final NativeImage[] activeFrame = new NativeImage[SpriteContents.this.byMipLevel.length];
		private SpriteContents parent;
		private static final int STRIDE = 4;
		InterpolationData(SpriteContents parent) {
			for (int i = 0; i < this.activeFrame.length; i++) {
				int j = SpriteContents.this.width >> i;
				int k = SpriteContents.this.height >> i;
				this.activeFrame[i] = new NativeImage(j, k, false);
			}
			this.parent = parent;
		}

		void uploadInterpolatedFrame(int i, int j, SpriteContents.Ticker ticker, GpuTexture gpuTexture) {
			AnimatedTexture animation = ticker.animationInfo;
			AnimatedTexture animation2 = ticker.animationInfo;
			List<FrameInfo> frames = animation.frames;
			FrameInfo animationFrame = frames.get(ticker.frame);
			int curIndex = animationFrame.index();
			int nextIndex = (animation2.frames.get((ticker.frame + 1) % frames.size())).index();
			if (curIndex != nextIndex) {
				float mix = 1.0F - (float)ticker.subFrame / animationFrame.time();

				for (int layer = 0; layer < this.activeFrame.length; layer++) {
					int width = this.parent.width() >> layer;
					int height = this.parent.height() >> layer;
					int curX = curIndex % animation2.frameRowSize * width;
					int curY = curIndex / animation2.frameRowSize * height;
					int nextX = nextIndex % animation2.frameRowSize * width;
					int nextY = nextIndex / animation2.frameRowSize * height;
					NativeImage src = this.parent.byMipLevel[layer];
					NativeImage dst = this.activeFrame[layer];
					long ppSrcPixel = NativeImageHelper.getPointerRGBA(src);
					long ppDstPixel = NativeImageHelper.getPointerRGBA(dst);

					for (int layerY = 0; layerY < height; layerY++) {
						long pRgba1 = ppSrcPixel + (curX + (long)(curY + layerY) * src.getWidth()) * 4L;
						long pRgba2 = ppSrcPixel + (nextX + (long)(nextY + layerY) * src.getWidth()) * 4L;

						for (int layerX = 0; layerX < width; layerX++) {
							int rgba1 = MemoryUtil.memGetInt(pRgba1);
							int rgba2 = MemoryUtil.memGetInt(pRgba2);
							int mixedRgb = ColorMixer.mix(rgba1, rgba2, mix) & 16777215;
							int alpha = rgba1 & 0xFF000000;
							MemoryUtil.memPutInt(ppDstPixel, mixedRgb | alpha);
							pRgba1 += 4L;
							pRgba2 += 4L;
							ppDstPixel += 4L;
						}
					}
				}

				this.parent.upload(i, j, 0, 0, this.activeFrame, gpuTexture);
			}
			/*SpriteContents.AnimatedTexture animatedTexture = ticker.animationInfo;
			List<SpriteContents.FrameInfo> list = animatedTexture.frames;
			SpriteContents.FrameInfo frameInfo = (SpriteContents.FrameInfo)list.get(ticker.frame);
			float f = (float)ticker.subFrame / frameInfo.time;
			int k = frameInfo.index;
			int l = ((SpriteContents.FrameInfo)list.get((ticker.frame + 1) % list.size())).index;
			if (k != l) {
				for (int m = 0; m < this.activeFrame.length; m++) {
					int n = SpriteContents.this.width >> m;
					int o = SpriteContents.this.height >> m;

					for (int p = 0; p < o; p++) {
						for (int q = 0; q < n; q++) {
							int r = this.getPixel(animatedTexture, k, m, q, p);
							int s = this.getPixel(animatedTexture, l, m, q, p);
							this.activeFrame[m].setPixel(q, p, ARGB.lerp(f, r, s));
						}
					}
				}

				SpriteContents.this.upload(i, j, 0, 0, this.activeFrame, gpuTexture);
			}*/
		}

		private int getPixel(SpriteContents.AnimatedTexture animatedTexture, int i, int j, int k, int l) {
			return SpriteContents.this.byMipLevel[j]
				.getPixel(k + (animatedTexture.getFrameX(i) * SpriteContents.this.width >> j), l + (animatedTexture.getFrameY(i) * SpriteContents.this.height >> j));
		}

		public void close() {
			for (NativeImage nativeImage : this.activeFrame) {
				nativeImage.close();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public class Ticker implements SpriteTicker {
		public int frame;
		public int subFrame;
		public final SpriteContents.AnimatedTexture animationInfo;
		@Nullable
		private final SpriteContents.InterpolationData interpolationData;
		private final SpriteContents parent;

		Ticker(SpriteContents parent, final SpriteContents.AnimatedTexture animatedTexture, @Nullable final SpriteContents.InterpolationData interpolationData) {
			this.animationInfo = animatedTexture;
			this.interpolationData = interpolationData;
			this.parent = parent;
		}

		@Override
		public void tickAndUpload(int i, int j, GpuTexture gpuTexture) {

			SpriteContents parent = this.parent;
			boolean onDemand = SodiumClientMod.options().performance.animateOnlyVisibleTextures;
			if (onDemand && !parent.sodium$isActive()) {
				this.subFrame++;
				List<FrameInfo> frames = this.animationInfo.frames;
				if (this.subFrame >= frames.get(this.frame).time()) {
					this.frame = (this.frame + 1) % frames.size();
					this.subFrame = 0;
				}

				return;
			}

			this.subFrame++;
			SpriteContents.FrameInfo frameInfo = (SpriteContents.FrameInfo)this.animationInfo.frames.get(this.frame);
			if (this.subFrame >= frameInfo.time) {
				int k = frameInfo.index;
				this.frame = (this.frame + 1) % this.animationInfo.frames.size();
				this.subFrame = 0;
				int l = ((SpriteContents.FrameInfo)this.animationInfo.frames.get(this.frame)).index;
				if (k != l) {
					this.animationInfo.uploadFrame(i, j, l, gpuTexture);
				}
			} else if (this.interpolationData != null) {
				this.interpolationData.uploadInterpolatedFrame(i, j, this, gpuTexture);
			}

			this.parent.sodium$setActive(false);

		}

		@Override
		public void close() {
			if (this.interpolationData != null) {
				this.interpolationData.close();
			}
		}
	}
}
