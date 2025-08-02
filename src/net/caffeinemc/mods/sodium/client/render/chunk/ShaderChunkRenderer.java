package net.caffeinemc.mods.sodium.client.render.chunk;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.gl.shader.GlProgram;
import net.caffeinemc.mods.sodium.client.gl.shader.GlShader;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderConstants;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderType;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceLocation;

public abstract class ShaderChunkRenderer implements ChunkRenderer {
   private final Map<ChunkShaderOptions, GlProgram<ChunkShaderInterface>> programs = new Object2ObjectOpenHashMap<>();
   protected final ChunkVertexType vertexType;
   protected final GlVertexFormat vertexFormat;
   protected final RenderDevice device;
   protected GlProgram<ChunkShaderInterface> activeProgram;

   public ShaderChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
      this.device = device;
      this.vertexType = vertexType;
      this.vertexFormat = vertexType.getVertexFormat();
   }

   protected GlProgram<ChunkShaderInterface> compileProgram(ChunkShaderOptions options) {
      GlProgram<ChunkShaderInterface> program = this.programs.get(options);
      if (program == null) {
         this.programs.put(options, program = this.createShader("blocks/block_layer_opaque", options));
      }

      return program;
   }

   private GlProgram<ChunkShaderInterface> createShader(String path, ChunkShaderOptions options) {
      ShaderConstants constants = options.constants();
      GlShader vertShader = ShaderLoader.loadShader(ShaderType.VERTEX, ResourceLocation.fromNamespaceAndPath("sodium", path + ".vsh"), constants);
      GlShader fragShader = ShaderLoader.loadShader(ShaderType.FRAGMENT, ResourceLocation.fromNamespaceAndPath("sodium", path + ".fsh"), constants);

      GlProgram var6;
      try {
         var6 = GlProgram.builder(ResourceLocation.fromNamespaceAndPath("sodium", "chunk_shader"))
            .attachShader(vertShader)
            .attachShader(fragShader)
            .bindAttribute("a_Position", 0)
            .bindAttribute("a_Color", 1)
            .bindAttribute("a_TexCoord", 2)
            .bindAttribute("a_LightAndData", 3)
            .bindFragmentData("fragColor", 0)
            .link(shader -> new DefaultShaderInterface(shader, options));
      } finally {
         vertShader.delete();
         fragShader.delete();
      }

      return var6;
   }

   protected void begin(TerrainRenderPass pass, FogParameters parameters) {
      RenderTarget target = pass.getTarget();
      GlStateManager._viewport(0, 0, target.getColorTexture().getWidth(0), target.getColorTexture().getHeight(0));
      GlStateManager._glBindFramebuffer(
         36160, ((GlTexture)target.getColorTexture()).getFbo(((GlDevice)RenderSystem.getDevice()).directStateAccess(), target.getDepthTexture())
      );
      ((GlCommandEncoder)RenderSystem.getDevice().createCommandEncoder()).sodium$applyPipelineState(pass.getPipeline());
      ((GlCommandEncoder)RenderSystem.getDevice().createCommandEncoder()).sodium$setLastProgram(null);
      ChunkShaderOptions options = new ChunkShaderOptions(ChunkFogMode.SMOOTH, pass, this.vertexType);
      this.activeProgram = this.compileProgram(options);
      this.activeProgram.bind();
      this.activeProgram.getInterface().setupState(pass, parameters);
   }

   protected void end(TerrainRenderPass pass) {
      this.activeProgram.getInterface().resetState();
      this.activeProgram.unbind();
      this.activeProgram = null;
   }

   @Override
   public void delete(CommandList commandList) {
      this.programs.values().forEach(GlProgram::delete);
   }
}
