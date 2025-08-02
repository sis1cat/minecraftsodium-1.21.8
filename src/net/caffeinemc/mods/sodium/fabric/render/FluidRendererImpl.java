package net.caffeinemc.mods.sodium.fabric.render;

import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.quad.blender.BlendedColorProvider;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.services.FluidRendererFactory;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRendering;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRendering.DefaultRenderer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class FluidRendererImpl extends FluidRenderer {
   private final ColorProviderRegistry colorProviderRegistry;
   private final DefaultFluidRenderer defaultRenderer;
   private final FluidRendererImpl.DefaultRenderContext defaultContext;

   public FluidRendererImpl(ColorProviderRegistry colorProviderRegistry, LightPipelineProvider lighters) {
      this.colorProviderRegistry = colorProviderRegistry;
      this.defaultRenderer = new DefaultFluidRenderer(lighters);
      this.defaultContext = new FluidRendererImpl.DefaultRenderContext();
   }

   @Override
   public void render(
      LevelSlice level,
      BlockState blockState,
      FluidState fluidState,
      BlockPos blockPos,
      BlockPos offset,
      TranslucentGeometryCollector collector,
      ChunkBuildBuffers buffers
   ) {
      Material material = DefaultMaterials.forFluidState(fluidState);
      ChunkModelBuilder meshBuilder = buffers.get(material);
      FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getType());
      boolean hasModOverride = FluidRenderHandlerRegistry.INSTANCE.getOverride(fluidState.getType()) != null;
      if (handler == null) {
         boolean isLava = fluidState.is(FluidTags.LAVA);
         handler = FluidRenderHandlerRegistry.INSTANCE.get(isLava ? Fluids.LAVA : Fluids.WATER);
      }

      this.defaultContext
         .setUp(
            this.colorProviderRegistry,
            this.defaultRenderer,
            level,
            blockState,
            fluidState,
            blockPos,
            offset,
            collector,
            meshBuilder,
            material,
            handler,
            hasModOverride
         );

      try {
         FluidRendering.render(handler, level, blockPos, meshBuilder.asFallbackVertexConsumer(material, collector), blockState, fluidState, blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.defaultContext);
      } finally {
         this.defaultContext.clear();
      }
   }

   private static class DefaultRenderContext implements DefaultRenderer {
      private DefaultFluidRenderer renderer;
      private LevelSlice level;
      private BlockState blockState;
      private FluidState fluidState;
      private BlockPos blockPos;
      private BlockPos offset;
      private TranslucentGeometryCollector collector;
      private ChunkModelBuilder meshBuilder;
      private Material material;
      private FluidRenderHandler handler;
      private ColorProviderRegistry colorProviderRegistry;
      private boolean hasModOverride;

      public void setUp(
         ColorProviderRegistry colorProviderRegistry,
         DefaultFluidRenderer renderer,
         LevelSlice level,
         BlockState blockState,
         FluidState fluidState,
         BlockPos blockPos,
         BlockPos offset,
         TranslucentGeometryCollector collector,
         ChunkModelBuilder meshBuilder,
         Material material,
         FluidRenderHandler handler,
         boolean hasModOverride
      ) {
         this.colorProviderRegistry = colorProviderRegistry;
         this.renderer = renderer;
         this.level = level;
         this.blockState = blockState;
         this.fluidState = fluidState;
         this.blockPos = blockPos;
         this.offset = offset;
         this.collector = collector;
         this.meshBuilder = meshBuilder;
         this.material = material;
         this.handler = handler;
         this.hasModOverride = hasModOverride;
      }

      public void clear() {
         this.renderer = null;
         this.level = null;
         this.blockState = null;
         this.fluidState = null;
         this.blockPos = null;
         this.offset = null;
         this.collector = null;
         this.meshBuilder = null;
         this.material = null;
         this.handler = null;
         this.hasModOverride = false;
      }

      public ColorProvider<FluidState> getColorProvider(Fluid fluid) {
         ColorProvider<FluidState> override = this.colorProviderRegistry.getColorProvider(fluid);
         return !this.hasModOverride && override != null ? override : FabricColorProviders.adapt(this.handler);
      }

      public void render(FluidRenderHandler handler, BlockAndTintGetter world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
         this.renderer
            .render(
               this.level,
               this.blockState,
               this.fluidState,
               this.blockPos,
               this.offset,
               this.collector,
               this.meshBuilder,
               this.material,
               this.getColorProvider(fluidState.getType()),
               handler.getFluidSprites(this.level, this.blockPos, this.fluidState)
            );
      }
   }

   public static class FabricFactory implements FluidRendererFactory {
      @Override
      public FluidRenderer createPlatformFluidRenderer(ColorProviderRegistry colorRegistry, LightPipelineProvider lightPipelineProvider) {
         return new FluidRendererImpl(colorRegistry, lightPipelineProvider);
      }

      @Override
      public BlendedColorProvider<FluidState> getWaterColorProvider() {
         return new BlendedColorProvider<FluidState>() {
            protected int getColor(LevelSlice slice, FluidState state, BlockPos pos) {
               return BiomeColors.getAverageWaterColor(slice, pos) | 0xFF000000;
            }
         };
      }

      @Override
      public BlendedColorProvider<BlockState> getWaterBlockColorProvider() {
         return new BlendedColorProvider<BlockState>() {
            protected int getColor(LevelSlice slice, BlockState state, BlockPos pos) {
               return BiomeColors.getAverageWaterColor(slice, pos) | 0xFF000000;
            }
         };
      }
   }
}
