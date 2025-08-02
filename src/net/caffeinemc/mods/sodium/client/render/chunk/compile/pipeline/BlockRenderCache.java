package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.ArrayLightDataCache;
import net.caffeinemc.mods.sodium.client.services.FluidRendererFactory;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.world.level.Level;

public class BlockRenderCache {
   private final ArrayLightDataCache lightDataCache;
   private final BlockRenderer blockRenderer;
   private final FluidRenderer fluidRenderer;
   private final BlockModelShaper blockModels;
   private final LevelSlice levelSlice;

   public BlockRenderCache(Minecraft minecraft, Level level) {
      this.levelSlice = new LevelSlice(level);
      this.lightDataCache = new ArrayLightDataCache(this.levelSlice);
      LightPipelineProvider lightPipelineProvider = new LightPipelineProvider(this.lightDataCache);
      ColorProviderRegistry colorRegistry = new ColorProviderRegistry(minecraft.getBlockColors());
      this.blockRenderer = new BlockRenderer(colorRegistry, lightPipelineProvider);
      this.fluidRenderer = FluidRendererFactory.getInstance().createPlatformFluidRenderer(colorRegistry, lightPipelineProvider);
      this.blockModels = minecraft.getModelManager().getBlockModelShaper();
   }

   public BlockModelShaper getBlockModels() {
      return this.blockModels;
   }

   public BlockRenderer getBlockRenderer() {
      return this.blockRenderer;
   }

   public FluidRenderer getFluidRenderer() {
      return this.fluidRenderer;
   }

   public void init(ChunkRenderContext context) {
      this.lightDataCache.reset(context.getOrigin());
      this.levelSlice.copyData(context);
   }

   public LevelSlice getWorldSlice() {
      return this.levelSlice;
   }

   public void cleanup() {
      this.levelSlice.reset();
   }
}
