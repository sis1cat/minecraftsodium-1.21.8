package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.LightPipelineProvider;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuad;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadViewMutable;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class DefaultFluidRenderer {
   public static final float EPSILON = 0.001F;
   private static final float ALIGNED_EQUALS_EPSILON = 0.011F;
   private final MutableBlockPos scratchPos = new MutableBlockPos();
   private final MutableFloat scratchHeight = new MutableFloat(0.0F);
   private final MutableInt scratchSamples = new MutableInt();
   private final BlockOcclusionCache occlusionCache = new BlockOcclusionCache();
   private final ModelQuadViewMutable quad = new ModelQuad();
   private final LightPipelineProvider lighters;
   private final QuadLightData quadLightData = new QuadLightData();
   private final int[] quadColors = new int[4];
   private final float[] brightness = new float[4];
   private final ChunkVertexEncoder.Vertex[] vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();

   public DefaultFluidRenderer(LightPipelineProvider lighters) {
      this.quad.setLightFace(Direction.UP);
      this.lighters = lighters;
   }

   private boolean isFullBlockFluidOccluded(BlockAndTintGetter world, BlockPos pos, Direction dir, BlockState blockState, FluidState fluid) {
      return !this.occlusionCache.shouldDrawFullBlockFluidSide(blockState, world, pos, dir, fluid, Shapes.block());
   }

   private boolean isSideExposed(BlockAndTintGetter world, int x, int y, int z, Direction dir, float height) {
      BlockPos pos = this.scratchPos.set(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ());
      BlockState blockState = world.getBlockState(pos);
      if (blockState.canOcclude()) {
         VoxelShape shape = blockState.getOcclusionShape();
         if (shape.isEmpty()) {
            return true;
         } else {
            VoxelShape threshold = Shapes.box(0.0, 0.0, 0.0, 1.0, height, 1.0);
            return !Shapes.blockOccludes(threshold, shape, dir);
         }
      } else {
         return true;
      }
   }

   public void render(
      LevelSlice level,
      BlockState blockState,
      FluidState fluidState,
      BlockPos blockPos,
      BlockPos offset,
      TranslucentGeometryCollector collector,
      ChunkModelBuilder meshBuilder,
      Material material,
      ColorProvider<FluidState> colorProvider,
      TextureAtlasSprite[] sprites
   ) {
      int posX = blockPos.getX();
      int posY = blockPos.getY();
      int posZ = blockPos.getZ();
      Fluid fluid = fluidState.getType();
      boolean cullUp = this.isFullBlockFluidOccluded(level, blockPos, Direction.UP, blockState, fluidState);
      boolean cullDown = this.isFullBlockFluidOccluded(level, blockPos, Direction.DOWN, blockState, fluidState)
         || !this.isSideExposed(level, posX, posY, posZ, Direction.DOWN, 0.8888889F);
      boolean cullNorth = this.isFullBlockFluidOccluded(level, blockPos, Direction.NORTH, blockState, fluidState);
      boolean cullSouth = this.isFullBlockFluidOccluded(level, blockPos, Direction.SOUTH, blockState, fluidState);
      boolean cullWest = this.isFullBlockFluidOccluded(level, blockPos, Direction.WEST, blockState, fluidState);
      boolean cullEast = this.isFullBlockFluidOccluded(level, blockPos, Direction.EAST, blockState, fluidState);
      if (!cullUp || !cullDown || !cullEast || !cullWest || !cullNorth || !cullSouth) {
         boolean isWater = fluidState.is(FluidTags.WATER);
         float fluidHeight = this.fluidHeight(level, fluid, blockPos, Direction.UP);
         float northWestHeight;
         float southWestHeight;
         float southEastHeight;
         float northEastHeight;
         if (fluidHeight >= 1.0F) {
            northWestHeight = 1.0F;
            southWestHeight = 1.0F;
            southEastHeight = 1.0F;
            northEastHeight = 1.0F;
         } else {
            MutableBlockPos scratchPos = new MutableBlockPos();
            float heightNorth = this.fluidHeight(level, fluid, scratchPos.setWithOffset(blockPos, Direction.NORTH), Direction.NORTH);
            float heightSouth = this.fluidHeight(level, fluid, scratchPos.setWithOffset(blockPos, Direction.SOUTH), Direction.SOUTH);
            float heightEast = this.fluidHeight(level, fluid, scratchPos.setWithOffset(blockPos, Direction.EAST), Direction.EAST);
            float heightWest = this.fluidHeight(level, fluid, scratchPos.setWithOffset(blockPos, Direction.WEST), Direction.WEST);
            northWestHeight = this.fluidCornerHeight(
               level,
               fluid,
               fluidHeight,
               heightNorth,
               heightWest,
               scratchPos.set(blockPos).move(Direction.NORTH).move(Direction.WEST)
            );
            southWestHeight = this.fluidCornerHeight(
               level,
               fluid,
               fluidHeight,
               heightSouth,
               heightWest,
               scratchPos.set(blockPos).move(Direction.SOUTH).move(Direction.WEST)
            );
            southEastHeight = this.fluidCornerHeight(
               level,
               fluid,
               fluidHeight,
               heightSouth,
               heightEast,
               scratchPos.set(blockPos).move(Direction.SOUTH).move(Direction.EAST)
            );
            northEastHeight = this.fluidCornerHeight(
               level,
               fluid,
               fluidHeight,
               heightNorth,
               heightEast,
               scratchPos.set(blockPos).move(Direction.NORTH).move(Direction.EAST)
            );
         }

         float yOffset = cullDown ? 0.0F : 0.001F;
         ModelQuadViewMutable quad = this.quad;
         LightMode lightMode = isWater && Minecraft.useAmbientOcclusion() ? LightMode.SMOOTH : LightMode.FLAT;
         LightPipeline lighter = this.lighters.getLighter(lightMode);
         quad.setFlags(0);
         if (!cullUp
            && this.isSideExposed(
               level,
               posX,
               posY,
               posZ,
               Direction.UP,
               Math.min(Math.min(northWestHeight, southWestHeight), Math.min(southEastHeight, northEastHeight))
            )) {
            northWestHeight -= 0.001F;
            southWestHeight -= 0.001F;
            southEastHeight -= 0.001F;
            northEastHeight -= 0.001F;
            Vec3 velocity = fluidState.getFlow(level, blockPos);
            TextureAtlasSprite sprite;
            float u1;
            float u2;
            float u3;
            float u4;
            float v1;
            float v2;
            float v3;
            float v4;
            if (velocity.x == 0.0 && velocity.z == 0.0) {
               sprite = sprites[0];
               u1 = sprite.getU(0.0F);
               v1 = sprite.getV(0.0F);
               u2 = u1;
               v2 = sprite.getV(1.0F);
               u3 = sprite.getU(1.0F);
               v3 = v2;
               u4 = u3;
               v4 = v1;
            } else {
               sprite = sprites[1];
               float dir = (float)Mth.atan2(velocity.z, velocity.x) - (float) (Math.PI / 2);
               float sin = Mth.sin(dir) * 0.25F;
               float cos = Mth.cos(dir) * 0.25F;
               u1 = sprite.getU(0.5F + (-cos - sin));
               v1 = sprite.getV(0.5F + -cos + sin);
               u2 = sprite.getU(0.5F + -cos + sin);
               v2 = sprite.getV(0.5F + cos + sin);
               u3 = sprite.getU(0.5F + cos + sin);
               v3 = sprite.getV(0.5F + (cos - sin));
               u4 = sprite.getU(0.5F + (cos - sin));
               v4 = sprite.getV(0.5F + (-cos - sin));
            }

            float uAvg = (u1 + u2 + u3 + u4) / 4.0F;
            float vAvg = (v1 + v2 + v3 + v4) / 4.0F;
            float s3 = sprites[0].uvShrinkRatio();
            u1 = Mth.lerp(s3, u1, uAvg);
            u2 = Mth.lerp(s3, u2, uAvg);
            u3 = Mth.lerp(s3, u3, uAvg);
            u4 = Mth.lerp(s3, u4, uAvg);
            v1 = Mth.lerp(s3, v1, vAvg);
            v2 = Mth.lerp(s3, v2, vAvg);
            v3 = Mth.lerp(s3, v3, vAvg);
            v4 = Mth.lerp(s3, v4, vAvg);
            quad.setSprite(sprite);
            boolean aligned = isAlignedEquals(northEastHeight, northWestHeight)
               && isAlignedEquals(northWestHeight, southEastHeight)
               && isAlignedEquals(southEastHeight, southWestHeight)
               && isAlignedEquals(southWestHeight, northEastHeight);
            boolean creaseNorthEastSouthWest = aligned
               || northEastHeight > northWestHeight && northEastHeight > southEastHeight
               || northEastHeight < northWestHeight && northEastHeight < southEastHeight
               || southWestHeight > northWestHeight && southWestHeight > southEastHeight
               || southWestHeight < northWestHeight && southWestHeight < southEastHeight;
            if (creaseNorthEastSouthWest) {
               setVertex(quad, 1, 0.0F, northWestHeight, 0.0F, u1, v1);
               setVertex(quad, 2, 0.0F, southWestHeight, 1.0F, u2, v2);
               setVertex(quad, 3, 1.0F, southEastHeight, 1.0F, u3, v3);
               setVertex(quad, 0, 1.0F, northEastHeight, 0.0F, u4, v4);
            } else {
               setVertex(quad, 0, 0.0F, northWestHeight, 0.0F, u1, v1);
               setVertex(quad, 1, 0.0F, southWestHeight, 1.0F, u2, v2);
               setVertex(quad, 2, 1.0F, southEastHeight, 1.0F, u3, v3);
               setVertex(quad, 3, 1.0F, northEastHeight, 0.0F, u4, v4);
            }

            this.updateQuad(quad, level, blockPos, lighter, Direction.UP, ModelQuadFacing.POS_Y, 1.0F, colorProvider, fluidState);
            this.writeQuad(meshBuilder, collector, material, offset, quad, aligned ? ModelQuadFacing.POS_Y : ModelQuadFacing.UNASSIGNED, false);
            if (fluidState.shouldRenderBackwardUpFace(level, this.scratchPos.set(posX, posY + 1, posZ))) {
               this.writeQuad(meshBuilder, collector, material, offset, quad, aligned ? ModelQuadFacing.NEG_Y : ModelQuadFacing.UNASSIGNED, true);
            }
         }

         if (!cullDown) {
            TextureAtlasSprite spritex = sprites[0];
            float minU = spritex.getU0();
            float maxU = spritex.getU1();
            float minV = spritex.getV0();
            float maxV = spritex.getV1();
            quad.setSprite(spritex);
            setVertex(quad, 0, 0.0F, yOffset, 1.0F, minU, maxV);
            setVertex(quad, 1, 0.0F, yOffset, 0.0F, minU, minV);
            setVertex(quad, 2, 1.0F, yOffset, 0.0F, maxU, minV);
            setVertex(quad, 3, 1.0F, yOffset, 1.0F, maxU, maxV);
            this.updateQuad(quad, level, blockPos, lighter, Direction.DOWN, ModelQuadFacing.NEG_Y, 1.0F, colorProvider, fluidState);
            this.writeQuad(meshBuilder, collector, material, offset, quad, ModelQuadFacing.NEG_Y, false);
         }

         quad.setFlags(6);

         for (Direction dir : DirectionUtil.HORIZONTAL_DIRECTIONS) {
            float c1;
            float c2;
            float x1;
            float z1;
            float x2;
            float z2;
            switch (dir) {
               case NORTH:
                  if (cullNorth) {
                     continue;
                  }

                  c1 = northWestHeight;
                  c2 = northEastHeight;
                  x1 = 0.0F;
                  x2 = 1.0F;
                  z1 = 0.001F;
                  z2 = z1;
                  break;
               case SOUTH:
                  if (cullSouth) {
                     continue;
                  }

                  c1 = southEastHeight;
                  c2 = southWestHeight;
                  x1 = 1.0F;
                  x2 = 0.0F;
                  z1 = 0.999F;
                  z2 = z1;
                  break;
               case WEST:
                  if (cullWest) {
                     continue;
                  }

                  c1 = southWestHeight;
                  c2 = northWestHeight;
                  x1 = 0.001F;
                  x2 = x1;
                  z1 = 1.0F;
                  z2 = 0.0F;
                  break;
               case EAST:
                  if (!cullEast) {
                     c1 = northEastHeight;
                     c2 = southEastHeight;
                     x1 = 0.999F;
                     x2 = x1;
                     z1 = 0.0F;
                     z2 = 1.0F;
                     break;
                  }
               default:
                  continue;
            }

            if (this.isSideExposed(level, posX, posY, posZ, dir, Math.max(c1, c2))) {
               int adjX = posX + dir.getStepX();
               int adjY = posY + dir.getStepY();
               int adjZ = posZ + dir.getStepZ();
               TextureAtlasSprite spritex = sprites[1];
               boolean isOverlay = false;
               if (sprites.length > 2 && sprites[2] != null) {
                  BlockPos adjPos = this.scratchPos.set(adjX, adjY, adjZ);
                  BlockState adjBlock = level.getBlockState(adjPos);
                  if (PlatformBlockAccess.getInstance().shouldShowFluidOverlay(adjBlock, level, adjPos, fluidState)) {
                     spritex = sprites[2];
                     isOverlay = true;
                  }
               }

               float u1x = spritex.getU(0.0F);
               float u2x = spritex.getU(0.5F);
               float v1x = spritex.getV((1.0F - c1) * 0.5F);
               float v2x = spritex.getV((1.0F - c2) * 0.5F);
               float v3x = spritex.getV(0.5F);
               quad.setSprite(spritex);
               setVertex(quad, 0, x2, c2, z2, u2x, v2x);
               setVertex(quad, 1, x2, yOffset, z2, u2x, v3x);
               setVertex(quad, 2, x1, yOffset, z1, u1x, v3x);
               setVertex(quad, 3, x1, c1, z1, u1x, v1x);
               float br = dir.getAxis() == Axis.Z ? 0.8F : 0.6F;
               ModelQuadFacing facing = ModelQuadFacing.fromDirection(dir);
               this.updateQuad(quad, level, blockPos, lighter, dir, facing, br, colorProvider, fluidState);
               this.writeQuad(meshBuilder, collector, material, offset, quad, facing, false);
               if (!isOverlay) {
                  this.writeQuad(meshBuilder, collector, material, offset, quad, facing.getOpposite(), true);
               }
            }
         }
      }
   }

   private static boolean isAlignedEquals(float a, float b) {
      return Math.abs(a - b) <= 0.011F;
   }

   private void updateQuad(
      ModelQuadViewMutable quad,
      LevelSlice level,
      BlockPos pos,
      LightPipeline lighter,
      Direction dir,
      ModelQuadFacing facing,
      float brightness,
      ColorProvider<FluidState> colorProvider,
      FluidState fluidState
   ) {
      int normal;
      if (facing.isAligned()) {
         normal = facing.getPackedAlignedNormal();
      } else {
         normal = quad.calculateNormal();
      }

      quad.setFaceNormal(normal);
      QuadLightData light = this.quadLightData;
      lighter.calculate(quad, pos, light, null, dir, false, false);
      colorProvider.getColors(level, pos, this.scratchPos, fluidState, quad, this.quadColors);

      for (int i = 0; i < 4; i++) {
         this.quadColors[i] = ColorARGB.toABGR(this.quadColors[i]);
         this.brightness[i] = light.br[i] * brightness;
      }
   }

   private void writeQuad(
      ChunkModelBuilder builder,
      TranslucentGeometryCollector collector,
      Material material,
      BlockPos offset,
      ModelQuadView quad,
      ModelQuadFacing facing,
      boolean flip
   ) {
      ChunkVertexEncoder.Vertex[] vertices = this.vertices;

      for (int i = 0; i < 4; i++) {
         ChunkVertexEncoder.Vertex out = vertices[flip ? 3 - i + 1 & 3 : i];
         out.x = offset.getX() + quad.getX(i);
         out.y = offset.getY() + quad.getY(i);
         out.z = offset.getZ() + quad.getZ(i);
         out.color = this.quadColors[i];
         out.ao = this.brightness[i];
         out.u = quad.getTexU(i);
         out.v = quad.getTexV(i);
         out.light = this.quadLightData.lm[i];
      }

      TextureAtlasSprite sprite = quad.getSprite();
      if (sprite != null) {
         builder.addSprite(sprite);
      }

      if (material.isTranslucent() && collector != null) {
         int normal;
         if (facing.isAligned()) {
            normal = facing.getPackedAlignedNormal();
         } else {
            normal = quad.getFaceNormal();
         }

         if (flip) {
            normal = NormI8.flipPacked(normal);
         }

         collector.appendQuad(normal, vertices, facing);
      }

      ChunkMeshBufferBuilder vertexBuffer = builder.getVertexBuffer(facing);
      vertexBuffer.push(vertices, material);
   }

   private static void setVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v) {
      quad.setX(i, x);
      quad.setY(i, y);
      quad.setZ(i, z);
      quad.setTexU(i, u);
      quad.setTexV(i, v);
   }

   private float fluidCornerHeight(BlockAndTintGetter world, Fluid fluid, float fluidHeight, float fluidHeightX, float fluidHeightY, BlockPos blockPos) {
      if (!(fluidHeightY >= 1.0F) && !(fluidHeightX >= 1.0F)) {
         if (fluidHeightY > 0.0F || fluidHeightX > 0.0F) {
            float height = this.fluidHeight(world, fluid, blockPos, Direction.UP);
            if (height >= 1.0F) {
               return 1.0F;
            }

            this.modifyHeight(this.scratchHeight, this.scratchSamples, height);
         }

         this.modifyHeight(this.scratchHeight, this.scratchSamples, fluidHeight);
         this.modifyHeight(this.scratchHeight, this.scratchSamples, fluidHeightY);
         this.modifyHeight(this.scratchHeight, this.scratchSamples, fluidHeightX);
         float result = this.scratchHeight.floatValue() / this.scratchSamples.intValue();
         this.scratchHeight.setValue(0.0F);
         this.scratchSamples.setValue(0);
         return result;
      } else {
         return 1.0F;
      }
   }

   private void modifyHeight(MutableFloat totalHeight, MutableInt samples, float target) {
      if (target >= 0.8F) {
         totalHeight.add(target * 10.0F);
         samples.add(10);
      } else if (target >= 0.0F) {
         totalHeight.add(target);
         samples.increment();
      }
   }

   private float fluidHeight(BlockAndTintGetter world, Fluid fluid, BlockPos blockPos, Direction direction) {
      BlockState blockState = world.getBlockState(blockPos);
      FluidState fluidState = blockState.getFluidState();
      if (fluid.isSame(fluidState.getType())) {
         FluidState fluidStateUp = world.getFluidState(blockPos.above());
         return fluid.isSame(fluidStateUp.getType()) ? 1.0F : fluidState.getOwnHeight();
      } else {
         return !blockState.isSolid() ? 0.0F : -1.0F;
      }
   }
}
