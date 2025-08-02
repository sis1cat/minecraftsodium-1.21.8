package net.caffeinemc.mods.sodium.client.render.chunk.shader;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.joml.Matrix4fc;

public interface ChunkShaderInterface {
   @Deprecated
   void setupState(TerrainRenderPass var1, FogParameters var2);

   @Deprecated
   void resetState();

   void setProjectionMatrix(Matrix4fc var1);

   void setModelViewMatrix(Matrix4fc var1);

   void setRegionOffset(float var1, float var2, float var3);
}
