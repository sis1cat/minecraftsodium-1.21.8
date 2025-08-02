package net.caffeinemc.mods.sodium.api.vertex.format.common;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.ColorAttribute;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.PositionAttribute;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import org.joml.Matrix4f;

public final class ColorVertex {
   public static final VertexFormat FORMAT = DefaultVertexFormat.POSITION_COLOR;
   public static final int STRIDE = 16;
   private static final int OFFSET_POSITION = 0;
   private static final int OFFSET_COLOR = 12;

   public static void put(long ptr, Matrix4f matrix, float x, float y, float z, int color) {
      float xt = MatrixHelper.transformPositionX(matrix, x, y, z);
      float yt = MatrixHelper.transformPositionY(matrix, x, y, z);
      float zt = MatrixHelper.transformPositionZ(matrix, x, y, z);
      put(ptr, xt, yt, zt, color);
   }

   public static void put(long ptr, float x, float y, float z, int color) {
      PositionAttribute.put(ptr + 0L, x, y, z);
      ColorAttribute.set(ptr + 12L, color);
   }
}
