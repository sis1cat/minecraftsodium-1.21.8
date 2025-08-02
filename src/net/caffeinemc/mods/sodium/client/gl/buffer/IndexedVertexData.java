package net.caffeinemc.mods.sodium.client.gl.buffer;

import net.caffeinemc.mods.sodium.client.gl.attribute.GlVertexFormat;
import net.caffeinemc.mods.sodium.client.util.NativeBuffer;

public record IndexedVertexData(GlVertexFormat vertexFormat, NativeBuffer vertexBuffer, NativeBuffer indexBuffer) {
   public void delete() {
      this.vertexBuffer.free();
      this.indexBuffer.free();
   }
}
