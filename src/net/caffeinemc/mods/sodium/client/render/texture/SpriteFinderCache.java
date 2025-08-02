package net.caffeinemc.mods.sodium.client.render.texture;

import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.server.commands.SaveAllCommand;

public class SpriteFinderCache {
   private static SpriteFinder blockAtlasSpriteFinder;

   public static SpriteFinder forBlockAtlas() {
      if (blockAtlasSpriteFinder == null) {
         blockAtlasSpriteFinder = SpriteFinder.get(Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS));
      }

      return blockAtlasSpriteFinder;
   }

   public static void resetSpriteFinder() {
      blockAtlasSpriteFinder = null;
   }
}
