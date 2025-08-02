package net.caffeinemc.mods.sodium.client.gl.functions;

import net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferStorageFlags;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBufferTarget;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.gl.util.EnumBitField;
import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.GL44C;
import org.lwjgl.opengl.GLCapabilities;

public enum BufferStorageFunctions {
   NONE {
      @Override
      public void createBufferStorage(GlBufferTarget target, long length, EnumBitField<GlBufferStorageFlags> flags) {
         throw new UnsupportedOperationException();
      }
   },
   CORE {
      @Override
      public void createBufferStorage(GlBufferTarget target, long length, EnumBitField<GlBufferStorageFlags> flags) {
         GL44C.glBufferStorage(target.getTargetParameter(), length, flags.getBitField());
      }
   },
   ARB {
      @Override
      public void createBufferStorage(GlBufferTarget target, long length, EnumBitField<GlBufferStorageFlags> flags) {
         ARBBufferStorage.glBufferStorage(target.getTargetParameter(), length, flags.getBitField());
      }
   };

   public static BufferStorageFunctions pickBest(RenderDevice device) {
      GLCapabilities capabilities = device.getCapabilities();
      if (capabilities.OpenGL44) {
         return CORE;
      } else {
         return capabilities.GL_ARB_buffer_storage ? ARB : NONE;
      }
   }

   public abstract void createBufferStorage(GlBufferTarget var1, long var2, EnumBitField<GlBufferStorageFlags> var4);
}
