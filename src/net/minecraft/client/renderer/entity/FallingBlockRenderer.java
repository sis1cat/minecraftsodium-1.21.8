package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity, FallingBlockRenderState> {
	private final BlockRenderDispatcher dispatcher;

	public FallingBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
		this.dispatcher = context.getBlockRenderDispatcher();
	}

	public boolean shouldRender(FallingBlockEntity fallingBlockEntity, Frustum frustum, double d, double e, double f) {
		return !super.shouldRender(fallingBlockEntity, frustum, d, e, f)
			? false
			: fallingBlockEntity.getBlockState() != fallingBlockEntity.level().getBlockState(fallingBlockEntity.blockPosition());
	}

	public void render(FallingBlockRenderState renderState, PoseStack matrixStack, MultiBufferSource vertexConsumers, int light) {
		BlockState blockState = renderState.blockState;

		if (blockState.getRenderShape() == RenderShape.MODEL) {
			matrixStack.pushPose();
			matrixStack.translate(-0.5, 0.0, -0.5);

			BlockStateModel model = dispatcher.getBlockModel(blockState);
			long seed = blockState.getSeed(renderState.startBlockPos);
			dispatcher.getModelRenderer().render(renderState, model, blockState, renderState.blockPos, matrixStack, RenderLayerHelper.movingDelegate(vertexConsumers), false, seed, OverlayTexture.NO_OVERLAY);

			matrixStack.popPose();
			super.render(renderState, matrixStack, vertexConsumers, light);
		}
		/*BlockState blockState = fallingBlockRenderState.blockState;
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			poseStack.pushPose();
			poseStack.translate(-0.5, 0.0, -0.5);
			List<BlockModelPart> list = this.dispatcher
				.getBlockModel(blockState)
				.collectParts(RandomSource.create(blockState.getSeed(fallingBlockRenderState.startBlockPos)));
			this.dispatcher
				.getModelRenderer()
				.tesselateBlock(
					fallingBlockRenderState,
					list,
					blockState,
					fallingBlockRenderState.blockPos,
					poseStack,
					multiBufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)),
					false,
					OverlayTexture.NO_OVERLAY
				);
			poseStack.popPose();
			super.render(fallingBlockRenderState, poseStack, multiBufferSource, i);
		}*/
	}

	public FallingBlockRenderState createRenderState() {
		return new FallingBlockRenderState();
	}

	public void extractRenderState(FallingBlockEntity fallingBlockEntity, FallingBlockRenderState fallingBlockRenderState, float f) {
		super.extractRenderState(fallingBlockEntity, fallingBlockRenderState, f);
		BlockPos blockPos = BlockPos.containing(fallingBlockEntity.getX(), fallingBlockEntity.getBoundingBox().maxY, fallingBlockEntity.getZ());
		fallingBlockRenderState.startBlockPos = fallingBlockEntity.getStartPos();
		fallingBlockRenderState.blockPos = blockPos;
		fallingBlockRenderState.blockState = fallingBlockEntity.getBlockState();
		fallingBlockRenderState.biome = fallingBlockEntity.level().getBiome(blockPos);
		fallingBlockRenderState.level = fallingBlockEntity.level();
	}
}
