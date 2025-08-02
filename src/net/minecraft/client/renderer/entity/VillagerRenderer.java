package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

@Environment(EnvType.CLIENT)
public class VillagerRenderer extends AgeableMobRenderer<Villager, VillagerRenderState, VillagerModel> {
	private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
	public static final CustomHeadLayer.Transforms CUSTOM_HEAD_TRANSFORMS = new CustomHeadLayer.Transforms(-0.1171875F, -0.07421875F, 1.0F);

	public VillagerRenderer(EntityRendererProvider.Context context) {
		super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER_BABY)), 0.5F);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), CUSTOM_HEAD_TRANSFORMS));
		this.addLayer(new VillagerProfessionLayer<>(this, context.getResourceManager(), "villager"));
		this.addLayer(new CrossedArmsItemLayer<>(this));
	}

	public ResourceLocation getTextureLocation(VillagerRenderState villagerRenderState) {
		return VILLAGER_BASE_SKIN;
	}

	protected float getShadowRadius(VillagerRenderState villagerRenderState) {
		float f = super.getShadowRadius(villagerRenderState);
		return villagerRenderState.isBaby ? f * 0.5F : f;
	}

	public VillagerRenderState createRenderState() {
		return new VillagerRenderState();
	}

	public void extractRenderState(Villager villager, VillagerRenderState villagerRenderState, float f) {
		super.extractRenderState(villager, villagerRenderState, f);
		HoldingEntityRenderState.extractHoldingEntityRenderState(villager, villagerRenderState, this.itemModelResolver);
		villagerRenderState.isUnhappy = villager.getUnhappyCounter() > 0;
		villagerRenderState.villagerData = villager.getVillagerData();
	}
}
