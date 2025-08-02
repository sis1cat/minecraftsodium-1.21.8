package net.caffeinemc.mods.sodium.client.services;

import java.util.List;
import java.util.function.Function;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.caffeinemc.mods.sodium.fabric.level.FabricLevelRenderHooks;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.network.chat.SignedMessageChain;
import org.joml.Matrix4f;

public interface PlatformLevelRenderHooks {
   PlatformLevelRenderHooks INSTANCE = new FabricLevelRenderHooks();

   static PlatformLevelRenderHooks getInstance() {
      return INSTANCE;
   }

   void runChunkLayerEvents(RenderType var1, Level var2, LevelRenderer var3, Matrix4f var4, Matrix4f var5, int var6, Camera var7, Frustum var8);

   List<?> retrieveChunkMeshAppenders(Level var1, BlockPos var2);

   void runChunkMeshAppenders(List<?> var1, Function<ChunkSectionLayer, VertexConsumer> var2, LevelSlice var3);
}
