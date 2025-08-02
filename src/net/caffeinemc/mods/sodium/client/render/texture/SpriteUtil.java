package net.caffeinemc.mods.sodium.client.render.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import org.jetbrains.annotations.Nullable;

@Deprecated(
   forRemoval = true
)
public class SpriteUtil {
   @Deprecated(
      forRemoval = true
   )
   public static void markSpriteActive(@Nullable TextureAtlasSprite sprite) {
      if (sprite != null) {
         net.caffeinemc.mods.sodium.api.texture.SpriteUtil.INSTANCE.markSpriteActive(sprite);
      }
   }

   @Deprecated(
      forRemoval = true
   )
   public static boolean hasAnimation(@Nullable TextureAtlasSprite sprite) {
      return sprite != null ? net.caffeinemc.mods.sodium.api.texture.SpriteUtil.INSTANCE.hasAnimation(sprite) : false;
   }
}
