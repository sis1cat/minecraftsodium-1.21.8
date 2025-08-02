package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.sprite.FabricSpriteAtlasTexture;
import net.fabricmc.fabric.impl.renderer.SpriteFinderImpl;
import net.fabricmc.fabric.impl.renderer.StitchResultExtension;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.component.Weapon;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TextureAtlas extends AbstractTexture implements Dumpable, Tickable, FabricSpriteAtlasTexture {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Deprecated
	public static final ResourceLocation LOCATION_BLOCKS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
	@Deprecated
	public static final ResourceLocation LOCATION_PARTICLES = ResourceLocation.withDefaultNamespace("textures/atlas/particles.png");
	private List<SpriteContents> sprites = List.of();
	private List<TextureAtlasSprite.Ticker> animatedTextures = List.of();
	private Map<ResourceLocation, TextureAtlasSprite> texturesByName = Map.of();
	@Nullable
	private TextureAtlasSprite missingSprite;
	private final ResourceLocation location;
	private final int maxSupportedTextureSize;
	public int width;
	public int height;
	private int mipLevel;
	private volatile SpriteFinder spriteFinder;

	public TextureAtlas(ResourceLocation resourceLocation) {
		this.location = resourceLocation;
		this.maxSupportedTextureSize = RenderSystem.getDevice().getMaxTextureSize();
	}

	private void createTexture(int i, int j, int k) {
		LOGGER.info("Created: {}x{}x{} {}-atlas", i, j, k, this.location);
		GpuDevice gpuDevice = RenderSystem.getDevice();
		this.close();
		this.texture = gpuDevice.createTexture(this.location::toString, 7, TextureFormat.RGBA8, i, j, 1, k + 1);
		this.textureView = gpuDevice.createTextureView(this.texture);
		this.width = i;
		this.height = j;
		this.mipLevel = k;
	}

	public void upload(SpriteLoader.Preparations preparations) {
		this.createTexture(preparations.width(), preparations.height(), preparations.mipLevel());
		this.clearTextureData();
		this.setFilter(false, this.mipLevel > 1);
		this.texturesByName = Map.copyOf(preparations.regions());
		this.missingSprite = (TextureAtlasSprite)this.texturesByName.get(MissingTextureAtlasSprite.getLocation());
		if (this.missingSprite == null) {
			throw new IllegalStateException("Atlas '" + this.location + "' (" + this.texturesByName.size() + " sprites) has no missing texture sprite");
		} else {
			List<SpriteContents> list = new ArrayList();
			List<TextureAtlasSprite.Ticker> list2 = new ArrayList();

			for (TextureAtlasSprite textureAtlasSprite : preparations.regions().values()) {
				list.add(textureAtlasSprite.contents());

				try {
					textureAtlasSprite.uploadFirstFrame(this.texture);
				} catch (Throwable var9) {
					CrashReport crashReport = CrashReport.forThrowable(var9, "Stitching texture atlas");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
					crashReportCategory.setDetail("Atlas path", this.location);
					crashReportCategory.setDetail("Sprite", textureAtlasSprite);
					throw new ReportedException(crashReport);
				}

				TextureAtlasSprite.Ticker ticker = textureAtlasSprite.createTicker();
				if (ticker != null) {
					list2.add(ticker);
				}
			}

			this.sprites = List.copyOf(list);
			this.animatedTextures = List.copyOf(list2);
		}

		spriteFinder = ((StitchResultExtension) (Object) preparations).fabric_spriteFinderNullable();

		if (this.location.equals(TextureAtlas.LOCATION_BLOCKS)) {
			SpriteFinderCache.resetSpriteFinder();
		}

	}

	@Override
	public void dumpContents(ResourceLocation resourceLocation, Path path) throws IOException {
		String string = resourceLocation.toDebugFileName();
		TextureUtil.writeAsPNG(path, string, this.getTexture(), this.mipLevel, i -> i);
		dumpSpriteNames(path, string, this.texturesByName);
	}

	private static void dumpSpriteNames(Path path, String string, Map<ResourceLocation, TextureAtlasSprite> map) {
		Path path2 = path.resolve(string + ".txt");

		try {
			Writer writer = Files.newBufferedWriter(path2);

			try {
				for (Entry<ResourceLocation, TextureAtlasSprite> entry : map.entrySet().stream().sorted(Entry.comparingByKey()).toList()) {
					TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)entry.getValue();
					writer.write(
						String.format(
							Locale.ROOT,
							"%s\tx=%d\ty=%d\tw=%d\th=%d%n",
							entry.getKey(),
							textureAtlasSprite.getX(),
							textureAtlasSprite.getY(),
							textureAtlasSprite.contents().width(),
							textureAtlasSprite.contents().height()
						)
					);
				}
			} catch (Throwable var9) {
				if (writer != null) {
					try {
						writer.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}
				}

				throw var9;
			}

			if (writer != null) {
				writer.close();
			}
		} catch (IOException var10) {
			LOGGER.warn("Failed to write file {}", path2, var10);
		}
	}

	public void cycleAnimationFrames() {
		if (this.texture != null) {
			for (TextureAtlasSprite.Ticker ticker : this.animatedTextures) {
				ticker.tickAndUpload(this.texture);
			}
		}
	}

	@Override
	public void tick() {
		this.cycleAnimationFrames();
	}

	public TextureAtlasSprite getSprite(ResourceLocation resourceLocation) {
		TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)this.texturesByName.getOrDefault(resourceLocation, this.missingSprite);
		if (textureAtlasSprite == null) {
			throw new IllegalStateException("Tried to lookup sprite, but atlas is not initialized");
		} else {
			SpriteUtil.INSTANCE.markSpriteActive(textureAtlasSprite);
			return textureAtlasSprite;
		}
	}

	public void clearTextureData() {
		this.sprites.forEach(SpriteContents::close);
		this.animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
		this.sprites = List.of();
		this.animatedTextures = List.of();
		this.texturesByName = Map.of();
		this.missingSprite = null;
	}

	public ResourceLocation location() {
		return this.location;
	}

	public int maxSupportedTextureSize() {
		return this.maxSupportedTextureSize;
	}

	int getWidth() {
		return this.width;
	}

	int getHeight() {
		return this.height;
	}

	@Override
	public SpriteFinder spriteFinder() {
		SpriteFinder result = spriteFinder;

		if (result == null) {
			synchronized (this) {
				result = spriteFinder;

				if (result == null) {
					if (missingSprite == null) {
						throw new IllegalStateException("Tried to create sprite finder, but atlas is not initialized");
					}

					spriteFinder = result = new SpriteFinderImpl(texturesByName, missingSprite);
				}
			}
		}

		return result;
	}

}
