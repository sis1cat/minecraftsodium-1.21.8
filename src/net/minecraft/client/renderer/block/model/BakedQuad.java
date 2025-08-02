package net.minecraft.client.renderer.block.model;

import net.caffeinemc.mods.sodium.client.model.quad.BakedQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import net.caffeinemc.mods.sodium.client.util.ModelQuadUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

@Environment(EnvType.CLIENT)
public class BakedQuad implements BakedQuadView {

	private final int[] vertices;
	private final int tintIndex;
	private final Direction direction;
	private final TextureAtlasSprite sprite;
	private final boolean shade;
	private final int lightEmission;
	private final int flags;
	private final int normal;
	private final ModelQuadFacing normalFace;

	public BakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, int lightEmission){
		this.vertices = vertices;
		this.tintIndex = tintIndex;
		this.direction = direction;
		this.sprite = sprite;
		this.shade = shade;
		this.lightEmission = lightEmission;
		this.normal = this.calculateNormal();
		this.normalFace = ModelQuadFacing.fromPackedNormal(this.normal);
		this.flags = ModelQuadFlags.getQuadFlags(this, direction);
	}

	public boolean isTinted() {
		return this.tintIndex != -1;
	}

	public int[] vertices() {
		return vertices;
	}
	public int tintIndex() {
		return tintIndex;
	}
	public Direction direction() {
		return direction;
	}
	public TextureAtlasSprite sprite() {
		return sprite;
	}
	public boolean shade() {
		return shade;
	}
	public int lightEmission() {
		return lightEmission;
	}

	@Override
	public float getX(int idx) {
		return Float.intBitsToFloat(this.vertices[ModelQuadUtil.vertexOffset(idx)]);
	}

	@Override
	public float getY(int idx) {
		return Float.intBitsToFloat(this.vertices[ModelQuadUtil.vertexOffset(idx) + 1]);
	}

	@Override
	public float getZ(int idx) {
		return Float.intBitsToFloat(this.vertices[ModelQuadUtil.vertexOffset(idx) + 2]);
	}

	@Override
	public int getColor(int idx) {
		return this.vertices[ModelQuadUtil.vertexOffset(idx) + 3];
	}

	@Override
	public int getVertexNormal(int idx) {
		return this.vertices[ModelQuadUtil.vertexOffset(idx) + 7];
	}

	@Override
	public int getLight(int idx) {
		return this.vertices[ModelQuadUtil.vertexOffset(idx) + 6];
	}

	@Override
	public TextureAtlasSprite getSprite() {
		return this.sprite;
	}

	@Override
	public float getTexU(int idx) {
		return Float.intBitsToFloat(this.vertices[ModelQuadUtil.vertexOffset(idx) + 4]);
	}

	@Override
	public float getTexV(int idx) {
		return Float.intBitsToFloat(this.vertices[ModelQuadUtil.vertexOffset(idx) + 4 + 1]);
	}

	@Override
	public int getFlags() {
		return this.flags;
	}

	@Override
	public int getTintIndex() {
		return this.tintIndex;
	}

	@Override
	public ModelQuadFacing getNormalFace() {
		return this.normalFace;
	}

	@Override
	public int getFaceNormal() {
		return this.normal;
	}

	@Override
	public Direction getLightFace() {
		return this.direction;
	}

	@Override
	public int getMaxLightQuad(int idx) {
		return LightTexture.lightCoordsWithEmission(this.getLight(idx), this.lightEmission());
	}

	@Override
	public boolean hasShade() {
		return this.shade;
	}

	@Override
	public boolean hasAO() {
		return true;
	}

}
