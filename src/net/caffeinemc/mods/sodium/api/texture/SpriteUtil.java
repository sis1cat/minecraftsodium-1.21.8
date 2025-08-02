package net.caffeinemc.mods.sodium.api.texture;

import net.caffeinemc.mods.sodium.api.internal.DependencyInjection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.random.WeightedList.Flat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public interface SpriteUtil {
   SpriteUtil INSTANCE = DependencyInjection.load(SpriteUtil.class, "net.caffeinemc.mods.sodium.client.render.texture.SpriteUtilImpl");

   void markSpriteActive(@NotNull TextureAtlasSprite var1);

   boolean hasAnimation(@NotNull TextureAtlasSprite var1);
}
