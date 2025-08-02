package net.caffeinemc.mods.sodium.client.gl.arena.staging;

import java.nio.ByteBuffer;
import net.caffeinemc.mods.sodium.client.gl.buffer.GlBuffer;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;

public interface StagingBuffer {
   void enqueueCopy(CommandList var1, ByteBuffer var2, GlBuffer var3, long var4);

   void flush(CommandList var1);

   void delete(CommandList var1);

   void flip();
}
