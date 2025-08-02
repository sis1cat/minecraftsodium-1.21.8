package net.caffeinemc.mods.sodium.client.platform.windows.api.d3dkmt;

import java.nio.ByteBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;

public class D3DKMTOpenGLInfoStruct extends Struct<D3DKMTOpenGLInfoStruct> {
   private static final int MAX_PATH = 260;
   private static final int SIZEOF;
   private static final int ALIGNOF;
   private static final int OFFSET_UMD_OPENGL_ICD_FILE_NAME;
   private static final int OFFSET_VERSION;
   private static final int OFFSET_FLAGS;

   private D3DKMTOpenGLInfoStruct(long address, @Nullable ByteBuffer container) {
      super(address, container);
   }

   @NotNull
   protected D3DKMTOpenGLInfoStruct create(long address, ByteBuffer container) {
      return new D3DKMTOpenGLInfoStruct(address, container);
   }

   public static D3DKMTOpenGLInfoStruct calloc() {
      return new D3DKMTOpenGLInfoStruct(MemoryUtil.nmemCalloc(1L, SIZEOF), null);
   }

   public static D3DKMTOpenGLInfoStruct calloc(MemoryStack stack) {
      return new D3DKMTOpenGLInfoStruct(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
   }

   public ByteBuffer getUserModeDriverFileNameBuffer() {
      return MemoryUtil.memByteBuffer(this.address + OFFSET_UMD_OPENGL_ICD_FILE_NAME, 520);
   }

   @Nullable
   public String getUserModeDriverFileName() {
      ByteBuffer name = this.getUserModeDriverFileNameBuffer();
      int length = MemoryUtil.memLengthNT2(name);
      return length == 0 ? null : MemoryUtil.memUTF16(MemoryUtil.memAddress(name), length >> 1);
   }

   public int getVersion() {
      return MemoryUtil.memGetInt(this.address + OFFSET_VERSION);
   }

   public int getFlags() {
      return MemoryUtil.memGetInt(this.address + OFFSET_FLAGS);
   }

   public int sizeof() {
      return SIZEOF;
   }

   static {
      Layout layout = __struct(new Member[]{__member(520, 2), __member(4), __member(4)});
      SIZEOF = layout.getSize();
      ALIGNOF = layout.getAlignment();
      OFFSET_UMD_OPENGL_ICD_FILE_NAME = layout.offsetof(0);
      OFFSET_VERSION = layout.offsetof(1);
      OFFSET_FLAGS = layout.offsetof(2);
   }
}
