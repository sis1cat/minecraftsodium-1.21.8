package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;

@Environment(EnvType.CLIENT)
public class ItemInHandLayer<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {
	public ItemInHandLayer(RenderLayerParent<S, M> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S armedEntityRenderState, float f, float g) {
		this.renderArmWithItem(armedEntityRenderState, armedEntityRenderState.rightHandItem, HumanoidArm.RIGHT, poseStack, multiBufferSource, i);
		this.renderArmWithItem(armedEntityRenderState, armedEntityRenderState.leftHandItem, HumanoidArm.LEFT, poseStack, multiBufferSource, i);
	}

	protected void renderArmWithItem(
		S armedEntityRenderState, ItemStackRenderState itemStackRenderState, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		if (!itemStackRenderState.isEmpty()) {
			poseStack.pushPose();
			this.getParentModel().translateToHand(humanoidArm, poseStack);
			poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			boolean bl = humanoidArm == HumanoidArm.LEFT;
			poseStack.translate((bl ? -1 : 1) / 16.0F, 0.125F, -0.625F);
			itemStackRenderState.render(poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}
}
