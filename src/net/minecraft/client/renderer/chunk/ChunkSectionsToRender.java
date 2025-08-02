package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.util.SodiumChunkSection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;

@Environment(EnvType.CLIENT)
public class ChunkSectionsToRender implements SodiumChunkSection {

	private final EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> drawsPerLayer;
	private final int maxIndicesRequired;
	private final GpuBufferSlice[] dynamicTransforms;


	private SodiumWorldRenderer renderer;

	private ChunkRenderMatrices matrices;

	private double x;

	private double y;

	private double z;

	public ChunkSectionsToRender(EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> drawsPerLayer, int maxIndicesRequired, GpuBufferSlice[] dynamicTransforms) {
		this.drawsPerLayer = drawsPerLayer;
		this.maxIndicesRequired = maxIndicesRequired;
		this.dynamicTransforms = dynamicTransforms;
	}

	public void renderGroup(ChunkSectionLayerGroup chunkSectionLayerGroup) {

		if (this.renderer != null) {
			RenderDevice.enterManagedCode();

			try {
				this.renderer.drawChunkLayer(chunkSectionLayerGroup, this.matrices, this.x, this.y, this.z);
			} finally {
				RenderDevice.exitManagedCode();
			}
			return;
		}

		RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		GpuBuffer gpuBuffer = this.maxIndicesRequired == 0 ? null : autoStorageIndexBuffer.getBuffer(this.maxIndicesRequired);
		VertexFormat.IndexType indexType = this.maxIndicesRequired == 0 ? null : autoStorageIndexBuffer.type();
		ChunkSectionLayer[] chunkSectionLayers = chunkSectionLayerGroup.layers();
		Minecraft minecraft = Minecraft.getInstance();
		boolean bl = false;
		RenderTarget renderTarget = chunkSectionLayerGroup.outputTarget();

		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(
					() -> "Section layers for " + chunkSectionLayerGroup.label(),
					renderTarget.getColorTextureView(),
					OptionalInt.empty(),
					renderTarget.getDepthTextureView(),
					OptionalDouble.empty()
				)) {
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.bindSampler("Sampler2", minecraft.gameRenderer.lightTexture().getTextureView());

			for (ChunkSectionLayer chunkSectionLayer : chunkSectionLayers) {
				List<RenderPass.Draw<GpuBufferSlice[]>> list = (List<RenderPass.Draw<GpuBufferSlice[]>>)this.drawsPerLayer.get(chunkSectionLayer);
				if (!list.isEmpty()) {
					if (chunkSectionLayer == ChunkSectionLayer.TRANSLUCENT) {
						list = list.reversed();
					}

					renderPass.setPipeline(bl ? RenderPipelines.WIREFRAME : chunkSectionLayer.pipeline());
					renderPass.bindSampler("Sampler0", chunkSectionLayer.textureView());
					renderPass.drawMultipleIndexed(list, gpuBuffer, indexType, List.of("DynamicTransforms"), this.dynamicTransforms);
				}
			}
		}
	}

	@Override
	public void sodium$setRendering(SodiumWorldRenderer renderer, ChunkRenderMatrices matrices, double x, double y, double z) {
		this.renderer = renderer;
		this.matrices = matrices;
		this.x = x;
		this.y = y;
		this.z = z;
	}

}
