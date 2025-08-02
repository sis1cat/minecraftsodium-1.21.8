package net.caffeinemc.mods.sodium.client.render.frapi.render;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ARGB;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import org.jetbrains.annotations.Nullable;

public class SimpleBlockRenderContext extends AbstractBlockRenderContext {
   public static final ThreadLocal<SimpleBlockRenderContext> POOL = ThreadLocal.withInitial(SimpleBlockRenderContext::new);
   private final RandomSource random = RandomSource.createNewThreadLocalInstance();
   private BlockVertexConsumerProvider vertexConsumers;
   private float red;
   private float green;
   private float blue;
   private int light;
   @Nullable
   private ChunkSectionLayer lastRenderLayer;
   @Nullable
   private VertexConsumer lastVertexConsumer;
   private Pose matrices;
   private int overlay;

   @Override
   protected void processQuad(MutableQuadViewImpl quad) {
      ChunkSectionLayer quadRenderLayer = quad.renderLayer();
      ChunkSectionLayer renderLayer = quadRenderLayer == null ? this.defaultRenderType : quadRenderLayer;
      VertexConsumer vertexConsumer;
      if (renderLayer == this.lastRenderLayer) {
         vertexConsumer = this.lastVertexConsumer;
      } else {
         this.lastVertexConsumer = vertexConsumer = this.vertexConsumers.getBuffer(renderLayer);
         this.lastRenderLayer = renderLayer;
      }

      if (quad.tintIndex() != -1) {
         float red = this.red;
         float green = this.green;
         float blue = this.blue;

         for (int i = 0; i < 4; i++) {
            quad.color(i, ARGB.scaleRGB(quad.color(i), red, green, blue));
         }
      }

      if (quad.emissive()) {
         for (int i = 0; i < 4; i++) {
            quad.lightmap(i, 15728880);
         }
      } else {
         int light = this.light;

         for (int i = 0; i < 4; i++) {
            quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), light));
         }
      }

      QuadEncoder.writeQuadVertices(quad, vertexConsumer, this.overlay, this.matrices.pose(), this.matrices.trustedNormals, this.matrices.normal());
      SpriteUtil.INSTANCE.markSpriteActive(quad.sprite(SpriteFinderCache.forBlockAtlas()));
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

   public void bufferModel(
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
      this.matrices = entry;
      this.overlay = overlay;
      this.prepareAoInfo(true);
      this.vertexConsumers = vertexConsumers;
      this.defaultRenderType = ItemBlockRenderTypes.getChunkRenderType(state);
      this.red = Mth.clamp(red, 0.0F, 1.0F);
      this.green = Mth.clamp(green, 0.0F, 1.0F);
      this.blue = Mth.clamp(blue, 0.0F, 1.0F);
      this.light = light;
      this.level = blockView;
      this.state = state;
      this.pos = pos;
      this.random.setSeed(42L);
      model.emitQuads(this.getEmitter(), blockView, pos, state, this.random, cullFace -> false);
      this.level = null;
      this.state = null;
      this.pos = null;
      this.vertexConsumers = null;
      this.lastRenderLayer = null;
      this.lastVertexConsumer = null;
   }
}
