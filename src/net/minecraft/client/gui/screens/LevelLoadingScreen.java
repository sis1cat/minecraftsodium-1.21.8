package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.status.ChunkStatus;

@Environment(EnvType.CLIENT)
public class LevelLoadingScreen extends Screen {
	private static final long NARRATION_DELAY_MS = 2000L;
	private final StoringChunkProgressListener progressListener;
	private long lastNarration = -1L;
	private boolean done;

	private static Reference2IntOpenHashMap<ChunkStatus> STATUS_TO_COLOR_FAST;

	private static final int NULL_STATUS_COLOR = ColorABGR.pack(0, 0, 0, 255);

	private static final int DEFAULT_STATUS_COLOR = ColorARGB.pack(0, 17, 255, 255);
	private static final Object2IntMap<ChunkStatus> COLORS = Util.make(new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> {
		object2IntOpenHashMap.defaultReturnValue(0);
		object2IntOpenHashMap.put(ChunkStatus.EMPTY, 5526612);
		object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_STARTS, 10066329);
		object2IntOpenHashMap.put(ChunkStatus.STRUCTURE_REFERENCES, 6250897);
		object2IntOpenHashMap.put(ChunkStatus.BIOMES, 8434258);
		object2IntOpenHashMap.put(ChunkStatus.NOISE, 13750737);
		object2IntOpenHashMap.put(ChunkStatus.SURFACE, 7497737);
		object2IntOpenHashMap.put(ChunkStatus.CARVERS, 3159410);
		object2IntOpenHashMap.put(ChunkStatus.FEATURES, 2213376);
		object2IntOpenHashMap.put(ChunkStatus.INITIALIZE_LIGHT, 13421772);
		object2IntOpenHashMap.put(ChunkStatus.LIGHT, 16769184);
		object2IntOpenHashMap.put(ChunkStatus.SPAWN, 15884384);
		object2IntOpenHashMap.put(ChunkStatus.FULL, 16777215);
	});

	public LevelLoadingScreen(StoringChunkProgressListener storingChunkProgressListener) {
		super(GameNarrator.NO_TITLE);
		this.progressListener = storingChunkProgressListener;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected boolean shouldNarrateNavigation() {
		return false;
	}

	@Override
	public void removed() {
		this.done = true;
		this.triggerImmediateNarration(true);
	}

	@Override
	protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
		if (this.done) {
			narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narrator.loading.done"));
		} else {
			narrationElementOutput.add(NarratedElementType.TITLE, this.getFormattedProgress());
		}
	}

	private Component getFormattedProgress() {
		return Component.translatable("loading.progress", Mth.clamp(this.progressListener.getProgress(), 0, 100));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		long l = Util.getMillis();
		if (l - this.lastNarration > 2000L) {
			this.lastNarration = l;
			this.triggerImmediateNarration(true);
		}

		int k = this.width / 2;
		int m = this.height / 2;
		renderChunks(guiGraphics, this.progressListener, k, m, 2, 0);
		int n = this.progressListener.getDiameter() + 9 + 2;
		guiGraphics.drawCenteredString(this.font, this.getFormattedProgress(), k, m - n, -1);
	}

	public static void renderChunks(GuiGraphics guiGraphics, StoringChunkProgressListener storingChunkProgressListener, int i, int j, int k, int l) {
		sodium$drawChunkMap(storingChunkProgressListener, i, j, k, l, guiGraphics);
		/*int m = k + l;
		int n = storingChunkProgressListener.getFullDiameter();
		int o = n * m - l;
		int p = storingChunkProgressListener.getDiameter();
		int q = p * m - l;
		int r = i - q / 2;
		int s = j - q / 2;
		int t = o / 2 + 1;
		int u = -16772609;
		if (l != 0) {
			guiGraphics.fill(i - t, j - t, i - t + 1, j + t, -16772609);
			guiGraphics.fill(i + t - 1, j - t, i + t, j + t, -16772609);
			guiGraphics.fill(i - t, j - t, i + t, j - t + 1, -16772609);
			guiGraphics.fill(i - t, j + t - 1, i + t, j + t, -16772609);
		}

		for (int v = 0; v < p; v++) {
			for (int w = 0; w < p; w++) {
				ChunkStatus chunkStatus = storingChunkProgressListener.getStatus(v, w);
				int x = r + v * m;
				int y = s + w * m;
				guiGraphics.fill(x, y, x + k, y + k, ARGB.opaque(COLORS.getInt(chunkStatus)));
			}
		}*/
	}

	private static void sodium$drawChunkMap(StoringChunkProgressListener listener, int mapX, int mapY, int mapScale, int mapPadding, GuiGraphics graphics) {
		if (STATUS_TO_COLOR_FAST == null) {
			STATUS_TO_COLOR_FAST = new Reference2IntOpenHashMap(COLORS.size());
			STATUS_TO_COLOR_FAST.put(null, NULL_STATUS_COLOR);
			COLORS.object2IntEntrySet().forEach(entry -> STATUS_TO_COLOR_FAST.put((ChunkStatus)entry.getKey(), ColorARGB.withAlpha(entry.getIntValue(), 255)));
		}

		int centerSize = listener.getFullDiameter();
		int size = listener.getDiameter();
		int tileSize = mapScale + mapPadding;
		if (mapPadding != 0) {
			int mapRenderCenterSize = centerSize * tileSize - mapPadding;
			int radius = mapRenderCenterSize / 2 + 1;
			addRect(graphics, mapX - radius, mapY - radius, mapX - radius + 1, mapY + radius, DEFAULT_STATUS_COLOR);
			addRect(graphics, mapX + radius - 1, mapY - radius, mapX + radius, mapY + radius, DEFAULT_STATUS_COLOR);
			addRect(graphics, mapX - radius, mapY - radius, mapX + radius, mapY - radius + 1, DEFAULT_STATUS_COLOR);
			addRect(graphics, mapX - radius, mapY + radius - 1, mapX + radius, mapY + radius, DEFAULT_STATUS_COLOR);
		}

		int mapRenderSize = size * tileSize - mapPadding;
		int mapStartX = mapX - mapRenderSize / 2;
		int mapStartY = mapY - mapRenderSize / 2;
		ChunkStatus prevStatus = null;
		int prevColor = NULL_STATUS_COLOR;

		for (int x = 0; x < size; x++) {
			int tileX = mapStartX + x * tileSize;

			for (int z = 0; z < size; z++) {
				int tileY = mapStartY + z * tileSize;
				ChunkStatus status = listener.getStatus(x, z);
				int color;
				if (prevStatus == status) {
					color = prevColor;
				} else {
					color = STATUS_TO_COLOR_FAST.getInt(status);
					prevStatus = status;
					prevColor = color;
				}

				addRect(graphics, tileX, tileY, tileX + mapScale, tileY + mapScale, color);
			}
		}
	}

	private static void addRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
		graphics.fill(x1, y1, x2, y2, color);
	}

}
