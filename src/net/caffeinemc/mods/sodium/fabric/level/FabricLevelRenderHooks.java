package net.caffeinemc.mods.sodium.fabric.level;

import java.util.List;
import java.util.function.Function;
import net.caffeinemc.mods.sodium.client.services.PlatformLevelRenderHooks;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
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

public class FabricLevelRenderHooks implements PlatformLevelRenderHooks {
   @Override
   public void runChunkLayerEvents(
      RenderType renderLayer,
      Level level,
      LevelRenderer levelRenderer,
      Matrix4f modelMatrix,
      Matrix4f projectionMatrix,
      int ticks,
      Camera mainCamera,
      Frustum cullingFrustum
   ) {
   }

   @Override
   public List<?> retrieveChunkMeshAppenders(Level level, BlockPos origin) {
      return List.of();
   }

   @Override
   public void runChunkMeshAppenders(List<?> renderers, Function<ChunkSectionLayer, VertexConsumer> typeToConsumer, LevelSlice slice) {
   }
}
