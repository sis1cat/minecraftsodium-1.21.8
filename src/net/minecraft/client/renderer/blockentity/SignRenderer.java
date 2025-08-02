package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
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
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class SignRenderer extends AbstractSignRenderer {
	public static final float RENDER_SCALE = 0.6666667F;
	private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.33333334F, 0.046666667F);
	private final Map<WoodType, SignRenderer.Models> signModels;

	public SignRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		this.signModels = (Map<WoodType, SignRenderer.Models>)WoodType.values()
			.collect(
				ImmutableMap.toImmutableMap(
					woodType -> woodType,
					woodType -> new SignRenderer.Models(createSignModel(context.getModelSet(), woodType, true), createSignModel(context.getModelSet(), woodType, false))
				)
			);
	}

	@Override
	protected Model getSignModel(BlockState blockState, WoodType woodType) {
		SignRenderer.Models models = (SignRenderer.Models)this.signModels.get(woodType);
		return blockState.getBlock() instanceof StandingSignBlock ? models.standing() : models.wall();
	}

	@Override
	protected Material getSignMaterial(WoodType woodType) {
		return Sheets.getSignMaterial(woodType);
	}

	@Override
	protected float getSignModelRenderScale() {
		return 0.6666667F;
	}

	@Override
	protected float getSignTextRenderScale() {
		return 0.6666667F;
	}

	private static void translateBase(PoseStack poseStack, float f) {
		poseStack.translate(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(f));
	}

	@Override
	protected void translateSign(PoseStack poseStack, float f, BlockState blockState) {
		translateBase(poseStack, f);
		if (!(blockState.getBlock() instanceof StandingSignBlock)) {
			poseStack.translate(0.0F, -0.3125F, -0.4375F);
		}
	}

	@Override
	protected Vec3 getTextOffset() {
		return TEXT_OFFSET;
	}

	public static void renderInHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Model model, Material material) {
		poseStack.pushPose();
		applyInHandTransforms(poseStack);
		VertexConsumer vertexConsumer = material.buffer(multiBufferSource, model::renderType);
		model.renderToBuffer(poseStack, vertexConsumer, i, j);
		poseStack.popPose();
	}

	public static void applyInHandTransforms(PoseStack poseStack) {
		translateBase(poseStack, 0.0F);
		poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
	}

	public static Model createSignModel(EntityModelSet entityModelSet, WoodType woodType, boolean bl) {
		ModelLayerLocation modelLayerLocation = bl ? ModelLayers.createStandingSignModelName(woodType) : ModelLayers.createWallSignModelName(woodType);
		return new Model.Simple(entityModelSet.bakeLayer(modelLayerLocation), RenderType::entityCutoutNoCull);
	}

	public static LayerDefinition createSignLayer(boolean bl) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
		if (bl) {
			partDefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
		}

		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Environment(EnvType.CLIENT)
	record Models(Model standing, Model wall) {
	}
}
