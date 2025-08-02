package net.caffeinemc.mods.sodium.client.model.quad;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import net.minecraft.core.Direction;

public interface ModelQuadViewMutable extends ModelQuadView {
   void setX(int var1, float var2);

   void setY(int var1, float var2);

   void setZ(int var1, float var2);

   void setColor(int var1, int var2);

   void setTexU(int var1, float var2);

   void setTexV(int var1, float var2);

   void setLight(int var1, int var2);

   void setNormal(int var1, int var2);

   void setFaceNormal(int var1);

   void setFlags(int var1);

   void setSprite(TextureAtlasSprite var1);

   void setLightFace(Direction var1);

   void setTintIndex(int var1);
}
