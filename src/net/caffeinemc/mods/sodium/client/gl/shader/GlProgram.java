package net.caffeinemc.mods.sodium.client.gl.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.caffeinemc.mods.sodium.client.gl.GlObject;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniform;
import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformBlock;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;

public class GlProgram<T> extends GlObject implements ShaderBindingContext {
   private static final Logger LOGGER = LogManager.getLogger(GlProgram.class);
   private final T shaderInterface;

   protected GlProgram(int program, Function<ShaderBindingContext, T> interfaceFactory) {
      this.setHandle(program);
      this.shaderInterface = interfaceFactory.apply(this);
   }

   public T getInterface() {
      return this.shaderInterface;
   }

   public static GlProgram.Builder builder(ResourceLocation name) {
      return new GlProgram.Builder(name);
   }

   public void bind() {
      GL20C.glUseProgram(this.handle());
   }

   public void unbind() {
      GL20C.glUseProgram(0);
   }

   public void delete() {
      GL20C.glDeleteProgram(this.handle());
      this.invalidateHandle();
   }

   @NotNull
   @Override
   public <U extends GlUniform<?>> U bindUniform(String name, IntFunction<U> factory) {
      int index = GL20C.glGetUniformLocation(this.handle(), name);
      if (index < 0) {
         throw new NullPointerException("No uniform exists with name: " + name);
      } else {
         return factory.apply(index);
      }
   }

   @Override
   public <U extends GlUniform<?>> U bindUniformOptional(String name, IntFunction<U> factory) {
      int index = GL20C.glGetUniformLocation(this.handle(), name);
      return index < 0 ? null : factory.apply(index);
   }

   @NotNull
   @Override
   public GlUniformBlock bindUniformBlock(String name, int bindingPoint) {
      int index = GL32C.glGetUniformBlockIndex(this.handle(), name);
      if (index < 0) {
         throw new NullPointerException("No uniform block exists with name: " + name);
      } else {
         GL32C.glUniformBlockBinding(this.handle(), index, bindingPoint);
         return new GlUniformBlock(bindingPoint);
      }
   }

   @Override
   public GlUniformBlock bindUniformBlockOptional(String name, int bindingPoint) {
      int index = GL32C.glGetUniformBlockIndex(this.handle(), name);
      if (index < 0) {
         return null;
      } else {
         GL32C.glUniformBlockBinding(this.handle(), index, bindingPoint);
         return new GlUniformBlock(bindingPoint);
      }
   }

   public static class Builder {
      private final ResourceLocation name;
      private final int program;

      public Builder(ResourceLocation name) {
         this.name = name;
         this.program = GL20C.glCreateProgram();
      }

      public GlProgram.Builder attachShader(GlShader shader) {
         GL20C.glAttachShader(this.program, shader.handle());
         return this;
      }

      public <U> GlProgram<U> link(Function<ShaderBindingContext, U> factory) {
         GL20C.glLinkProgram(this.program);
         String log = GL20C.glGetProgramInfoLog(this.program);
         if (!log.isEmpty()) {
            GlProgram.LOGGER.warn("Program link log for " + this.name + ": " + log);
         }

         int result = GlStateManager.glGetProgrami(this.program, 35714);
         if (result != 1) {
            throw new RuntimeException("Shader program linking failed, see log for details");
         } else {
            return new GlProgram<>(this.program, factory);
         }
      }

      public GlProgram.Builder bindAttribute(String name, int index) {
         GL20C.glBindAttribLocation(this.program, index, name);
         return this;
      }

      public GlProgram.Builder bindFragmentData(String name, int index) {
         GL30C.glBindFragDataLocation(this.program, index, name);
         return this;
      }
   }
}
