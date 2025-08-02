package net.caffeinemc.mods.sodium.client.render.chunk.terrain;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;


public class DefaultTerrainRenderPasses {
   public static final TerrainRenderPass SOLID = new TerrainRenderPass(RenderPipelines.SOLID, false, false, true);
   public static final TerrainRenderPass CUTOUT = new TerrainRenderPass(RenderPipelines.CUTOUT_MIPPED, false, true, true);
   public static final TerrainRenderPass TRANSLUCENT = new TerrainRenderPass(RenderPipelines.TRANSLUCENT, true, false, true);
   public static final TerrainRenderPass[] ALL = new TerrainRenderPass[]{SOLID, CUTOUT, TRANSLUCENT};
}
