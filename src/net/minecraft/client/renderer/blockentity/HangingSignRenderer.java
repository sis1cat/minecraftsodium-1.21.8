package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class HangingSignRenderer extends AbstractSignRenderer {
	private static final String PLANK = "plank";
	private static final String V_CHAINS = "vChains";
	private static final String NORMAL_CHAINS = "normalChains";
	private static final String CHAIN_L_1 = "chainL1";
	private static final String CHAIN_L_2 = "chainL2";
	private static final String CHAIN_R_1 = "chainR1";
	private static final String CHAIN_R_2 = "chainR2";
	private static final String BOARD = "board";
	public static final float MODEL_RENDER_SCALE = 1.0F;
	private static final float TEXT_RENDER_SCALE = 0.9F;
	private static final Vec3 TEXT_OFFSET = new Vec3(0.0, -0.32F, 0.073F);
	private final Map<HangingSignRenderer.ModelKey, Model> hangingSignModels;

	public HangingSignRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		Stream<HangingSignRenderer.ModelKey> stream = WoodType.values()
			.flatMap(
				woodType -> Arrays.stream(HangingSignRenderer.AttachmentType.values()).map(attachmentType -> new HangingSignRenderer.ModelKey(woodType, attachmentType))
			);
		this.hangingSignModels = (Map<HangingSignRenderer.ModelKey, Model>)stream.collect(
			ImmutableMap.toImmutableMap(modelKey -> modelKey, modelKey -> createSignModel(context.getModelSet(), modelKey.woodType, modelKey.attachmentType))
		);
	}

	public static Model createSignModel(EntityModelSet entityModelSet, WoodType woodType, HangingSignRenderer.AttachmentType attachmentType) {
		return new Model.Simple(entityModelSet.bakeLayer(ModelLayers.createHangingSignModelName(woodType, attachmentType)), RenderType::entityCutoutNoCull);
	}

	@Override
	protected float getSignModelRenderScale() {
		return 1.0F;
	}

	@Override
	protected float getSignTextRenderScale() {
		return 0.9F;
	}

	public static void translateBase(PoseStack poseStack, float f) {
		poseStack.translate(0.5, 0.9375, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(f));
		poseStack.translate(0.0F, -0.3125F, 0.0F);
	}

	@Override
	protected void translateSign(PoseStack poseStack, float f, BlockState blockState) {
		translateBase(poseStack, f);
	}

	@Override
	protected Model getSignModel(BlockState blockState, WoodType woodType) {
		HangingSignRenderer.AttachmentType attachmentType = HangingSignRenderer.AttachmentType.byBlockState(blockState);
		return (Model)this.hangingSignModels.get(new HangingSignRenderer.ModelKey(woodType, attachmentType));
	}

	@Override
	protected Material getSignMaterial(WoodType woodType) {
		return Sheets.getHangingSignMaterial(woodType);
	}

	@Override
	protected Vec3 getTextOffset() {
		return TEXT_OFFSET;
	}

	public static void renderInHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Model model, Material material) {
		poseStack.pushPose();
		translateBase(poseStack, 0.0F);
		poseStack.scale(1.0F, -1.0F, -1.0F);
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, model::renderType);
		model.renderToBuffer(poseStack, vertexConsumer, i, j);
		poseStack.popPose();
	}

	public static LayerDefinition createHangingSignLayer(HangingSignRenderer.AttachmentType attachmentType) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("board", CubeListBuilder.create().texOffs(0, 12).addBox(-7.0F, 0.0F, -1.0F, 14.0F, 10.0F, 2.0F), PartPose.ZERO);
		if (attachmentType == HangingSignRenderer.AttachmentType.WALL) {
			partDefinition.addOrReplaceChild("plank", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -2.0F, 16.0F, 2.0F, 4.0F), PartPose.ZERO);
		}

		if (attachmentType == HangingSignRenderer.AttachmentType.WALL || attachmentType == HangingSignRenderer.AttachmentType.CEILING) {
			PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("normalChains", CubeListBuilder.create(), PartPose.ZERO);
			partDefinition2.addOrReplaceChild(
				"chainL1",
				CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
				PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
			);
			partDefinition2.addOrReplaceChild(
				"chainL2",
				CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
				PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
			);
			partDefinition2.addOrReplaceChild(
				"chainR1",
				CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
				PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
			);
			partDefinition2.addOrReplaceChild(
				"chainR2",
				CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
				PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
			);
		}

		if (attachmentType == HangingSignRenderer.AttachmentType.CEILING_MIDDLE) {
			partDefinition.addOrReplaceChild("vChains", CubeListBuilder.create().texOffs(14, 6).addBox(-6.0F, -6.0F, 0.0F, 12.0F, 6.0F, 0.0F), PartPose.ZERO);
		}

		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Environment(EnvType.CLIENT)
	public static enum AttachmentType implements StringRepresentable {
		WALL("wall"),
		CEILING("ceiling"),
		CEILING_MIDDLE("ceiling_middle");

		private final String name;

		private AttachmentType(final String string2) {
			this.name = string2;
		}

		public static HangingSignRenderer.AttachmentType byBlockState(BlockState blockState) {
			if (blockState.getBlock() instanceof CeilingHangingSignBlock) {
				return blockState.getValue(BlockStateProperties.ATTACHED) ? CEILING_MIDDLE : CEILING;
			} else {
				return WALL;
			}
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	@Environment(EnvType.CLIENT)
	public record ModelKey(WoodType woodType, HangingSignRenderer.AttachmentType attachmentType) {
	}
}
