package net.caffeinemc.mods.sodium.client.services;

import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.quad.blender.BlendedColorProvider;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer;
import net.caffeinemc.mods.sodium.fabric.render.FluidRendererImpl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface FluidRendererFactory {
   FluidRendererFactory INSTANCE = new FluidRendererImpl.FabricFactory();

   static FluidRendererFactory getInstance() {
      return INSTANCE;
   }

   FluidRenderer createPlatformFluidRenderer(ColorProviderRegistry var1, LightPipelineProvider var2);

   BlendedColorProvider<FluidState> getWaterColorProvider();

   BlendedColorProvider<BlockState> getWaterBlockColorProvider();
}
