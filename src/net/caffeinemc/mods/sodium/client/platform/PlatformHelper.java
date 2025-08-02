package net.caffeinemc.mods.sodium.client.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformHelper {
   private static final Logger LOGGER = LoggerFactory.getLogger("Sodium-EarlyDriverScanner");

   public static void showCriticalErrorAndClose(
      @Nullable NativeWindowHandle window, @NotNull String messageTitle, @NotNull String messageBody, @NotNull String helpUrl
   ) {
      LOGGER.error(
         "###ERROR_DESCRIPTION###\n\nFor more information, please see: ###HELP_URL###"
            .replace("###ERROR_DESCRIPTION###", messageBody)
            .replace("###HELP_URL###", helpUrl)
      );
      MessageBox.showMessageBox(window, MessageBox.IconType.ERROR, messageTitle, messageBody, helpUrl);
      System.exit(1);
   }
}
