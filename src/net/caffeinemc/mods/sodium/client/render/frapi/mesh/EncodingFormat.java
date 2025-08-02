package net.caffeinemc.mods.sodium.client.render.frapi.mesh;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public final class EncodingFormat {
   static final int HEADER_BITS = 0;
   static final int HEADER_FACE_NORMAL = 1;
   static final int HEADER_TINT_INDEX = 2;
   static final int HEADER_TAG = 3;
   public static final int HEADER_STRIDE = 4;
   static final int VERTEX_X = 4;
   static final int VERTEX_Y = 5;
   static final int VERTEX_Z = 6;
   static final int VERTEX_COLOR = 7;
   static final int VERTEX_U = 8;
   static final int VERTEX_V = VERTEX_U + 1;
   static final int VERTEX_LIGHTMAP = 10;
   static final int VERTEX_NORMAL = 11;
   public static final int VERTEX_STRIDE;
   public static final int QUAD_STRIDE;
   public static final int QUAD_STRIDE_BYTES;
   public static final int TOTAL_STRIDE;
   static final int[] EMPTY;
   private static final int DIRECTION_COUNT = Direction.values().length;
   private static final int NULLABLE_DIRECTION_COUNT = DIRECTION_COUNT + 1;
   @Nullable
   private static final ChunkSectionLayer[] NULLABLE_BLOCK_RENDER_LAYERS = (ChunkSectionLayer[])ArrayUtils.add(ChunkSectionLayer.values(), null);
   private static final int NULLABLE_BLOCK_RENDER_LAYER_COUNT = NULLABLE_BLOCK_RENDER_LAYERS.length;
   private static final TriState[] TRI_STATES = TriState.values();
   private static final int TRI_STATE_COUNT = TRI_STATES.length;
   @Nullable
   private static final FoilType[] NULLABLE_GLINTS = (FoilType[])ArrayUtils.add(FoilType.values(), null);
   private static final int NULLABLE_GLINT_COUNT = NULLABLE_GLINTS.length;
   private static final ShadeMode[] SHADE_MODES = ShadeMode.values();
   private static final int SHADE_MODE_COUNT = SHADE_MODES.length;
   private static final int NULL_RENDER_LAYER_INDEX = NULLABLE_BLOCK_RENDER_LAYER_COUNT - 1;
   private static final int NULL_GLINT_INDEX = NULLABLE_GLINT_COUNT - 1;
   private static final int CULL_BIT_LENGTH = Mth.ceillog2(NULLABLE_DIRECTION_COUNT);
   private static final int LIGHT_BIT_LENGTH = Mth.ceillog2(DIRECTION_COUNT);
   private static final int NORMALS_BIT_LENGTH = 4;
   private static final int NORMAL_FACE_BIT_LENGTH = 3;
   private static final int GEOMETRY_BIT_LENGTH = 3;
   private static final int RENDER_LAYER_BIT_LENGTH = Mth.ceillog2(NULLABLE_BLOCK_RENDER_LAYER_COUNT);
   private static final int EMISSIVE_BIT_LENGTH = 1;
   private static final int DIFFUSE_BIT_LENGTH = 1;
   private static final int AO_BIT_LENGTH = Mth.ceillog2(TRI_STATE_COUNT);
   private static final int GLINT_BIT_LENGTH = Mth.ceillog2(NULLABLE_GLINT_COUNT);
   private static final int SHADE_MODE_BIT_LENGTH = Mth.ceillog2(SHADE_MODE_COUNT);
   private static final int CULL_BIT_OFFSET = 0;
   private static final int LIGHT_BIT_OFFSET = 0 + CULL_BIT_LENGTH;
   private static final int NORMAL_FACE_BIT_OFFSET = LIGHT_BIT_OFFSET + LIGHT_BIT_LENGTH;
   private static final int NORMALS_BIT_OFFSET = NORMAL_FACE_BIT_OFFSET + 3;
   private static final int GEOMETRY_BIT_OFFSET = NORMALS_BIT_OFFSET + 4;
   private static final int RENDER_LAYER_BIT_OFFSET = GEOMETRY_BIT_OFFSET + 3;
   private static final int EMISSIVE_BIT_OFFSET = RENDER_LAYER_BIT_OFFSET + RENDER_LAYER_BIT_LENGTH;
   private static final int DIFFUSE_BIT_OFFSET = EMISSIVE_BIT_OFFSET + 1;
   private static final int AO_BIT_OFFSET = DIFFUSE_BIT_OFFSET + 1;
   private static final int GLINT_BIT_OFFSET = AO_BIT_OFFSET + AO_BIT_LENGTH;
   private static final int SHADE_MODE_BIT_OFFSET = GLINT_BIT_OFFSET + GLINT_BIT_LENGTH;
   private static final int TOTAL_BIT_LENGTH = SHADE_MODE_BIT_OFFSET + SHADE_MODE_BIT_LENGTH;
   private static final int CULL_MASK = bitMask(CULL_BIT_LENGTH, 0);
   private static final int LIGHT_MASK = bitMask(LIGHT_BIT_LENGTH, LIGHT_BIT_OFFSET);
   private static final int NORMAL_FACE_MASK = bitMask(3, NORMAL_FACE_BIT_OFFSET);
   private static final int NORMALS_MASK = bitMask(4, NORMALS_BIT_OFFSET);
   private static final int GEOMETRY_MASK = bitMask(3, GEOMETRY_BIT_OFFSET);
   private static final int RENDER_LAYER_MASK = bitMask(RENDER_LAYER_BIT_LENGTH, RENDER_LAYER_BIT_OFFSET);
   private static final int EMISSIVE_MASK = bitMask(1, EMISSIVE_BIT_OFFSET);
   private static final int DIFFUSE_MASK = bitMask(1, DIFFUSE_BIT_OFFSET);
   private static final int AO_MASK = bitMask(AO_BIT_LENGTH, AO_BIT_OFFSET);
   private static final int GLINT_MASK = bitMask(GLINT_BIT_LENGTH, GLINT_BIT_OFFSET);
   private static final int SHADE_MODE_MASK = bitMask(SHADE_MODE_BIT_LENGTH, SHADE_MODE_BIT_OFFSET);

   private EncodingFormat() {
   }

   private static int bitMask(int bitLength, int bitOffset) {
      return (1 << bitLength) - 1 << bitOffset;
   }

   @Nullable
   static Direction cullFace(int bits) {
      return ModelHelper.faceFromIndex((bits & CULL_MASK) >>> 0);
   }

   static int cullFace(int bits, @Nullable Direction face) {
      return bits & ~CULL_MASK | ModelHelper.toFaceIndex(face) << 0;
   }

   static Direction lightFace(int bits) {
      return ModelHelper.faceFromIndex((bits & LIGHT_MASK) >>> LIGHT_BIT_OFFSET);
   }

   static int lightFace(int bits, Direction face) {
      return bits & ~LIGHT_MASK | ModelHelper.toFaceIndex(face) << LIGHT_BIT_OFFSET;
   }

   static int normalFlags(int bits) {
      return (bits & NORMALS_MASK) >>> NORMALS_BIT_OFFSET;
   }

   static int normalFlags(int bits, int normalFlags) {
      return bits & ~NORMALS_MASK | normalFlags << NORMALS_BIT_OFFSET & NORMALS_MASK;
   }

   static int geometryFlags(int bits) {
      return (bits & GEOMETRY_MASK) >>> GEOMETRY_BIT_OFFSET;
   }

   static int geometryFlags(int bits, int geometryFlags) {
      return bits & ~GEOMETRY_MASK | geometryFlags << GEOMETRY_BIT_OFFSET & GEOMETRY_MASK;
   }

   static ModelQuadFacing normalFace(int bits) {
      return ModelQuadFacing.values()[(bits & NORMAL_FACE_MASK) >>> NORMAL_FACE_BIT_OFFSET];
   }

   static int normalFace(int bits, ModelQuadFacing face) {
      return bits & ~NORMAL_FACE_MASK | face.ordinal() << NORMAL_FACE_BIT_OFFSET & NORMAL_FACE_MASK;
   }

   @Nullable
   static ChunkSectionLayer renderLayer(int bits) {
      return NULLABLE_BLOCK_RENDER_LAYERS[(bits & RENDER_LAYER_MASK) >>> RENDER_LAYER_BIT_OFFSET];
   }

   static int renderLayer(int bits, @Nullable ChunkSectionLayer renderLayer) {
      int index = renderLayer == null ? NULL_RENDER_LAYER_INDEX : renderLayer.ordinal();
      return bits & ~RENDER_LAYER_MASK | index << RENDER_LAYER_BIT_OFFSET;
   }

   static boolean emissive(int bits) {
      return (bits & EMISSIVE_MASK) != 0;
   }

   static int emissive(int bits, boolean emissive) {
      return emissive ? bits | EMISSIVE_MASK : bits & ~EMISSIVE_MASK;
   }

   static boolean diffuseShade(int bits) {
      return (bits & DIFFUSE_MASK) != 0;
   }

   static int diffuseShade(int bits, boolean shade) {
      return shade ? bits | DIFFUSE_MASK : bits & ~DIFFUSE_MASK;
   }

   static TriState ambientOcclusion(int bits) {
      return TRI_STATES[(bits & AO_MASK) >>> AO_BIT_OFFSET];
   }

   static int ambientOcclusion(int bits, TriState ao) {
      return bits & ~AO_MASK | ao.ordinal() << AO_BIT_OFFSET;
   }

   @Nullable
   static FoilType glint(int bits) {
      return NULLABLE_GLINTS[(bits & GLINT_MASK) >>> GLINT_BIT_OFFSET];
   }

   static int glint(int bits, @Nullable FoilType glint) {
      int index = glint == null ? NULL_GLINT_INDEX : glint.ordinal();
      return bits & ~GLINT_MASK | index << GLINT_BIT_OFFSET;
   }

   static ShadeMode shadeMode(int bits) {
      return SHADE_MODES[(bits & SHADE_MODE_MASK) >>> SHADE_MODE_BIT_OFFSET];
   }

   static int shadeMode(int bits, ShadeMode mode) {
      return bits & ~SHADE_MODE_MASK | mode.ordinal() << SHADE_MODE_BIT_OFFSET;
   }

   static {
      VertexFormat format = DefaultVertexFormat.BLOCK;
      VERTEX_STRIDE = format.getVertexSize() / 4;
      QUAD_STRIDE = VERTEX_STRIDE * 4;
      QUAD_STRIDE_BYTES = QUAD_STRIDE * 4;
      TOTAL_STRIDE = 4 + QUAD_STRIDE;
      Preconditions.checkState(
         VERTEX_STRIDE == QuadView.VANILLA_VERTEX_STRIDE,
         "Indigo vertex stride (%s) mismatched with rendering API (%s)",
         VERTEX_STRIDE,
         QuadView.VANILLA_VERTEX_STRIDE
      );
      Preconditions.checkState(
         QUAD_STRIDE == QuadView.VANILLA_QUAD_STRIDE, "Indigo quad stride (%s) mismatched with rendering API (%s)", QUAD_STRIDE, QuadView.VANILLA_QUAD_STRIDE
      );
      EMPTY = new int[TOTAL_STRIDE];
      Preconditions.checkArgument(TOTAL_BIT_LENGTH <= 32, "Indigo header encoding bit count (%s) exceeds integer bit length)", TOTAL_STRIDE);
   }
}
