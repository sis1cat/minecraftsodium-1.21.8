package net.caffeinemc.mods.sodium.client.services;

import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AmbientOcclusionMode;
import net.caffeinemc.mods.sodium.fabric.block.FabricBlockAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.network.chat.ChatTypeDecoration;

public interface PlatformBlockAccess {
   PlatformBlockAccess INSTANCE = new FabricBlockAccess();

   static PlatformBlockAccess getInstance() {
      return INSTANCE;
   }

   int getLightEmission(BlockState var1, BlockAndTintGetter var2, BlockPos var3);

   boolean shouldSkipRender(BlockGetter var1, BlockState var2, BlockState var3, BlockPos var4, BlockPos var5, Direction var6);

   boolean shouldShowFluidOverlay(BlockState var1, BlockAndTintGetter var2, BlockPos var3, FluidState var4);

   boolean platformHasBlockData();

   float getNormalVectorShade(ModelQuadView var1, BlockAndTintGetter var2, boolean var3);

   AmbientOcclusionMode usesAmbientOcclusion(BlockModelPart var1, BlockState var2, ChunkSectionLayer var3, BlockAndTintGetter var4, BlockPos var5);

   boolean shouldBlockEntityGlow(BlockEntity var1, LocalPlayer var2);

   boolean shouldOccludeFluid(Direction var1, BlockState var2, FluidState var3);
}
