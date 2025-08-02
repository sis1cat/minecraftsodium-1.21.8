package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

@Environment(EnvType.CLIENT)
public class BannerModel extends Model {
	public static final int BANNER_WIDTH = 20;
	public static final int BANNER_HEIGHT = 40;
	public static final String FLAG = "flag";
	private static final String POLE = "pole";
	private static final String BAR = "bar";

	public BannerModel(ModelPart modelPart) {
		super(modelPart, RenderType::entitySolid);
	}

	public static LayerDefinition createBodyLayer(boolean bl) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		if (bl) {
			partDefinition.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, -42.0F, -1.0F, 2.0F, 42.0F, 2.0F), PartPose.ZERO);
		}

		partDefinition.addOrReplaceChild(
			"bar", CubeListBuilder.create().texOffs(0, 42).addBox(-10.0F, bl ? -44.0F : -20.5F, bl ? -1.0F : 9.5F, 20.0F, 2.0F, 2.0F), PartPose.ZERO
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}
}
