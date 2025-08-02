package net.caffeinemc.mods.sodium.client.render.chunk.shader;

import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat2v;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat4v;
import net.caffeinemc.mods.sodium.client.util.FogParameters;

public abstract class ChunkShaderFogComponent {
   public abstract void setup(FogParameters var1);

   public static class None extends ChunkShaderFogComponent {
      public None(ShaderBindingContext context) {
      }

      @Override
      public void setup(FogParameters parameters) {
      }
   }

   public static class Smooth extends ChunkShaderFogComponent {
      private final GlUniformFloat4v uFogColor;
      private final GlUniformFloat2v uEnvironmentFog;
      private final GlUniformFloat2v uRenderFog;

      public Smooth(ShaderBindingContext context) {
         this.uFogColor = context.bindUniform("u_FogColor", GlUniformFloat4v::new);
         this.uEnvironmentFog = context.bindUniform("u_EnvironmentFog", GlUniformFloat2v::new);
         this.uRenderFog = context.bindUniform("u_RenderFog", GlUniformFloat2v::new);
      }

      @Override
      public void setup(FogParameters fogParameters) {
         this.uFogColor.set(fogParameters.red(), fogParameters.green(), fogParameters.blue(), fogParameters.alpha());
         this.uEnvironmentFog.set(fogParameters.environmentalStart(), fogParameters.environmentalEnd());
         this.uRenderFog.set(fogParameters.renderStart(), fogParameters.renderEnd());
      }
   }
}
