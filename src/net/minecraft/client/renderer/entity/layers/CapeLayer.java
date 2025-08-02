package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;

@Environment(EnvType.CLIENT)
public class CapeLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
	private final HumanoidModel<PlayerRenderState> model;
	private final EquipmentAssetManager equipmentAssets;

	public CapeLayer(
		RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentAssetManager equipmentAssetManager
	) {
		super(renderLayerParent);
		this.model = new PlayerCapeModel<>(entityModelSet.bakeLayer(ModelLayers.PLAYER_CAPE));
		this.equipmentAssets = equipmentAssetManager;
	}

	private boolean hasLayer(ItemStack itemStack, EquipmentClientInfo.LayerType layerType) {
		Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
		if (equippable != null && !equippable.assetId().isEmpty()) {
			EquipmentClientInfo equipmentClientInfo = this.equipmentAssets.get((ResourceKey<EquipmentAsset>)equippable.assetId().get());
			return !equipmentClientInfo.getLayers(layerType).isEmpty();
		} else {
			return false;
		}
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState playerRenderState, float f, float g) {
		if (!playerRenderState.isInvisible && playerRenderState.showCape) {
			PlayerSkin playerSkin = playerRenderState.skin;
			if (playerSkin.capeTexture() != null) {
				if (!this.hasLayer(playerRenderState.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
					poseStack.pushPose();
					if (this.hasLayer(playerRenderState.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
						poseStack.translate(0.0F, -0.053125F, 0.06875F);
					}

					VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(playerSkin.capeTexture()));
					this.getParentModel().copyPropertiesTo(this.model);
					this.model.setupAnim(playerRenderState);
					this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
					poseStack.popPose();
				}
			}
		}
	}
}
