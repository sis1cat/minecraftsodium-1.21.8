package net.caffeinemc.mods.sodium.client.render.frapi.render;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.SingleBlockLightDataCache;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class NonTerrainBlockRenderContext extends AbstractBlockRenderContext {
   public static final ThreadLocal<NonTerrainBlockRenderContext> POOL = ThreadLocal.withInitial(NonTerrainBlockRenderContext::new);
   private BlockColors colorMap;
   private final SingleBlockLightDataCache lightDataCache = new SingleBlockLightDataCache();
   private BlockVertexConsumerProvider vertexConsumer;
   private Matrix4f matPosition;
   private boolean trustedNormals;
   private Matrix3f matNormal;
   private int overlay;

   public NonTerrainBlockRenderContext() {
      this.lighters = new LightPipelineProvider(this.lightDataCache);
      this.random = new SingleThreadedRandomSource(42L);
   }

   public void renderModel(
      BlockAndTintGetter blockView,
      BlockColors blockColors,
      BlockStateModel model,
      BlockState state,
      BlockPos pos,
      PoseStack poseStack,
      BlockVertexConsumerProvider buffer,
      boolean cull,
      long seed,
      int overlay
   ) {
      this.level = blockView;
      this.state = state;
      this.pos = pos;
      this.colorMap = blockColors;
      this.vertexConsumer = buffer;
      this.matPosition = poseStack.last().pose();
      this.trustedNormals = poseStack.last().trustedNormals;
      this.matNormal = poseStack.last().normal();
      this.overlay = overlay;
      this.defaultRenderType = ItemBlockRenderTypes.getChunkRenderType(state);
      this.lightDataCache.reset(pos, blockView);
      this.prepareCulling(cull);
      this.random.setSeed(seed);
      ((FabricBlockStateModel)model).emitQuads(this.getEmitter(), blockView, pos, state, this.random, this::isFaceCulled);
      this.defaultRenderType = null;
      this.level = null;
      this.lightDataCache.release();
      this.vertexConsumer = null;
   }

   @Override
   protected void processQuad(MutableQuadViewImpl quad) {
      TriState aoMode = quad.ambientOcclusion();
      ShadeMode shadeMode = quad.shadeMode();
      LightMode lightMode;
      if (aoMode == TriState.DEFAULT) {
         lightMode = this.defaultLightMode;
      } else {
         lightMode = this.useAmbientOcclusion && aoMode.get() ? LightMode.SMOOTH : LightMode.FLAT;
      }

      boolean emissive = quad.emissive();
      VertexConsumer vertexConsumer = this.getVertexConsumer(quad.renderLayer());
      this.tintQuad(quad);
      this.shadeQuad(quad, lightMode, emissive, shadeMode);
      this.bufferQuad(quad, vertexConsumer);
   }

   private VertexConsumer getVertexConsumer(ChunkSectionLayer blendMode) {
      return this.vertexConsumer.getBuffer(blendMode == null ? this.defaultRenderType : blendMode);
   }

   private RenderType toRenderLayer(ChunkSectionLayer defaultRenderType) {
      return switch (defaultRenderType) {
         case SOLID -> RenderType.solid();
         case CUTOUT_MIPPED -> RenderType.cutoutMipped();
         case CUTOUT -> RenderType.cutout();
         case TRANSLUCENT -> RenderType.translucentMovingBlock();
         case TRIPWIRE -> RenderType.tripwire();
         default -> throw new MatchException(null, null);
      };
   }

   private void tintQuad(MutableQuadViewImpl quad) {
      if (quad.tintIndex() != -1) {
         int blockColor = 0xFF000000 | this.colorMap.getColor(this.state, this.level, this.pos, quad.tintIndex());

         for (int i = 0; i < 4; i++) {
            quad.color(i, ColorMixer.mulComponentWise(blockColor, quad.color(i)));
         }
      }
   }

   @Override
   protected void shadeQuad(MutableQuadViewImpl quad, LightMode lightMode, boolean emissive, ShadeMode shadeMode) {
      super.shadeQuad(quad, lightMode, emissive, shadeMode);
      float[] brightnesses = this.quadLightData.br;

      for (int i = 0; i < 4; i++) {
         quad.color(i, ColorARGB.mulRGB(quad.color(i), brightnesses[i]));
      }
   }

   private void bufferQuad(MutableQuadViewImpl quad, VertexConsumer vertexConsumer) {
      QuadEncoder.writeQuadVertices(quad, vertexConsumer, this.overlay, this.matPosition, this.trustedNormals, this.matNormal);
      TextureAtlasSprite sprite = quad.sprite(SpriteFinderCache.forBlockAtlas());
      if (sprite != null) {
         SpriteUtil.INSTANCE.markSpriteActive(sprite);
      }
   }
}
