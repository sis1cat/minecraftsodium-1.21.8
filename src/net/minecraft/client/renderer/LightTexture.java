package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class LightTexture implements AutoCloseable {
	public static final int FULL_BRIGHT = 15728880;
	public static final int FULL_SKY = 15728640;
	public static final int FULL_BLOCK = 240;
	private static final int TEXTURE_SIZE = 16;
	private static final int LIGHTMAP_UBO_SIZE = new Std140SizeCalculator()
		.putFloat()
		.putFloat()
		.putFloat()
		.putInt()
		.putFloat()
		.putFloat()
		.putFloat()
		.putFloat()
		.putVec3()
		.get();
	private final GpuTexture texture;
	private final GpuTextureView textureView;
	private boolean updateLightTexture;
	private float blockLightRedFlicker;
	private final GameRenderer renderer;
	private final Minecraft minecraft;
	private final MappableRingBuffer ubo;

	public LightTexture(GameRenderer gameRenderer, Minecraft minecraft) {
		this.renderer = gameRenderer;
		this.minecraft = minecraft;
		GpuDevice gpuDevice = RenderSystem.getDevice();
		this.texture = gpuDevice.createTexture("Light Texture", 12, TextureFormat.RGBA8, 16, 16, 1, 1);
		this.texture.setTextureFilter(FilterMode.LINEAR, false);
		this.textureView = gpuDevice.createTextureView(this.texture);
		gpuDevice.createCommandEncoder().clearColorTexture(this.texture, -1);
		this.ubo = new MappableRingBuffer(() -> "Lightmap UBO", 130, LIGHTMAP_UBO_SIZE);
	}

	public GpuTextureView getTextureView() {
		return this.textureView;
	}

	public void close() {
		this.texture.close();
		this.textureView.close();
		this.ubo.close();
	}

	public void tick() {
		this.blockLightRedFlicker = this.blockLightRedFlicker + (float)((Math.random() - Math.random()) * Math.random() * Math.random() * 0.1);
		this.blockLightRedFlicker *= 0.9F;
		this.updateLightTexture = true;
	}

	public void turnOffLightLayer() {
		RenderSystem.setShaderTexture(2, null);
	}

	public void turnOnLightLayer() {
		RenderSystem.setShaderTexture(2, this.textureView);
	}

	private float calculateDarknessScale(LivingEntity livingEntity, float f, float g) {
		float h = 0.45F * f;
		return Math.max(0.0F, Mth.cos((livingEntity.tickCount - g) * (float) Math.PI * 0.025F) * h);
	}

	public void updateLightTexture(float f) {
		if (this.updateLightTexture) {
			this.updateLightTexture = false;
			ProfilerFiller profilerFiller = Profiler.get();
			profilerFiller.push("lightTex");
			ClientLevel clientLevel = this.minecraft.level;
			if (clientLevel != null) {
				float g = clientLevel.getSkyDarken(1.0F);
				float h;
				if (clientLevel.getSkyFlashTime() > 0) {
					h = 1.0F;
				} else {
					h = g * 0.95F + 0.05F;
				}

				float i = this.minecraft.options.darknessEffectScale().get().floatValue();
				float j = this.minecraft.player.getEffectBlendFactor(MobEffects.DARKNESS, f) * i;
				float k = this.calculateDarknessScale(this.minecraft.player, j, f) * i;
				float l = this.minecraft.player.getWaterVision();
				float m;
				if (this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION)) {
					m = GameRenderer.getNightVisionScale(this.minecraft.player, f);
				} else if (l > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER)) {
					m = l;
				} else {
					m = 0.0F;
				}

				Vector3f vector3f = new Vector3f(g, g, 1.0F).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
				float n = this.blockLightRedFlicker + 1.5F;
				float o = clientLevel.dimensionType().ambientLight();
				boolean bl = clientLevel.effects().forceBrightLightmap();
				float p = this.minecraft.options.gamma().get().floatValue();
				RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
				GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(6);
				CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

				try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.ubo.currentBuffer(), false, true)) {
					Std140Builder.intoBuffer(mappedView.data())
						.putFloat(o)
						.putFloat(h)
						.putFloat(n)
						.putInt(bl ? 1 : 0)
						.putFloat(m)
						.putFloat(k)
						.putFloat(this.renderer.getDarkenWorldAmount(f))
						.putFloat(Math.max(0.0F, p - j))
						.putVec3(vector3f);
				}

				try (RenderPass renderPass = commandEncoder.createRenderPass(() -> "Update light", this.textureView, OptionalInt.empty())) {
					renderPass.setPipeline(RenderPipelines.LIGHTMAP);
					RenderSystem.bindDefaultUniforms(renderPass);
					renderPass.setUniform("LightmapInfo", this.ubo.currentBuffer());
					renderPass.setVertexBuffer(0, RenderSystem.getQuadVertexBuffer());
					renderPass.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
					renderPass.drawIndexed(0, 0, 6, 1);
				}

				this.ubo.rotate();
				profilerFiller.pop();
			}
		}
	}

	public static float getBrightness(DimensionType dimensionType, int i) {
		return getBrightness(dimensionType.ambientLight(), i);
	}

	public static float getBrightness(float f, int i) {
		float g = i / 15.0F;
		float h = g / (4.0F - 3.0F * g);
		return Mth.lerp(f, h, 1.0F);
	}

	public static int pack(int i, int j) {
		return i << 4 | j << 20;
	}

	public static int block(int i) {
		return i >>> 4 & 15;
	}

	public static int sky(int i) {
		return i >>> 20 & 15;
	}

	public static int lightCoordsWithEmission(int i, int j) {
		if (j == 0) {
			return i;
		} else {
			int k = Math.max(sky(i), j);
			int l = Math.max(block(i), j);
			return pack(l, k);
		}
	}
}
