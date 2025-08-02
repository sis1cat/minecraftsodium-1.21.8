package net.caffeinemc.mods.sodium.client.render.frapi.mesh;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.model.quad.BakedQuadView;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.TextureHelper;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FilterMask.Type;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import org.jetbrains.annotations.Nullable;

public abstract class MutableQuadViewImpl extends QuadViewImpl implements QuadEmitter {
   @Nullable
   private TextureAtlasSprite cachedSprite;
   protected static final QuadTransform NO_TRANSFORM = q -> true;
   protected QuadTransform activeTransform = NO_TRANSFORM;
   private final ObjectArrayList<QuadTransform> transformStack = new ObjectArrayList();
   private final QuadTransform stackTransform = q -> {
      int i = this.transformStack.size() - 1;

      while (i >= 0) {
         if (!((QuadTransform)this.transformStack.get(i--)).transform(q)) {
            return false;
         }
      }

      return true;
   };
   static final int[] DEFAULT = (int[])EncodingFormat.EMPTY.clone();

   @Nullable
   public TextureAtlasSprite cachedSprite() {
      return this.cachedSprite;
   }

   public void cachedSprite(@Nullable TextureAtlasSprite sprite) {
      this.cachedSprite = sprite;
   }

   public TextureAtlasSprite sprite(SpriteFinder finder) {
      TextureAtlasSprite sprite = this.cachedSprite;
      if (sprite == null) {
         this.cachedSprite = sprite = finder.find(this);
      }

      return sprite;
   }

   public void clear() {
      System.arraycopy(DEFAULT, 0, this.data, this.baseIndex, EncodingFormat.TOTAL_STRIDE);
      this.isGeometryInvalid = true;
      this.nominalFace = null;
      this.cachedSprite(null);
   }

   @Override
   public void load() {
      super.load();
      this.cachedSprite(null);
   }

   public MutableQuadViewImpl pos(int vertexIndex, float x, float y, float z) {
      int index = this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_X;
      this.data[index] = Float.floatToRawIntBits(x);
      this.data[index + 1] = Float.floatToRawIntBits(y);
      this.data[index + 2] = Float.floatToRawIntBits(z);
      this.isGeometryInvalid = true;
      return this;
   }

   public MutableQuadViewImpl color(int vertexIndex, int color) {
      this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_COLOR] = color;
      return this;
   }

   public MutableQuadViewImpl uv(int vertexIndex, float u, float v) {
      int i = this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_U;
      this.data[i] = Float.floatToRawIntBits(u);
      this.data[i + 1] = Float.floatToRawIntBits(v);
      this.cachedSprite(null);
      return this;
   }

   public MutableQuadViewImpl spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
      TextureHelper.bakeSprite(this, sprite, bakeFlags);
      this.cachedSprite(sprite);
      return this;
   }

   public MutableQuadViewImpl lightmap(int vertexIndex, int lightmap) {
      this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_LIGHTMAP] = lightmap;
      return this;
   }

   protected void normalFlags(int flags) {
      this.data[this.baseIndex + 0] = EncodingFormat.normalFlags(this.data[this.baseIndex + 0], flags);
   }

   public MutableQuadViewImpl normal(int vertexIndex, float x, float y, float z) {
      this.normalFlags(this.normalFlags() | 1 << vertexIndex);
      this.data[this.baseIndex + vertexIndex * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_NORMAL] = NormI8.pack(x, y, z);
      return this;
   }

   public void pushTransform(QuadTransform transform) {
      if (transform == null) {
         throw new NullPointerException("QuadTransform cannot be null!");
      } else {
         this.transformStack.push(transform);
         if (this.transformStack.size() == 1) {
            this.activeTransform = transform;
         } else if (this.transformStack.size() == 2) {
            this.activeTransform = this.stackTransform;
         }
      }
   }

   public void popTransform() {
      this.transformStack.pop();
      if (this.transformStack.isEmpty()) {
         this.activeTransform = NO_TRANSFORM;
      } else if (this.transformStack.size() == 1) {
         this.activeTransform = (QuadTransform)this.transformStack.getFirst();
      }
   }

   public final void populateMissingNormals() {
      int normalFlags = this.normalFlags();
      if (normalFlags != 15) {
         int packedFaceNormal = this.packedFaceNormal();

         for (int v = 0; v < 4; v++) {
            if ((normalFlags & 1 << v) == 0) {
               this.data[this.baseIndex + v * EncodingFormat.VERTEX_STRIDE + EncodingFormat.VERTEX_NORMAL] = packedFaceNormal;
            }
         }

         this.normalFlags(15);
      }
   }

   public final MutableQuadViewImpl cullFace(@Nullable Direction face) {
      this.data[this.baseIndex + 0] = EncodingFormat.cullFace(this.data[this.baseIndex + 0], face);
      this.nominalFace(face);
      return this;
   }

   public final MutableQuadViewImpl nominalFace(@Nullable Direction face) {
      this.nominalFace = face;
      return this;
   }

   public MutableQuadViewImpl renderLayer(@Nullable ChunkSectionLayer renderLayer) {
      this.data[this.baseIndex + 0] = EncodingFormat.renderLayer(this.data[this.baseIndex + 0], renderLayer);
      return this;
   }

   public MutableQuadViewImpl emissive(boolean emissive) {
      this.data[this.baseIndex + 0] = EncodingFormat.emissive(this.data[this.baseIndex + 0], emissive);
      return this;
   }

   public MutableQuadViewImpl diffuseShade(boolean shade) {
      this.data[this.baseIndex + 0] = EncodingFormat.diffuseShade(this.data[this.baseIndex + 0], shade);
      return this;
   }

   public MutableQuadViewImpl ambientOcclusion(TriState ao) {
      Objects.requireNonNull(ao, "ambient occlusion TriState may not be null");
      this.data[this.baseIndex + 0] = EncodingFormat.ambientOcclusion(this.data[this.baseIndex + 0], ao);
      return this;
   }

   public MutableQuadViewImpl glint(@Nullable FoilType glint) {
      this.data[this.baseIndex + 0] = EncodingFormat.glint(this.data[this.baseIndex + 0], glint);
      return this;
   }

   public MutableQuadViewImpl shadeMode(ShadeMode mode) {
      Objects.requireNonNull(mode, "ShadeMode may not be null");
      this.data[this.baseIndex + 0] = EncodingFormat.shadeMode(this.data[this.baseIndex + 0], mode);
      return this;
   }

   public final MutableQuadViewImpl tintIndex(int tintIndex) {
      this.data[this.baseIndex + 2] = tintIndex;
      return this;
   }

   public final MutableQuadViewImpl tag(int tag) {
      this.data[this.baseIndex + 3] = tag;
      return this;
   }

   public MutableQuadViewImpl copyFrom(QuadView quad) {
      QuadViewImpl q = (QuadViewImpl)quad;
      System.arraycopy(q.data, q.baseIndex, this.data, this.baseIndex, EncodingFormat.TOTAL_STRIDE);
      this.nominalFace = q.nominalFace;
      this.isGeometryInvalid = q.isGeometryInvalid;
      if (!this.isGeometryInvalid) {
         this.faceNormal.set(q.faceNormal);
      }

      if (quad instanceof MutableQuadViewImpl mutableQuad) {
         this.cachedSprite(mutableQuad.cachedSprite());
      } else {
         this.cachedSprite(null);
      }

      return this;
   }

   private void fromVanillaInternal(int[] quadData, int startIndex) {
      System.arraycopy(quadData, startIndex, this.data, this.baseIndex + 4, QuadView.VANILLA_QUAD_STRIDE);
      int colorIndex = this.baseIndex + EncodingFormat.VERTEX_COLOR;

      for (int i = 0; i < 4; i++) {
         this.data[colorIndex] = ColorHelper.fromVanillaColor(this.data[colorIndex]);
         colorIndex += EncodingFormat.VERTEX_STRIDE;
      }
   }

   public final MutableQuadViewImpl fromVanilla(int[] quadData, int startIndex) {
      this.fromVanillaInternal(quadData, startIndex);
      this.isGeometryInvalid = true;
      this.cachedSprite(null);
      return this;
   }

   public final MutableQuadViewImpl fromBakedQuad(BakedQuad quad) {
      this.fromVanillaInternal(quad.vertices(), 0);
      this.nominalFace(quad.direction());
      this.diffuseShade(quad.shade());
      this.tintIndex(quad.tintIndex());
      this.ambientOcclusion(TriState.of(((BakedQuadView)quad).hasAO()));
      NormI8.unpack(((BakedQuadView) quad).getFaceNormal(), this.faceNormal);
      this.data[this.baseIndex + 1] = ((BakedQuadView) quad).getFaceNormal();
      int headerBits = EncodingFormat.lightFace(this.data[this.baseIndex], ((BakedQuadView) quad).getLightFace());
      headerBits = EncodingFormat.normalFace(headerBits, ((BakedQuadView) quad).getNormalFace());
      this.data[this.baseIndex] = EncodingFormat.geometryFlags(headerBits, ((BakedQuadView) quad).getFlags());
      this.isGeometryInvalid = false;
      int lightEmission = quad.lightEmission();
      if (lightEmission > 0) {
         for (int i = 0; i < 4; i++) {
            this.lightmap(i, LightTexture.lightCoordsWithEmission(this.lightmap(i), lightEmission));
         }
      }

      this.cachedSprite(quad.sprite());
      return this;
   }

   protected abstract void emitDirectly();

   public final void transformAndEmit() {
      if (this.activeTransform.transform(this)) {
         this.emitDirectly();
      }
   }

   public final MutableQuadViewImpl emit() {
      this.transformAndEmit();
      this.clear();
      return this;
   }

   static {
      MutableQuadViewImpl quad = new MutableQuadViewImpl() {
         @Override
         protected void emitDirectly() {
         }
      };
      quad.data = DEFAULT;
      quad.color(-1, -1, -1, -1);
      quad.cullFace(null);
      quad.renderLayer(null);
      quad.diffuseShade(true);
      quad.ambientOcclusion(TriState.DEFAULT);
      quad.glint(null);
      quad.tintIndex(-1);
   }
}
