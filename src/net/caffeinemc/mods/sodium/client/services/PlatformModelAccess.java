package net.caffeinemc.mods.sodium.client.services;

import java.util.List;

import net.caffeinemc.mods.sodium.fabric.model.FabricModelAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface PlatformModelAccess {
   PlatformModelAccess INSTANCE = new FabricModelAccess();

   static PlatformModelAccess getInstance() {
      return INSTANCE;
   }

   List<BakedQuad> getQuads(BlockAndTintGetter var1, BlockPos var2, BlockModelPart var3, BlockState var4, Direction var5, RandomSource var6, ChunkSectionLayer var7);

   SodiumModelDataContainer getModelDataContainer(Level var1, SectionPos var2);

   @Internal
   SodiumModelData getEmptyModelData();

   ChunkSectionLayer getPartRenderType(BlockModelPart var1, BlockState var2, ChunkSectionLayer var3);

   List<BlockModelPart> collectPartsOf(BlockStateModel var1, BlockAndTintGetter var2, BlockPos var3, BlockState var4, RandomSource var5, QuadEmitter var6);
}
