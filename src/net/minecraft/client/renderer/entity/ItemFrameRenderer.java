package net.minecraft.client.renderer.entity;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ItemFrameRenderer<T extends ItemFrame> extends EntityRenderer<T, ItemFrameRenderState> {
	public static final int GLOW_FRAME_BRIGHTNESS = 5;
	public static final int BRIGHT_MAP_LIGHT_ADJUSTMENT = 30;
	private final ItemModelResolver itemModelResolver;
	private final MapRenderer mapRenderer;
	private final BlockRenderDispatcher blockRenderer;

	public ItemFrameRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
		this.mapRenderer = context.getMapRenderer();
		this.blockRenderer = context.getBlockRenderDispatcher();
	}

	protected int getBlockLightLevel(T itemFrame, BlockPos blockPos) {
		return itemFrame.getType() == EntityType.GLOW_ITEM_FRAME
			? Math.max(5, super.getBlockLightLevel(itemFrame, blockPos))
			: super.getBlockLightLevel(itemFrame, blockPos);
	}

	private void renderProxy(PoseStack.Pose matrices, VertexConsumer vertexConsumer, BlockStateModel model, float red, float green, float blue, int light, int overlay, BlockState blockState) {
		FabricBlockModelRenderer.render(matrices, layer -> vertexConsumer, model, red, green, blue, light, overlay, EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, blockState);
	}

	public void render(ItemFrameRenderState itemFrameRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		super.render(itemFrameRenderState, poseStack, multiBufferSource, i);
		poseStack.pushPose();
		Direction direction = itemFrameRenderState.direction;
		Vec3 vec3 = this.getRenderOffset(itemFrameRenderState);
		poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
		double d = 0.46875;
		poseStack.translate(direction.getStepX() * 0.46875, direction.getStepY() * 0.46875, direction.getStepZ() * 0.46875);
		float f;
		float g;
		if (direction.getAxis().isHorizontal()) {
			f = 0.0F;
			g = 180.0F - direction.toYRot();
		} else {
			f = -90 * direction.getAxisDirection().getStep();
			g = 180.0F;
		}

		poseStack.mulPose(Axis.XP.rotationDegrees(f));
		poseStack.mulPose(Axis.YP.rotationDegrees(g));
		if (!itemFrameRenderState.isInvisible) {
			BlockState blockState = BlockStateDefinitions.getItemFrameFakeState(itemFrameRenderState.isGlowFrame, itemFrameRenderState.mapId != null);
			BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
			poseStack.pushPose();
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			renderProxy(
				poseStack.last(),
				multiBufferSource.getBuffer(RenderType.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS)),
				blockStateModel,
				1.0F,
				1.0F,
				1.0F,
				i,
				OverlayTexture.NO_OVERLAY,
				blockState
			);
			/*ModelBlockRenderer.renderModel(
				poseStack.last(),
				multiBufferSource.getBuffer(RenderType.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS)),
				blockStateModel,
				1.0F,
				1.0F,
				1.0F,
				i,
				OverlayTexture.NO_OVERLAY
			);*/
			poseStack.popPose();
		}

		if (itemFrameRenderState.isInvisible) {
			poseStack.translate(0.0F, 0.0F, 0.5F);
		} else {
			poseStack.translate(0.0F, 0.0F, 0.4375F);
		}

		if (itemFrameRenderState.mapId != null) {
			int j = itemFrameRenderState.rotation % 4 * 2;
			poseStack.mulPose(Axis.ZP.rotationDegrees(j * 360.0F / 8.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
			float h = 0.0078125F;
			poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
			poseStack.translate(-64.0F, -64.0F, 0.0F);
			poseStack.translate(0.0F, 0.0F, -1.0F);
			int k = this.getLightCoords(itemFrameRenderState.isGlowFrame, 15728850, i);
			this.mapRenderer.render(itemFrameRenderState.mapRenderState, poseStack, multiBufferSource, true, k);
		} else if (!itemFrameRenderState.item.isEmpty()) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(itemFrameRenderState.rotation * 360.0F / 8.0F));
			int j = this.getLightCoords(itemFrameRenderState.isGlowFrame, 15728880, i);
			poseStack.scale(0.5F, 0.5F, 0.5F);
			itemFrameRenderState.item.render(poseStack, multiBufferSource, j, OverlayTexture.NO_OVERLAY);
		}

		poseStack.popPose();
	}

	private int getLightCoords(boolean bl, int i, int j) {
		return bl ? i : j;
	}

	public Vec3 getRenderOffset(ItemFrameRenderState itemFrameRenderState) {
		return new Vec3(itemFrameRenderState.direction.getStepX() * 0.3F, -0.25, itemFrameRenderState.direction.getStepZ() * 0.3F);
	}

	protected boolean shouldShowName(T itemFrame, double d) {
		return Minecraft.renderNames() && this.entityRenderDispatcher.crosshairPickEntity == itemFrame && itemFrame.getItem().getCustomName() != null;
	}

	protected Component getNameTag(T itemFrame) {
		return itemFrame.getItem().getHoverName();
	}

	public ItemFrameRenderState createRenderState() {
		return new ItemFrameRenderState();
	}

	public void extractRenderState(T itemFrame, ItemFrameRenderState itemFrameRenderState, float f) {
		super.extractRenderState(itemFrame, itemFrameRenderState, f);
		itemFrameRenderState.direction = itemFrame.getDirection();
		ItemStack itemStack = itemFrame.getItem();
		this.itemModelResolver.updateForNonLiving(itemFrameRenderState.item, itemStack, ItemDisplayContext.FIXED, itemFrame);
		itemFrameRenderState.rotation = itemFrame.getRotation();
		itemFrameRenderState.isGlowFrame = itemFrame.getType() == EntityType.GLOW_ITEM_FRAME;
		itemFrameRenderState.mapId = null;
		if (!itemStack.isEmpty()) {
			MapId mapId = itemFrame.getFramedMapId(itemStack);
			if (mapId != null) {
				MapItemSavedData mapItemSavedData = itemFrame.level().getMapData(mapId);
				if (mapItemSavedData != null) {
					this.mapRenderer.extractRenderState(mapId, mapItemSavedData, itemFrameRenderState.mapRenderState);
					itemFrameRenderState.mapId = mapId;
				}
			}
		}
	}
}
