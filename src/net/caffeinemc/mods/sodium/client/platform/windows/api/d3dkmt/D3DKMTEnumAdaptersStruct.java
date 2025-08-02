package net.caffeinemc.mods.sodium.client.platform.windows.api.d3dkmt;

import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;

class D3DKMTEnumAdaptersStruct extends Struct<D3DKMTEnumAdaptersStruct> {
   private static final int SIZEOF;
   private static final int ALIGNOF;
   private static final int MAX_ENUM_ADAPTERS = 16;
   private static final int OFFSET_NUM_ADAPTERS;
   private static final int OFFSET_ADAPTERS;

   private D3DKMTEnumAdaptersStruct(long address, @Nullable ByteBuffer container) {
      super(address, container);
   }

   @NotNull
   protected D3DKMTEnumAdaptersStruct create(long address, ByteBuffer container) {
      return new D3DKMTEnumAdaptersStruct(address, container);
   }

   public static D3DKMTEnumAdaptersStruct calloc(MemoryStack stack) {
      return new D3DKMTEnumAdaptersStruct(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
   }

   public D3DKMTAdapterInfoStruct.Buffer getAdapters() {
      return new D3DKMTAdapterInfoStruct.Buffer(this.address + OFFSET_ADAPTERS, MemoryUtil.memGetInt(this.address + OFFSET_NUM_ADAPTERS));
   }

   public int sizeof() {
      return SIZEOF;
   }

   static {
      Layout layout = __struct(new Member[]{__member(4), __member(D3DKMTAdapterInfoStruct.SIZEOF * 16, D3DKMTAdapterInfoStruct.ALIGNOF)});
      SIZEOF = layout.getSize();
      ALIGNOF = layout.getAlignment();
      OFFSET_NUM_ADAPTERS = layout.offsetof(0);
      OFFSET_ADAPTERS = layout.offsetof(1);
   }
}
