package net.caffeinemc.mods.sodium.client.gl.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.caffeinemc.mods.sodium.client.gl.GlObject;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;

public class GlShader extends GlObject {
   private static final Logger LOGGER = LogManager.getLogger(GlShader.class);
   private final ResourceLocation name;

   public GlShader(ShaderType type, ResourceLocation name, String src) {
      this.name = name;
      int handle = GL20C.glCreateShader(type.id);
      ShaderWorkarounds.safeShaderSource(handle, src);
      GL20C.glCompileShader(handle);
      String log = GL20C.glGetShaderInfoLog(handle);
      if (!log.isEmpty()) {
         LOGGER.warn("Shader compilation log for " + this.name + ": " + log);
      }

      int result = GlStateManager.glGetShaderi(handle, 35713);
      if (result != 1) {
         throw new RuntimeException("Shader compilation failed, see log for details");
      } else {
         this.setHandle(handle);
      }
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public void delete() {
      GL20C.glDeleteShader(this.handle());
      this.invalidateHandle();
   }
}
