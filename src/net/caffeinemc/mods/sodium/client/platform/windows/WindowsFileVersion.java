package net.caffeinemc.mods.sodium.client.platform.windows;

import net.caffeinemc.mods.sodium.client.platform.windows.api.version.VersionFixedFileInfoStruct;
import org.jetbrains.annotations.NotNull;

public record WindowsFileVersion(int x, int y, int z, int w) {
   @NotNull
   public static WindowsFileVersion fromFileVersion(VersionFixedFileInfoStruct fileVersion) {
      int x = fileVersion.getFileVersionMostSignificantBits() >>> 16 & 65535;
      int y = fileVersion.getFileVersionMostSignificantBits() >>> 0 & 65535;
      int z = fileVersion.getFileVersionLeastSignificantBits() >>> 16 & 65535;
      int w = fileVersion.getFileVersionLeastSignificantBits() >>> 0 & 65535;
      return new WindowsFileVersion(x, y, z, w);
   }

   @Override
   public String toString() {
      return "%s.%s.%s.%s".formatted(this.x, this.y, this.z, this.w);
   }
}
