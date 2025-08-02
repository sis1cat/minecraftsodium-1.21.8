package net.caffeinemc.mods.sodium.client.render.chunk.compile;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.minecraft.client.Minecraft;
import net.minecraft.server.commands.SaveAllCommand;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.world.level.Level;

public class ChunkBuildContext {
   public final ChunkBuildBuffers buffers;
   public final BlockRenderCache cache;

   public ChunkBuildContext(Level level, ChunkVertexType vertexType) {
      this.buffers = new ChunkBuildBuffers(vertexType);
      this.cache = new BlockRenderCache(Minecraft.getInstance(), level);
   }

   public void cleanup() {
      this.buffers.destroy();
      this.cache.cleanup();
   }
}
