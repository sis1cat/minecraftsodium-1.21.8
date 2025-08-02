package net.caffeinemc.mods.sodium.client.model.light.flat;

import java.util.Arrays;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FilterMask.Type;

public class FlatLightPipeline implements LightPipeline {
   private final LightDataAccess lightCache;

   public FlatLightPipeline(LightDataAccess lightCache) {
      this.lightCache = lightCache;
   }

   @Override
   public void calculate(ModelQuadView quad, BlockPos pos, QuadLightData out, Direction cullFace, Direction lightFace, boolean shade, boolean enhanced) {
      int lightmap;
      if (cullFace != null) {
         lightmap = this.getOffsetLightmap(pos, cullFace);
         Arrays.fill(out.br, this.lightCache.getLevel().getShade(lightFace, shade));
      } else {
         int flags = quad.getFlags();
         if ((flags & 4) == 0 && ((flags & 2) == 0 || !LightDataAccess.unpackFC(this.lightCache.get(pos)))) {
            lightmap = LightDataAccess.getEmissiveLightmap(this.lightCache.get(pos));
            Arrays.fill(
               out.br,
               enhanced
                  ? PlatformBlockAccess.getInstance().getNormalVectorShade(quad, this.lightCache.getLevel(), shade)
                  : this.lightCache.getLevel().getShade(lightFace, shade)
            );
         } else {
            lightmap = this.getOffsetLightmap(pos, lightFace);
            Arrays.fill(out.br, this.lightCache.getLevel().getShade(lightFace, shade));
         }
      }

      Arrays.fill(out.lm, lightmap);
   }

   private int getOffsetLightmap(BlockPos pos, Direction face) {
      int word = this.lightCache.get(pos);
      if (LightDataAccess.unpackEM(word)) {
         return 15728880;
      } else {
         int adjWord = this.lightCache.get(pos, face);
         return LightTexture.pack(Math.max(LightDataAccess.unpackBL(adjWord), LightDataAccess.unpackLU(word)), LightDataAccess.unpackSL(adjWord));
      }
   }
}
