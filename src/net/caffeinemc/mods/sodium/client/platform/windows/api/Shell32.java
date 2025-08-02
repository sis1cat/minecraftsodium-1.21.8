package net.caffeinemc.mods.sodium.client.platform.windows.api;

import java.util.Objects;
import net.caffeinemc.mods.sodium.client.platform.NativeWindowHandle;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.SharedLibrary;

public class Shell32 {
   private static final SharedLibrary LIBRARY = APIUtil.apiCreateLibrary("shell32");
   private static final long PFN_ShellExecuteW = APIUtil.apiGetFunctionAddressOptional(LIBRARY, "ShellExecuteW");

   public static void browseUrl(@Nullable NativeWindowHandle window, String url) {
      Objects.requireNonNull(url, "URL parameter must be non-null");
      MemoryStack stack = MemoryStack.stackPush();

      try {
         stack.nUTF16("open", true);
         long lpOperation = stack.getPointerAddress();
         stack.nUTF16(url, true);
         long lpFile = stack.getPointerAddress();
         nShellExecuteW(window != null ? window.getWin32Handle() : 0L, lpOperation, lpFile, 0L, 0L, 1);
      } catch (Throwable var8) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public static long nShellExecuteW(long hwnd, long lpOperation, long lpFile, long lpParameters, long lpDirectory, int nShowCmd) {
      return JNI.invokePPPPPP(hwnd, lpOperation, lpFile, lpParameters, lpDirectory, nShowCmd, checkPfn(PFN_ShellExecuteW));
   }

   private static long checkPfn(long pfn) {
      if (pfn == 0L) {
         throw new NullPointerException("Function pointer not available");
      } else {
         return pfn;
      }
   }
}
