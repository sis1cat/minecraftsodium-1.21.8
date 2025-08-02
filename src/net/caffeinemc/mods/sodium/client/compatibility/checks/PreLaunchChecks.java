package net.caffeinemc.mods.sodium.client.compatibility.checks;

import net.caffeinemc.mods.sodium.client.platform.PlatformHelper;
import org.lwjgl.Version;

public class PreLaunchChecks {
   private static final String REQUIRED_LWJGL_VERSION = "3.3.3";

   public static void checkEnvironment() {
      if (BugChecks.ISSUE_2561) {
         checkLwjglRuntimeVersion();
      }
   }

   private static void checkLwjglRuntimeVersion() {
      if (!isUsingKnownCompatibleLwjglVersion()) {
         String advice;
         if (isUsingPrismLauncher()) {
            advice = "It appears you are using Prism Launcher to start the game. You can likely fix this problem by opening your instance settings and navigating to the Versionsection in the sidebar.";
         } else {
            advice = "You must change the LWJGL version in your launcher to continue. This is usually controlled by the settings for a profile or instance in your launcher.";
         }

         String message = "The game failed to start because the currently active LWJGL version is not compatible.\n\nInstalled version: ###CURRENT_VERSION###\nRequired version: ###REQUIRED_VERSION###\n\n###ADVICE_STRING###"
            .replace("###CURRENT_VERSION###", Version.getVersion())
            .replace("###REQUIRED_VERSION###", "3.3.3")
            .replace("###ADVICE_STRING###", advice);
         PlatformHelper.showCriticalErrorAndClose(
            null, "Sodium Renderer - Unsupported LWJGL", message, "https://link.caffeinemc.net/help/sodium/runtime-issue/lwjgl3/gh-2561"
         );
      }
   }

   private static boolean isUsingKnownCompatibleLwjglVersion() {
      return Version.getVersion().startsWith("3.3.3");
   }

   private static boolean isUsingPrismLauncher() {
      return getLauncherBrand().equalsIgnoreCase("PrismLauncher");
   }

   private static String getLauncherBrand() {
      return System.getProperty("minecraft.launcher.brand", "unknown");
   }
}
