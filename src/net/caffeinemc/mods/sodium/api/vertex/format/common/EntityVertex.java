package net.caffeinemc.mods.sodium.api.vertex.format.common;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.LightAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.NormalAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.OverlayAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.TextureAttribute;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;

public final class EntityVertex {
   public static final VertexFormat FORMAT = DefaultVertexFormat.NEW_ENTITY;
   public static final int STRIDE = 36;
   private static final int OFFSET_POSITION = 0;
   private static final int OFFSET_COLOR = 12;
   private static final int OFFSET_TEXTURE = 16;
   private static final int OFFSET_OVERLAY = 24;
   private static final int OFFSET_LIGHT = 28;
   private static final int OFFSET_NORMAL = 32;

   public static void write(long ptr, float x, float y, float z, int color, float u, float v, int overlay, int light, int normal) {
      PositionAttribute.put(ptr + 0L, x, y, z);
      ColorAttribute.set(ptr + 12L, color);
      TextureAttribute.put(ptr + 16L, u, v);
      OverlayAttribute.set(ptr + 24L, overlay);
      LightAttribute.set(ptr + 28L, light);
      NormalAttribute.set(ptr + 32L, normal);
   }
}
