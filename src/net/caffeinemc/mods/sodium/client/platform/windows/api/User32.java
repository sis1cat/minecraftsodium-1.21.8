package net.caffeinemc.mods.sodium.client.platform.windows.api;

import net.caffeinemc.mods.sodium.client.platform.windows.api.msgbox.MsgBoxParamSw;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.JNI;
import org.lwjgl.system.SharedLibrary;

public class User32 {
   private static final SharedLibrary LIBRARY = APIUtil.apiCreateLibrary("user32");
   private static final long PFN_MessageBoxIndirectW = APIUtil.apiGetFunctionAddress(LIBRARY, "MessageBoxIndirectW");

   public static void callMessageBoxIndirectW(MsgBoxParamSw params) {
      JNI.callPI(params.address(), PFN_MessageBoxIndirectW);
   }
}
