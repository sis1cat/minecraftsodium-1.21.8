package net.caffeinemc.mods.sodium.client.platform.windows.api.d3dkmt;

import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

class D3DKMTAdapterInfoStruct extends Struct<D3DKMTAdapterInfoStruct> {
   public static final int SIZEOF;
   public static final int ALIGNOF;
   private static final int OFFSET_HADAPTER;
   private static final int OFFSET_ADAPTER_LUID;
   private static final int OFFSET_NUM_OF_SOURCES;
   private static final int OFFSET_PRECISE_PRESENT_REGIONS_PREFERRED;

   D3DKMTAdapterInfoStruct(long address, ByteBuffer container) {
      super(address, container);
   }

   protected D3DKMTAdapterInfoStruct create(long address, ByteBuffer container) {
      return new D3DKMTAdapterInfoStruct(address, container);
   }

   public static D3DKMTAdapterInfoStruct create(long address) {
      return new D3DKMTAdapterInfoStruct(address, null);
   }

   public static D3DKMTAdapterInfoStruct.Buffer calloc(int count) {
      return new D3DKMTAdapterInfoStruct.Buffer(MemoryUtil.nmemCalloc(count, SIZEOF), count);
   }

   public int getAdapterHandle() {
      return MemoryUtil.memGetInt(this.address + OFFSET_HADAPTER);
   }

   public int sizeof() {
      return SIZEOF;
   }

   static {
      Layout layout = __struct(new Member[]{__member(4), __member(8, 4), __member(4), __member(4)});
      SIZEOF = layout.getSize();
      ALIGNOF = layout.getAlignment();
      OFFSET_HADAPTER = layout.offsetof(0);
      OFFSET_ADAPTER_LUID = layout.offsetof(1);
      OFFSET_NUM_OF_SOURCES = layout.offsetof(2);
      OFFSET_PRECISE_PRESENT_REGIONS_PREFERRED = layout.offsetof(3);
   }

   static class Buffer extends StructBuffer<D3DKMTAdapterInfoStruct, D3DKMTAdapterInfoStruct.Buffer> {
      private static final D3DKMTAdapterInfoStruct ELEMENT_FACTORY = D3DKMTAdapterInfoStruct.create(-1L);

      protected Buffer(long address, int capacity) {
         super(address, null, -1, 0, capacity, capacity);
      }

      @NotNull
      protected D3DKMTAdapterInfoStruct getElementFactory() {
         return ELEMENT_FACTORY;
      }

      @NotNull
      protected D3DKMTAdapterInfoStruct.Buffer self() {
         return this;
      }
   }
}
