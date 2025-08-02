package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockRenderManager;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SpecialBlockModelRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class BlockRenderDispatcher implements ResourceManagerReloadListener, FabricBlockRenderManager {
	private final BlockModelShaper blockModelShaper;
	private final ModelBlockRenderer modelRenderer;
	public final Supplier<SpecialBlockModelRenderer> specialBlockModelRenderer;
	private final LiquidBlockRenderer liquidBlockRenderer;
	private final RandomSource singleThreadRandom = RandomSource.create();
	private final List<BlockModelPart> singleThreadPartList = new ArrayList();
	private final BlockColors blockColors;

	public BlockRenderDispatcher(BlockModelShaper blockModelShaper, Supplier<SpecialBlockModelRenderer> supplier, BlockColors blockColors) {
		this.blockModelShaper = blockModelShaper;
		this.specialBlockModelRenderer = supplier;
		this.blockColors = blockColors;
		this.modelRenderer = new ModelBlockRenderer(this.blockColors);
		this.liquidBlockRenderer = new LiquidBlockRenderer();
	}

	public BlockModelShaper getBlockModelShaper() {
		return this.blockModelShaper;
	}

	public void renderBreakingTexture(
		BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, VertexConsumer vertexConsumer
	) {
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			BlockStateModel blockStateModel = this.blockModelShaper.getBlockModel(blockState);
			this.modelRenderer
					.render(blockAndTintGetter, blockStateModel, blockState, blockPos, poseStack, layer -> vertexConsumer, true, blockState.getSeed(blockPos), OverlayTexture.NO_OVERLAY);
			return;
			/*this.singleThreadRandom.setSeed(blockState.getSeed(blockPos));
			this.singleThreadPartList.clear();
			blockStateModel.collectParts(this.singleThreadRandom, this.singleThreadPartList);
			this.modelRenderer
				.tesselateBlock(blockAndTintGetter, this.singleThreadPartList, blockState, blockPos, poseStack, vertexConsumer, true, OverlayTexture.NO_OVERLAY);*/
		}
	}

	public void renderBatched(
		BlockState blockState,
		BlockPos blockPos,
		BlockAndTintGetter blockAndTintGetter,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		boolean bl,
		List<BlockModelPart> list
	) {
		try {
			this.modelRenderer.tesselateBlock(blockAndTintGetter, list, blockState, blockPos, poseStack, vertexConsumer, bl, OverlayTexture.NO_OVERLAY);
		} catch (Throwable var11) {
			CrashReport crashReport = CrashReport.forThrowable(var11, "Tesselating block in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
			CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
			throw new ReportedException(crashReport);
		}
	}

	public void renderLiquid(BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		try {
			this.liquidBlockRenderer.tesselate(blockAndTintGetter, blockPos, vertexConsumer, blockState, fluidState);
		} catch (Throwable var9) {
			CrashReport crashReport = CrashReport.forThrowable(var9, "Tesselating liquid in world");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
			CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
			throw new ReportedException(crashReport);
		}
	}

	public ModelBlockRenderer getModelRenderer() {
		return this.modelRenderer;
	}

	public BlockStateModel getBlockModel(BlockState blockState) {
		return this.blockModelShaper.getBlockModel(blockState);
	}

	private void renderProxy(
			PoseStack.Pose entry,
			VertexConsumer vertexConsumer,
			BlockStateModel model,
			float red,
			float green,
			float blue,
			int light,
			int overlay,
			BlockState state,
			PoseStack matrices,
			MultiBufferSource vertexConsumers,
			int light1,
			int overlay1
	) {
		FabricBlockModelRenderer.render(
				entry,
				layer -> vertexConsumers.getBuffer(RenderLayerHelper.getEntityBlockLayer(layer)),
				model,
				red,
				green,
				blue,
				light,
				overlay,
				EmptyBlockAndTintGetter.INSTANCE,
				BlockPos.ZERO,
				state
		);
	}

	public void renderSingleBlock(BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		RenderShape renderShape = blockState.getRenderShape();
		if (renderShape != RenderShape.INVISIBLE) {
			BlockStateModel blockStateModel = this.getBlockModel(blockState);
			int k = this.blockColors.getColor(blockState, null, null, 0);
			float f = (k >> 16 & 0xFF) / 255.0F;
			float g = (k >> 8 & 0xFF) / 255.0F;
			float h = (k & 0xFF) / 255.0F;
			renderProxy(poseStack.last(), multiBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState)), blockStateModel, f, g, h, i, j, blockState, poseStack, multiBufferSource, 0, 0);
			//ModelBlockRenderer.renderModel(poseStack.last(), multiBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState)), blockStateModel, f, g, h, i, j);
			((SpecialBlockModelRenderer)this.specialBlockModelRenderer.get())
				.renderByBlock(blockState.getBlock(), ItemDisplayContext.NONE, poseStack, multiBufferSource, i, j);
		}
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.liquidBlockRenderer.setupSprites();
	}
}
