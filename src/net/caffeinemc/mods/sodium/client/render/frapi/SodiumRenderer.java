package net.caffeinemc.mods.sodium.client.render.frapi;

import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableMeshImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.NonTerrainBlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.frapi.render.SimpleBlockRenderContext;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public class SodiumRenderer implements Renderer {
   public static final SodiumRenderer INSTANCE = new SodiumRenderer();

   private SodiumRenderer() {
   }

   public MutableMesh mutableMesh() {
      return new MutableMeshImpl();
   }

   public void render(
      ModelBlockRenderer modelBlockRenderer,
      BlockAndTintGetter blockView,
      BlockStateModel model,
      BlockState state,
      BlockPos pos,
      PoseStack poseStack,
      BlockVertexConsumerProvider multiBufferSource,
      boolean cull,
      long seed,
      int overlay
   ) {
      NonTerrainBlockRenderContext.POOL
         .get()
         .renderModel(
            blockView, modelBlockRenderer.blockColors, model, state, pos, poseStack, multiBufferSource, cull, seed, overlay
         );
   }

   public void render(
      Pose entry,
      BlockVertexConsumerProvider vertexConsumers,
      BlockStateModel model,
      float red,
      float green,
      float blue,
      int light,
      int overlay,
      BlockAndTintGetter blockView,
      BlockPos pos,
      BlockState state
   ) {
      SimpleBlockRenderContext.POOL.get().bufferModel(entry, vertexConsumers, model, red, green, blue, light, overlay, blockView, pos, state);
   }

   public void renderBlockAsEntity(
      BlockRenderDispatcher renderManager,
      BlockState state,
      PoseStack poseStack,
      MultiBufferSource multiBufferSource,
      int light,
      int overlay,
      BlockAndTintGetter blockView,
      BlockPos pos
   ) {
      RenderShape renderShape = state.getRenderShape();
      if (renderShape != RenderShape.INVISIBLE) {
         BlockStateModel model = renderManager.getBlockModel(state);
         int tint = Minecraft.getInstance().getBlockColors().getColor(state, null, null, 0);
         float red = (tint >> 16 & 0xFF) / 255.0F;
         float green = (tint >> 8 & 0xFF) / 255.0F;
         float blue = (tint & 0xFF) / 255.0F;
         FabricBlockModelRenderer.render(
            poseStack.last(),
            layer -> multiBufferSource.getBuffer(RenderLayerHelper.getEntityBlockLayer(layer)),
            model,
            red,
            green,
            blue,
            light,
            overlay,
            blockView,
            pos,
            state
         );
         renderManager
            .specialBlockModelRenderer
            .get()
            .renderByBlock(state.getBlock(), ItemDisplayContext.GUI, poseStack, multiBufferSource, light, overlay);
      }
   }

   public QuadEmitter getLayerRenderStateEmitter(LayerRenderState layer) {
      return layer.fabric_getMutableMesh().emitter();
   }
}
