package net.caffeinemc.mods.sodium.client.world;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;

public interface LevelRendererExtension {
   SodiumWorldRenderer sodium$getWorldRenderer();

   void sodium$setMatrices(ChunkRenderMatrices var1);

   ChunkRenderMatrices sodium$getMatrices();
}
