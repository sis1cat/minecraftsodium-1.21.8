package net.caffeinemc.mods.sodium.client.render.chunk.terrain;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

import static net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses.*;

public class TerrainRenderPass {

   private final RenderPipeline renderType;
   private final boolean isTranslucent;
   private final boolean fragmentDiscard;
   private final boolean useMipmaps;

   public TerrainRenderPass(RenderPipeline renderType, boolean isTranslucent, boolean allowFragmentDiscard, boolean useMipmaps) {
      this.renderType = renderType;
      this.isTranslucent = isTranslucent;
      this.fragmentDiscard = allowFragmentDiscard;
      this.useMipmaps = useMipmaps;
   }

   public boolean isTranslucent() {
      return this.isTranslucent;
   }

   public boolean supportsFragmentDiscard() {
      return this.fragmentDiscard;
   }

   public RenderPipeline getPipeline() {
      return renderType;
   }

   public RenderTarget getTarget() {

      Minecraft minecraft = Minecraft.getInstance();

       if (this.equals(TRANSLUCENT)) {
           RenderTarget renderTarget = minecraft.levelRenderer.getTranslucentTarget();
           return renderTarget != null ? renderTarget : minecraft.getMainRenderTarget();
       }

       return minecraft.getMainRenderTarget();

   }

   public GpuTextureView getAtlas() {
      TextureManager textureManager = Minecraft.getInstance().getTextureManager();
      AbstractTexture abstractTexture = textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS);
      abstractTexture.setUseMipmaps(this.useMipmaps);
      return abstractTexture.getTextureView();
   }
}
