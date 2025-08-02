package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.client.compatibility.workarounds.Workarounds;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.color.ColorProviderRegistry;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.MaterialParameters;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.texture.SpriteFinderCache;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BlockRenderer extends AbstractBlockRenderContext {
   private final ColorProviderRegistry colorProviderRegistry;
   private final int[] vertexColors = new int[4];
   private final ChunkVertexEncoder.Vertex[] vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();
   private ChunkBuildBuffers buffers;
   private final Vector3f posOffset = new Vector3f();
   private final MutableBlockPos scratchPos = new MutableBlockPos();
   @Nullable
   private ColorProvider<BlockState> colorProvider;
   private TranslucentGeometryCollector collector;

   public BlockRenderer(ColorProviderRegistry colorRegistry, LightPipelineProvider lighters) {
      this.colorProviderRegistry = colorRegistry;
      this.lighters = lighters;
      this.random = new SingleThreadedRandomSource(42L);
   }

   public void prepare(ChunkBuildBuffers buffers, LevelSlice level, TranslucentGeometryCollector collector) {
      this.buffers = buffers;
      this.level = level;
      this.collector = collector;
      this.slice = level;
   }

   public void release() {
      this.buffers = null;
      this.level = null;
      this.collector = null;
      this.slice = null;
   }

   public void renderModel(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin) {
      this.state = state;
      this.pos = pos;
      this.prepareAoInfo(true);
      this.posOffset.set(origin.getX(), origin.getY(), origin.getZ());
      if (state.hasOffsetFunction()) {
         Vec3 modelOffset = state.getOffset(pos);
         this.posOffset.add((float)modelOffset.x, (float)modelOffset.y, (float)modelOffset.z);
      }

      this.colorProvider = this.colorProviderRegistry.getColorProvider(state.getBlock());
      this.prepareCulling(true);
      this.defaultRenderType = ItemBlockRenderTypes.getChunkRenderType(state);
      this.allowDowngrade = true;
      this.random.setSeed(state.getSeed(pos));
      ((FabricBlockStateModel)model).emitQuads(this.getEmitter(), this.level, pos, state, this.random, this::isFaceCulled);
      this.defaultRenderType = null;
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
      ChunkSectionLayer blendMode = quad.renderLayer();
      Material material = DefaultMaterials.forChunkLayer(blendMode == null ? this.defaultRenderType : blendMode);
      this.tintQuad(quad);
      this.shadeQuad(quad, lightMode, emissive, shadeMode);
      this.bufferQuad(quad, this.quadLightData.br, material);
   }

   private void tintQuad(MutableQuadViewImpl quad) {
      int tintIndex = quad.tintIndex();
      if (tintIndex != -1) {
         ColorProvider<BlockState> colorProvider = this.colorProvider;
         if (colorProvider != null) {
            int[] vertexColors = this.vertexColors;
            colorProvider.getColors(this.slice, this.pos, this.scratchPos, this.state, quad, vertexColors);

            for (int i = 0; i < 4; i++) {
               quad.color(i, ColorMixer.mulComponentWise(vertexColors[i], quad.color(i)));
            }
         }
      }
   }

   private void bufferQuad(MutableQuadViewImpl quad, float[] brightnesses, Material material) {
      ModelQuadOrientation orientation = ModelQuadOrientation.NORMAL;
      ChunkVertexEncoder.Vertex[] vertices = this.vertices;
      Vector3f offset = this.posOffset;

      for (int dstIndex = 0; dstIndex < 4; dstIndex++) {
         int srcIndex = orientation.getVertexIndex(dstIndex);
         ChunkVertexEncoder.Vertex out = vertices[dstIndex];
         out.x = quad.x(srcIndex) + offset.x;
         out.y = quad.y(srcIndex) + offset.y;
         out.z = quad.z(srcIndex) + offset.z;
         out.color = ColorARGB.toABGR(quad.color(srcIndex));
         out.ao = brightnesses[srcIndex];
         out.u = quad.u(srcIndex);
         out.v = quad.v(srcIndex);
         out.light = quad.lightmap(srcIndex);
      }

      TextureAtlasSprite atlasSprite = quad.sprite(SpriteFinderCache.forBlockAtlas());
      int materialBits = material.bits();
      ModelQuadFacing normalFace = quad.normalFace();
      TerrainRenderPass pass = material.pass;
      TerrainRenderPass downgradedPass = this.attemptPassDowngrade(atlasSprite, pass);
      if (downgradedPass != null) {
         pass = downgradedPass;
      }

      if (pass.isTranslucent() && this.collector != null) {
         this.collector.appendQuad(quad.getFaceNormal(), vertices, normalFace);
      }

      if (downgradedPass != null && material == DefaultMaterials.TRANSLUCENT && pass == DefaultTerrainRenderPasses.CUTOUT) {
         materialBits = MaterialParameters.pack(AlphaCutoffParameter.ONE_TENTH, material.mipped);
      }

      ChunkModelBuilder builder = this.buffers.get(pass);
      ChunkMeshBufferBuilder vertexBuffer = builder.getVertexBuffer(normalFace);
      vertexBuffer.push(vertices, materialBits);
      if (atlasSprite != null) {
         builder.addSprite(atlasSprite);
      }
   }

   private boolean validateQuadUVs(TextureAtlasSprite atlasSprite) {
      float spriteUMin = atlasSprite.getU0();
      float spriteUMax = atlasSprite.getU1();
      float spriteVMin = atlasSprite.getV0();
      float spriteVMax = atlasSprite.getV1();

      for (int i = 0; i < 4; i++) {
         float u = this.vertices[i].u;
         float v = this.vertices[i].v;
         if (u < spriteUMin || u > spriteUMax || v < spriteVMin || v > spriteVMax) {
            return false;
         }
      }

      return true;
   }

   @Nullable
   private TerrainRenderPass attemptPassDowngrade(TextureAtlasSprite sprite, TerrainRenderPass pass) {
      if (this.allowDowngrade && !Workarounds.isWorkaroundEnabled(Workarounds.Reference.INTEL_DEPTH_BUFFER_COMPARISON_UNRELIABLE)) {
         boolean attemptDowngrade = true;
         boolean hasNonOpaqueVertex = false;

         for (int i = 0; i < 4; i++) {
            hasNonOpaqueVertex |= ColorABGR.unpackAlpha(this.vertices[i].color) != 255;
         }

         if (pass.isTranslucent() && hasNonOpaqueVertex) {
            attemptDowngrade = false;
         }

         if (attemptDowngrade) {
            attemptDowngrade = this.validateQuadUVs(sprite);
         }

         return attemptDowngrade ? getDowngradedPass(sprite, pass) : null;
      } else {
         return null;
      }
   }

   private static TerrainRenderPass getDowngradedPass(TextureAtlasSprite sprite, TerrainRenderPass pass) {
      if (sprite instanceof TextureAtlasSpriteExtension spriteExt) {
         if (spriteExt.sodium$hasUnknownImageContents()) {
            return pass;
         }

         if (sprite.contents() instanceof SpriteContentsExtension contentsExt) {
            if (pass == DefaultTerrainRenderPasses.TRANSLUCENT && !contentsExt.sodium$hasTranslucentPixels()) {
               pass = DefaultTerrainRenderPasses.CUTOUT;
            }

            if (pass == DefaultTerrainRenderPasses.CUTOUT && !contentsExt.sodium$hasTransparentPixels()) {
               pass = DefaultTerrainRenderPasses.SOLID;
            }
         }
      }

      return pass;
   }
}
