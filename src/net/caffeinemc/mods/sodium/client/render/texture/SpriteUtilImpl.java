package net.caffeinemc.mods.sodium.client.render.texture;

import java.util.Objects;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import org.jetbrains.annotations.NotNull;

public class SpriteUtilImpl implements net.caffeinemc.mods.sodium.api.texture.SpriteUtil {
   @Override
   public void markSpriteActive(@NotNull TextureAtlasSprite sprite) {
      Objects.requireNonNull(sprite);
      ((SpriteContentsExtension)sprite.contents()).sodium$setActive(true);
   }

   @Override
   public boolean hasAnimation(@NotNull TextureAtlasSprite sprite) {
      Objects.requireNonNull(sprite);
      return ((SpriteContentsExtension)sprite.contents()).sodium$hasAnimation();
   }
}
