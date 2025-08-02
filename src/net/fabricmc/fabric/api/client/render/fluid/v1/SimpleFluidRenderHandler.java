
package net.fabricmc.fabric.api.client.render.fluid.v1;

import java.util.Objects;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;


public class SimpleFluidRenderHandler implements FluidRenderHandler {

	/*public static final ResourceLocation WATER_STILL = new ResourceLocation("block/water_still");


	public static final ResourceLocation WATER_FLOWING = new ResourceLocation("block/water_flow");

	public static final ResourceLocation WATER_OVERLAY = new ResourceLocation("block/water_overlay");

	public static final ResourceLocation LAVA_STILL = new ResourceLocation("block/lava_still");

	public static final ResourceLocation LAVA_FLOWING = new ResourceLocation("block/lava_flow");*/

	protected final ResourceLocation stillTexture;
	protected final ResourceLocation flowingTexture;
	protected final ResourceLocation overlayTexture;

	protected final TextureAtlasSprite[] sprites;

	protected final int tint;


	public SimpleFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture, @Nullable ResourceLocation overlayTexture, int tint) {
		this.stillTexture = Objects.requireNonNull(stillTexture, "stillTexture");
		this.flowingTexture = Objects.requireNonNull(flowingTexture, "flowingTexture");
		this.overlayTexture = overlayTexture;
		this.sprites = new TextureAtlasSprite[overlayTexture == null ? 2 : 3];
		this.tint = tint;
	}


	/*public SimpleFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture, ResourceLocation overlayTexture) {
		this(stillTexture, flowingTexture, overlayTexture, -1);
	}


	public SimpleFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tint) {
		this(stillTexture, flowingTexture, null, tint);
	}


	public SimpleFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
		this(stillTexture, flowingTexture, null, -1);
	}


	public static SimpleFluidRenderHandler coloredWater(int tint) {
		return new SimpleFluidRenderHandler(WATER_STILL, WATER_FLOWING, WATER_OVERLAY, tint);
	}*/


	@Override
	public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
		return sprites;
	}

	@Override
	public void reloadTextures(TextureAtlas textureAtlas) {
		sprites[0] = textureAtlas.getSprite(stillTexture);
		sprites[1] = textureAtlas.getSprite(flowingTexture);

		if (overlayTexture != null) {
			sprites[2] = textureAtlas.getSprite(overlayTexture);
		}
	}


	@Override
	public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
		return tint;
	}
}
