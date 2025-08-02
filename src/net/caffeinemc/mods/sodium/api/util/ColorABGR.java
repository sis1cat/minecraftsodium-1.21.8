package net.caffeinemc.mods.sodium.api.util;

import java.nio.ByteOrder;

public class ColorABGR implements ColorU8 {
   private static final int RED_COMPONENT_OFFSET = 0;
   private static final int GREEN_COMPONENT_OFFSET = 8;
   private static final int BLUE_COMPONENT_OFFSET = 16;
   private static final int ALPHA_COMPONENT_OFFSET = 24;
   private static final int RED_COMPONENT_MASK = 255;
   private static final int GREEN_COMPONENT_MASK = 65280;
   private static final int BLUE_COMPONENT_MASK = 16711680;
   private static final int ALPHA_COMPONENT_MASK = -16777216;
   private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

   public static int pack(float r, float g, float b) {
      return pack(r, g, b, 255.0F);
   }

   public static int pack(int r, int g, int b, int a) {
      return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | (r & 0xFF) << 0;
   }

   public static int withAlpha(int rgb, float alpha) {
      return withAlpha(rgb, ColorU8.normalizedFloatToByte(alpha));
   }

   public static int withAlpha(int rgb, int alpha) {
      return alpha << 24 | rgb & 16777215;
   }

   public static int pack(float r, float g, float b, float a) {
      return pack(ColorU8.normalizedFloatToByte(r), ColorU8.normalizedFloatToByte(g), ColorU8.normalizedFloatToByte(b), ColorU8.normalizedFloatToByte(a));
   }

   public static int unpackRed(int color) {
      return color >> 0 & 0xFF;
   }

   public static int unpackGreen(int color) {
      return color >> 8 & 0xFF;
   }

   public static int unpackBlue(int color) {
      return color >> 16 & 0xFF;
   }

   public static int unpackAlpha(int color) {
      return color >> 24 & 0xFF;
   }

   public static int mulRGB(int color, int factor) {
      return ColorMixer.mul(color, factor) & 16777215 | color & 0xFF000000;
   }

   public static int mulRGB(int color, float factor) {
      return mulRGB(color, ColorU8.normalizedFloatToByte(factor));
   }

   public static int fromNativeByteOrder(int color) {
      return BIG_ENDIAN ? Integer.reverseBytes(color) : color;
   }

   public static int toNativeByteOrder(int color) {
      return BIG_ENDIAN ? Integer.reverseBytes(color) : color;
   }
}
