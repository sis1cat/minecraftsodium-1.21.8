package net.caffeinemc.mods.sodium.client.platform.windows.api.d3dkmt;

import java.nio.ByteBuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;

class D3DKMTAdapterRegistryInfoStruct extends Struct<D3DKMTAdapterRegistryInfoStruct> {
   private static final int MAX_PATH = 260;
   private static final int SIZEOF;
   private static final int ALIGNOF;
   private static final int OFFSET_ADAPTER_STRING;
   private static final int OFFSET_BIOS_STRING;
   private static final int OFFSET_DAC_TYPE;
   private static final int OFFSET_CHIP_TYPE;

   private D3DKMTAdapterRegistryInfoStruct(long address, ByteBuffer container) {
      super(address, container);
   }

   protected D3DKMTAdapterRegistryInfoStruct create(long address, ByteBuffer container) {
      return new D3DKMTAdapterRegistryInfoStruct(address, container);
   }

   public static D3DKMTAdapterRegistryInfoStruct calloc(MemoryStack stack) {
      return new D3DKMTAdapterRegistryInfoStruct(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
   }

   @Nullable
   public String getAdapterString() {
      return getString(this.address + OFFSET_ADAPTER_STRING);
   }

   @Nullable
   private static String getString(long ptr) {
      ByteBuffer buf = MemoryUtil.memByteBuffer(ptr, 520);
      int len = MemoryUtil.memLengthNT2(buf) >> 1;
      return len == 0 ? null : MemoryUtil.memUTF16(buf, len);
   }

   public int sizeof() {
      return SIZEOF;
   }

   static {
      Layout layout = __struct(new Member[]{__member(520, 2), __member(520, 2), __member(520, 2), __member(520, 2)});
      SIZEOF = layout.getSize();
      ALIGNOF = layout.getAlignment();
      OFFSET_ADAPTER_STRING = layout.offsetof(0);
      OFFSET_BIOS_STRING = layout.offsetof(1);
      OFFSET_DAC_TYPE = layout.offsetof(2);
      OFFSET_CHIP_TYPE = layout.offsetof(3);
   }
}
