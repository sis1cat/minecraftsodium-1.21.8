package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public enum ChunkSectionLayerGroup {
	OPAQUE(ChunkSectionLayer.SOLID, ChunkSectionLayer.CUTOUT_MIPPED, ChunkSectionLayer.CUTOUT),
	TRANSLUCENT(ChunkSectionLayer.TRANSLUCENT),
	TRIPWIRE(ChunkSectionLayer.TRIPWIRE);

	private final String label;
	private final ChunkSectionLayer[] layers;

	private ChunkSectionLayerGroup(final ChunkSectionLayer... chunkSectionLayers) {
		this.layers = chunkSectionLayers;
		this.label = this.toString().toLowerCase(Locale.ROOT);
	}

	public String label() {
		return this.label;
	}

	public ChunkSectionLayer[] layers() {
		return this.layers;
	}

	public RenderTarget outputTarget() {
		Minecraft minecraft = Minecraft.getInstance();

		RenderTarget renderTarget = switch (this) {
			case TRANSLUCENT -> minecraft.levelRenderer.getTranslucentTarget();
			case TRIPWIRE -> minecraft.levelRenderer.getWeatherTarget();
			default -> minecraft.getMainRenderTarget();
		};
		return renderTarget != null ? renderTarget : minecraft.getMainRenderTarget();
	}
}
