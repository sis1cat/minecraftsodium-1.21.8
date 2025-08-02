package net.caffeinemc.mods.sodium.client.render.frapi.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.EncodingFormat;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformModelAccess;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBlockRenderContext extends AbstractRenderContext {
   private final AbstractBlockRenderContext.BlockEmitter editorQuad = new AbstractBlockRenderContext.BlockEmitter();
   protected BlockAndTintGetter level;
   protected LevelSlice slice;
   protected BlockState state;
   protected BlockPos pos;
   protected ChunkSectionLayer defaultRenderType;
   protected boolean allowDowngrade;
   private final BlockOcclusionCache occlusionCache = new BlockOcclusionCache();
   private boolean enableCulling = true;
   private int cullCompletionFlags;
   private int cullResultFlags;
   protected RandomSource random;
   protected LightPipelineProvider lighters;
   protected final QuadLightData quadLightData = new QuadLightData();
   protected boolean useAmbientOcclusion;
   protected LightMode defaultLightMode;
   private List<BlockModelPart> parts = new ObjectArrayList();

   @Override
   public QuadEmitter getEmitter() {
      this.editorQuad.clear();
      return this.editorQuad;
   }

   public boolean isFaceCulled(@Nullable Direction face) {
      if (face != null && this.enableCulling) {
         int mask = 1 << face.get3DDataValue();
         if ((this.cullCompletionFlags & mask) == 0) {
            this.cullCompletionFlags |= mask;
            if (this.occlusionCache.shouldDrawSide(this.state, this.level, this.pos, face)) {
               this.cullResultFlags |= mask;
               return false;
            } else {
               return true;
            }
         } else {
            return (this.cullResultFlags & mask) == 0;
         }
      } else {
         return false;
      }
   }

   private void renderQuad(MutableQuadViewImpl quad) {
      if (!this.isFaceCulled(quad.cullFace())) {
         this.processQuad(quad);
      }
   }

   protected abstract void processQuad(MutableQuadViewImpl var1);

   protected void prepareCulling(boolean enableCulling) {
      this.enableCulling = enableCulling;
      this.cullCompletionFlags = 0;
      this.cullResultFlags = 0;
   }

   protected void prepareAoInfo(boolean modelAo) {
      this.useAmbientOcclusion = Minecraft.useAmbientOcclusion();
      this.defaultLightMode = this.useAmbientOcclusion
            && modelAo
            && this.state != null
            && PlatformBlockAccess.getInstance().getLightEmission(this.state, this.level, this.pos) == 0
         ? LightMode.SMOOTH
         : LightMode.FLAT;
   }

   protected void shadeQuad(MutableQuadViewImpl quad, LightMode lightMode, boolean emissive, ShadeMode shadeMode) {
      LightPipeline lighter = this.lighters.getLighter(lightMode);
      QuadLightData data = this.quadLightData;
      lighter.calculate(quad, this.pos, data, quad.cullFace(), quad.lightFace(), quad.hasShade(), shadeMode == ShadeMode.ENHANCED);
      if (emissive) {
         for (int i = 0; i < 4; i++) {
            quad.lightmap(i, 15728880);
         }
      } else {
         int[] lightmaps = data.lm;

         for (int i = 0; i < 4; i++) {
            quad.lightmap(i, ColorHelper.maxBrightness(quad.lightmap(i), lightmaps[i]));
         }
      }
   }

   public void bufferDefaultModel(BlockModelPart part, Predicate<Direction> cullTest) {
      MutableQuadViewImpl editorQuad = this.editorQuad;
      this.prepareAoInfo(part.useAmbientOcclusion());
      ChunkSectionLayer renderType = PlatformModelAccess.getInstance().getPartRenderType(part, this.state, this.defaultRenderType);
      ChunkSectionLayer defaultType = this.defaultRenderType;
      this.defaultRenderType = renderType;

      for (int i = 0; i <= 6; i++) {
         Direction cullFace = ModelHelper.faceFromIndex(i);
         if (!cullTest.test(cullFace)) {
            AmbientOcclusionMode ao = PlatformBlockAccess.getInstance().usesAmbientOcclusion(part, this.state, renderType, this.slice, this.pos);
            List<BakedQuad> quads = PlatformModelAccess.getInstance().getQuads(this.level, this.pos, part, this.state, cullFace, this.random, renderType);
            int count = quads.size();

            for (int j = 0; j < count; j++) {
               BakedQuad q = quads.get(j);
               editorQuad.fromBakedQuad(q);
               editorQuad.cullFace(cullFace);
               editorQuad.renderLayer(renderType);
               editorQuad.ambientOcclusion(ao.toTriState());
               editorQuad.transformAndEmit();
            }
         }
      }

      editorQuad.clear();
      this.defaultRenderType = defaultType;
   }

   public class BlockEmitter extends MutableQuadViewImpl {
      private final List<BlockModelPart> cachedList = new ObjectArrayList();

      public BlockEmitter() {
         this.data = new int[EncodingFormat.TOTAL_STRIDE];
         this.clear();
      }

      @Override
      public void emitDirectly() {
         AbstractBlockRenderContext.this.renderQuad(this);
      }

      public void markInvalidToDowngrade() {
         AbstractBlockRenderContext.this.allowDowngrade = false;
      }

      public void emitPart(BlockModelPart part, Predicate<Direction> cullTest) {
         AbstractBlockRenderContext.this.bufferDefaultModel(part, cullTest);
      }

      public List<BlockModelPart> cachedList() {
         return this.cachedList;
      }
   }
}
