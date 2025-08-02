package net.caffeinemc.mods.sodium.client.render.frapi.render;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.EncodingFormat;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MeshViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import com.mojang.math.MatrixUtil;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.util.datafix.fixes.ChestedHorsesInventoryZeroIndexingFix;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ItemRenderContext extends AbstractRenderContext {
   public static final ThreadLocal<ItemRenderContext> POOL = ThreadLocal.withInitial(ItemRenderContext::new);
   private static final long ITEM_RANDOM_SEED = 42L;
   private static final int GLINT_COUNT = FoilType.values().length;
   private final MutableQuadViewImpl editorQuad = new ItemRenderContext.ItemEmitter();
   private final RandomSource random = new SingleThreadedRandomSource(42L);
   private final Supplier<RandomSource> randomSupplier = () -> {
      this.random.setSeed(42L);
      return this.random;
   };
   private ItemDisplayContext transformMode;
   private PoseStack poseStack;
   private Matrix4f matPosition;
   private boolean trustedNormals;
   private Matrix3f matNormal;
   private MultiBufferSource bufferSource;
   private int lightmap;
   private int overlay;
   private int[] colors;
   private RenderType defaultLayer;
   private FoilType defaultGlint;
   private Pose specialGlintEntry;
   private final VertexConsumer[] vertexConsumerCache = new VertexConsumer[3 * GLINT_COUNT];

   @Override
   public QuadEmitter getEmitter() {
      this.editorQuad.clear();
      return this.editorQuad;
   }

   public void renderItem(
      ItemDisplayContext displayContext,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int lightmap,
      int overlay,
      int[] colors,
      List<BakedQuad> vanillaQuads,
      MeshViewImpl mesh,
      RenderType layer,
      FoilType glint
   ) {
      this.transformMode = displayContext;
      this.matPosition = poseStack.last().pose();
      this.poseStack = poseStack;
      this.trustedNormals = this.poseStack.last().trustedNormals;
      this.matNormal = this.poseStack.last().normal();
      this.bufferSource = bufferSource;
      this.lightmap = lightmap;
      this.overlay = overlay;
      this.colors = colors;
      this.defaultLayer = layer;
      this.defaultGlint = glint;
      this.bufferQuads(vanillaQuads, mesh);
      this.poseStack = null;
      this.bufferSource = null;
      this.colors = null;
      this.specialGlintEntry = null;
      Arrays.fill(this.vertexConsumerCache, null);
   }

   private void bufferQuads(List<BakedQuad> vanillaQuads, MeshViewImpl mesh) {
      QuadEmitter emitter = this.getEmitter();
      int vanillaQuadCount = vanillaQuads.size();

       for (BakedQuad q : vanillaQuads) {
           emitter.fromBakedQuad(q);
           emitter.emit();
       }

      mesh.outputTo(emitter);
   }

   private void renderQuad(MutableQuadViewImpl quad) {
      boolean emissive = quad.emissive();
      VertexConsumer vertexConsumer = this.getVertexConsumer(quad.renderLayer(), quad.glint());
      this.tintQuad(quad);
      this.shadeQuad(quad, emissive);
      this.bufferQuad(quad, vertexConsumer);
   }

   private void tintQuad(MutableQuadViewImpl quad) {
      int tintIndex = quad.tintIndex();
      if (tintIndex != -1 && tintIndex < this.colors.length) {
         int color = this.colors[tintIndex];

         for (int i = 0; i < 4; i++) {
            quad.color(i, ColorMixer.mulComponentWise(color, quad.color(i)));
         }
      }
   }

   private void shadeQuad(MutableQuadViewImpl quad, boolean emissive) {
      if (emissive) {
         for (int i = 0; i < 4; i++) {
            quad.lightmap(i, 15728880);
         }
      } else {
         int lightmap = this.lightmap;

         for (int i = 0; i < 4; i++) {
            quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), lightmap));
         }
      }
   }

   private void bufferQuad(MutableQuadViewImpl quad, VertexConsumer vertexConsumer) {
      QuadEncoder.writeQuadVertices(quad, vertexConsumer, this.overlay, this.matPosition, this.trustedNormals, this.matNormal);
      TextureAtlasSprite sprite = quad.sprite(SpriteFinderCache.forBlockAtlas());
      if (sprite != null) {
         SpriteUtil.INSTANCE.markSpriteActive(sprite);
      }
   }

   private VertexConsumer getVertexConsumer(@Nullable ChunkSectionLayer blendMode, @Nullable FoilType glintMode) {
      RenderType type;
      if (blendMode == null) {
         type = this.defaultLayer;
      } else {
         type = RenderLayerHelper.getEntityBlockLayer(blendMode);
      }

      FoilType glint;
      if (glintMode == null) {
         glint = this.defaultGlint;
      } else {
         glint = glintMode;
      }

      int cacheIndex;
      if (type == Sheets.translucentItemSheet()) {
         cacheIndex = 0;
      } else if (type == Sheets.cutoutBlockSheet()) {
         cacheIndex = GLINT_COUNT;
      } else {
         cacheIndex = 2 * GLINT_COUNT;
      }

      cacheIndex += glint.ordinal();
      VertexConsumer vertexConsumer = this.vertexConsumerCache[cacheIndex];
      if (vertexConsumer == null) {
         vertexConsumer = this.createVertexConsumer(type, glint);
         this.vertexConsumerCache[cacheIndex] = vertexConsumer;
      }

      return vertexConsumer;
   }

   private VertexConsumer createVertexConsumer(RenderType type, FoilType glint) {
      if (glint == FoilType.SPECIAL) {
         if (this.specialGlintEntry == null) {
            this.specialGlintEntry = this.poseStack.last().copy();
            if (this.transformMode == ItemDisplayContext.GUI) {
               MatrixUtil.mulComponentWise(this.specialGlintEntry.pose(), 0.5F);
            } else if (this.transformMode.firstPerson()) {
               MatrixUtil.mulComponentWise(this.specialGlintEntry.pose(), 0.75F);
            }
         }

         return ItemRenderer.getSpecialFoilBuffer(this.bufferSource, type, this.specialGlintEntry);
      } else {
         return ItemRenderer.getFoilBuffer(this.bufferSource, type, true, glint != FoilType.NONE);
      }
   }

   public class ItemEmitter extends MutableQuadViewImpl {
      public ItemEmitter() {
         this.data = new int[EncodingFormat.TOTAL_STRIDE];
         this.clear();
      }

      @Override
      public void emitDirectly() {
         ItemRenderContext.this.renderQuad(this);
      }

      public boolean hasTransforms() {
         return this.activeTransform != NO_TRANSFORM;
      }
   }

   @FunctionalInterface
   public interface VanillaModelBufferer {
      void accept(DirectStateAccess var1, int[] var2, int var3, int var4, PoseStack var5, VertexConsumer var6);
   }
}
