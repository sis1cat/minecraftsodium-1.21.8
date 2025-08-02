package com.mojang.blaze3d.font;

import com.mojang.blaze3d.textures.GpuTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SheetGlyphInfo {
	int getPixelWidth();

	int getPixelHeight();

	void upload(int i, int j, GpuTexture gpuTexture);

	boolean isColored();

	float getOversample();

	default float getLeft() {
		return this.getBearingLeft();
	}

	default float getRight() {
		return this.getLeft() + this.getPixelWidth() / this.getOversample();
	}

	default float getTop() {
		return 7.0F - this.getBearingTop();
	}

	default float getBottom() {
		return this.getTop() + this.getPixelHeight() / this.getOversample();
	}

	default float getBearingLeft() {
		return 0.0F;
	}

	default float getBearingTop() {
		return 7.0F;
	}
}
