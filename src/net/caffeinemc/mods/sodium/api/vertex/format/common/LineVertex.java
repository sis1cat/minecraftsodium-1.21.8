package net.caffeinemc.mods.sodium.api.vertex.format.common;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.NormalAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;

public final class LineVertex {
   public static final VertexFormat FORMAT = DefaultVertexFormat.POSITION_COLOR_NORMAL;
   public static final int STRIDE = 20;
   private static final int OFFSET_POSITION = 0;
   private static final int OFFSET_COLOR = 12;
   private static final int OFFSET_NORMAL = 16;

   public static void put(long ptr, float x, float y, float z, int color, int normal) {
      PositionAttribute.put(ptr + 0L, x, y, z);
      ColorAttribute.set(ptr + 12L, color);
      NormalAttribute.set(ptr + 16L, normal);
   }
}
