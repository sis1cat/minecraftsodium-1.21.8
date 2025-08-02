package net.caffeinemc.mods.sodium.client.render.chunk;

import java.util.Collections;

import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.util.SignatureValidator;
import net.minecraft.client.renderer.SectionBufferBuilderPool;
import org.jetbrains.annotations.Nullable;

public class NonStoringBuilderPool extends SectionBufferBuilderPool {
   public NonStoringBuilderPool() {
      super(Collections.emptyList());
   }

   @Nullable
   public SectionBufferBuilderPack acquire() {
      return null;
   }

   public void release(SectionBufferBuilderPack blockBufferBuilderStorage) {
   }

   public boolean isEmpty() {
      return true;
   }

   public int getFreeBufferCount() {
      return 0;
   }
}
