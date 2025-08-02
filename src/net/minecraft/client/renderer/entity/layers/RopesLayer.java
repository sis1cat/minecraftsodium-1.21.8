package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HappyGhastModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;

@Environment(EnvType.CLIENT)
public class RopesLayer<M extends HappyGhastModel> extends RenderLayer<HappyGhastRenderState, M> {
	private final RenderType ropes;
	private final HappyGhastModel adultModel;
	private final HappyGhastModel babyModel;

	public RopesLayer(RenderLayerParent<HappyGhastRenderState, M> renderLayerParent, EntityModelSet entityModelSet, ResourceLocation resourceLocation) {
		super(renderLayerParent);
		this.ropes = RenderType.entityCutoutNoCull(resourceLocation);
		this.adultModel = new HappyGhastModel(entityModelSet.bakeLayer(ModelLayers.HAPPY_GHAST_ROPES));
		this.babyModel = new HappyGhastModel(entityModelSet.bakeLayer(ModelLayers.HAPPY_GHAST_BABY_ROPES));
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, HappyGhastRenderState happyGhastRenderState, float f, float g) {
		if (happyGhastRenderState.isLeashHolder && happyGhastRenderState.bodyItem.is(ItemTags.HARNESSES)) {
			HappyGhastModel happyGhastModel = happyGhastRenderState.isBaby ? this.babyModel : this.adultModel;
			happyGhastModel.setupAnim(happyGhastRenderState);
			happyGhastModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.ropes), i, OverlayTexture.NO_OVERLAY);
		}
	}
}
