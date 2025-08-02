package net.minecraft.client.renderer.texture;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.sprite.FabricStitchResult;
import net.fabricmc.fabric.impl.renderer.SpriteFinderImpl;
import net.fabricmc.fabric.impl.renderer.StitchResultExtension;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SpriteLoader {
	public static final Set<MetadataSectionType<?>> DEFAULT_METADATA_SECTIONS = Set.of(AnimationMetadataSection.TYPE);
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ResourceLocation location;
	private final int maxSupportedTextureSize;
	private final int minWidth;
	private final int minHeight;

	public SpriteLoader(ResourceLocation resourceLocation, int i, int j, int k) {
		this.location = resourceLocation;
		this.maxSupportedTextureSize = i;
		this.minWidth = j;
		this.minHeight = k;
	}

	public static SpriteLoader create(TextureAtlas textureAtlas) {
		return new SpriteLoader(textureAtlas.location(), textureAtlas.maxSupportedTextureSize(), textureAtlas.getWidth(), textureAtlas.getHeight());
	}

	public SpriteLoader.Preparations stitch(List<SpriteContents> list, int mipmap, Executor executor) {
		SpriteLoader.Preparations var17;
		try (Zone zone = Profiler.get().zone((Supplier<String>)(() -> "stitch " + this.location))) {
			int j = this.maxSupportedTextureSize;
			Stitcher<SpriteContents> stitcher = new Stitcher<>(j, j, mipmap);
			int k = Integer.MAX_VALUE;
			int l = 1 << mipmap;

			for (SpriteContents spriteContents : list) {
				k = Math.min(k, Math.min(spriteContents.width(), spriteContents.height()));
				int m = Math.min(Integer.lowestOneBit(spriteContents.width()), Integer.lowestOneBit(spriteContents.height()));
				if (m < l) {
					LOGGER.warn(
						"Texture {} with size {}x{} limits mip level from {} to {}",
						spriteContents.name(),
						spriteContents.width(),
						spriteContents.height(),
						Mth.log2(l),
						Mth.log2(m)
					);
					l = m;
				}

				stitcher.registerSprite(spriteContents);
			}

			int n = Math.min(k, l);
			int o = Mth.log2(n);
			int m;
			if (o < mipmap) {
				LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, mipmap, o, n);
				m = o;
			} else {
				m = mipmap;
			}

			try {
				stitcher.stitch();
			} catch (StitcherException var19) {
				CrashReport crashReport = CrashReport.forThrowable(var19, "Stitching");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Stitcher");
				crashReportCategory.setDetail(
					"Sprites",
					var19.getAllSprites()
						.stream()
						.map(entry -> String.format(Locale.ROOT, "%s[%dx%d]", entry.name(), entry.width(), entry.height()))
						.collect(Collectors.joining(","))
				);
				crashReportCategory.setDetail("Max Texture Size", j);
				throw new ReportedException(crashReport);
			}

			int p = Math.max(stitcher.getWidth(), this.minWidth);
			int q = Math.max(stitcher.getHeight(), this.minHeight);
			Map<ResourceLocation, TextureAtlasSprite> map = this.getStitchedSprites(stitcher, p, q);
			TextureAtlasSprite textureAtlasSprite = map.get(MissingTextureAtlasSprite.getLocation());
			CompletableFuture<Void> completableFuture;
			if (m > 0) {
				completableFuture = CompletableFuture.runAsync(
					() -> map.values().forEach(textureAtlasSpritex -> textureAtlasSpritex.contents().increaseMipLevel(m)), executor
				);
			} else {
				completableFuture = CompletableFuture.completedFuture(null);
			}

			var17 = new Preparations(p, q, m, textureAtlasSprite, map, completableFuture);
		}

		return var17;
	}

	public static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(
		SpriteResourceLoader spriteResourceLoader, List<Function<SpriteResourceLoader, SpriteContents>> list, Executor executor
	) {
		List<CompletableFuture<SpriteContents>> list2 = list.stream()
			.map(function -> CompletableFuture.supplyAsync(() -> (SpriteContents)function.apply(spriteResourceLoader), executor))
			.toList();
		return Util.sequence(list2).thenApply(listx -> listx.stream().filter(Objects::nonNull).toList());
	}

	public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(ResourceManager resourceManager, ResourceLocation resourceLocation, int i, Executor executor) {
		return this.loadAndStitch(resourceManager, resourceLocation, i, executor, DEFAULT_METADATA_SECTIONS);
	}

	public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(
		ResourceManager resourceManager, ResourceLocation resourceLocation, int i, Executor executor, Collection<MetadataSectionType<?>> collection
	) {
		SpriteResourceLoader spriteResourceLoader = SpriteResourceLoader.create(collection);
		return CompletableFuture.supplyAsync(() -> SpriteSourceList.load(resourceManager, resourceLocation).list(resourceManager), executor)
			.thenCompose(list -> runSpriteSuppliers(spriteResourceLoader, list, executor))
			.thenApply(list -> this.stitch(list, i, executor));
	}

	private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher, int i, int j) {
		Map<ResourceLocation, TextureAtlasSprite> map = new HashMap();
		stitcher.gatherSprites((spriteContents, k, l) -> map.put(spriteContents.name(), new TextureAtlasSprite(this.location, spriteContents, i, j, k, l)));
		return map;
	}

	@Environment(EnvType.CLIENT)
	public static class Preparations implements FabricStitchResult, StitchResultExtension {

		private final int width;
		private final int height;
		private final int mipLevel;
		private final TextureAtlasSprite missing;
		private final Map<ResourceLocation, TextureAtlasSprite> regions;
		private final CompletableFuture<Void> readyForUpload;

		public int width() {
			return width;
		}
		public int height() {
			return height;
		}
		public int mipLevel() {
			return mipLevel;
		}
		public TextureAtlasSprite missing() {
			return missing;
		}
		public Map<ResourceLocation, TextureAtlasSprite> regions() {
			return regions;
		}
		public CompletableFuture<Void> readyForUpload() {
			return readyForUpload;
		}

		public Preparations(int width, int height, int mipLevel, TextureAtlasSprite missing, Map<ResourceLocation, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload) {
			this.width = width;
			this.height = height;
			this.mipLevel = mipLevel;
			this.missing = missing;
			this.regions = regions;
			this.readyForUpload = readyForUpload;
		}

		public CompletableFuture<SpriteLoader.Preparations> waitForUpload() {
			return this.readyForUpload.thenApply(void_ -> this);
		}
		private volatile SpriteFinder spriteFinder;
		@Override
		public SpriteFinder spriteFinder() {
			SpriteFinder result = spriteFinder;

			if (result == null) {
				synchronized (this) {
					result = spriteFinder;

					if (result == null) {
						spriteFinder = result = new SpriteFinderImpl(regions, missing);
					}
				}
			}

			return result;
		}

		@Override
		@Nullable
		public SpriteFinder fabric_spriteFinderNullable() {
			return spriteFinder;
		}
	}
}
