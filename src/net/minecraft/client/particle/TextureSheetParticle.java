package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public abstract class TextureSheetParticle extends SingleQuadParticle {
	protected TextureAtlasSprite sprite;
	private boolean shouldTickSprite;

	protected TextureSheetParticle(ClientLevel clientLevel, double d, double e, double f) {
		super(clientLevel, d, e, f);
	}

	protected TextureSheetParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f, g, h, i);
	}

	protected void setSprite(TextureAtlasSprite textureAtlasSprite) {
		this.sprite = textureAtlasSprite;
		this.shouldTickSprite = sprite != null && SpriteUtil.INSTANCE.hasAnimation(sprite);
	}

	@Override
	protected float getU0() {
		return this.sprite.getU0();
	}

	@Override
	protected float getU1() {
		return this.sprite.getU1();
	}

	@Override
	protected float getV0() {
		return this.sprite.getV0();
	}

	@Override
	protected float getV1() {
		return this.sprite.getV1();
	}

	public void pickSprite(SpriteSet spriteSet) {
		this.setSprite(spriteSet.get(this.random));
	}

	public void setSpriteFromAge(SpriteSet spriteSet) {
		if (!this.removed) {
			this.setSprite(spriteSet.get(this.age, this.lifetime));
		}
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		if (this.shouldTickSprite) {
			SpriteUtil.INSTANCE.markSpriteActive(this.sprite);
		}

		super.render(vertexConsumer, camera, f);
	}

}
