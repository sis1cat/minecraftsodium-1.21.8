package net.caffeinemc.mods.sodium.client.compatibility.environment.probe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.caffeinemc.mods.sodium.client.compatibility.environment.OsUtils;
import net.caffeinemc.mods.sodium.client.platform.windows.api.d3dkmt.D3DKMT;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphicsAdapterProbe {
   private static final Logger LOGGER = LoggerFactory.getLogger("Sodium-GraphicsAdapterProbe");
   private static final Set<String> LINUX_PCI_CLASSES = Set.of("0x030000", "0x030001", "0x030200", "0x038000");
   private static List<? extends GraphicsAdapterInfo> ADAPTERS = List.of();

   public static void findAdapters() {
      LOGGER.info("Searching for graphics cards...");

      List<? extends GraphicsAdapterInfo> adapters;
      try {
         adapters = switch (OsUtils.getOs()) {
            case WIN -> findAdapters$Windows();
            case LINUX -> findAdapters$Linux();
            default -> null;
         };
      } catch (Exception var3) {
         LOGGER.error("Failed to find graphics adapters!", var3);
         return;
      }

      if (adapters != null) {
         if (adapters.isEmpty()) {
            LOGGER.warn(
               "Could not find any graphics adapters! Probably the device is not on a bus we can probe, or there are no devices supporting 3D acceleration."
            );
         } else {
            for (GraphicsAdapterInfo adapter : adapters) {
               LOGGER.info("Found graphics adapter: {}", adapter);
            }
         }

         ADAPTERS = adapters;
      }
   }

   private static List<? extends GraphicsAdapterInfo> findAdapters$Windows() {
      return D3DKMT.findGraphicsAdapters();
   }

   private static List<? extends GraphicsAdapterInfo> findAdapters$Linux() {
      ArrayList<GraphicsAdapterInfo> results = new ArrayList<>();

      try (Stream<Path> devices = Files.list(Path.of("/sys/bus/pci/devices/"))) {
         Iterable<Path> devicesIter = devices::iterator;
         for (Path devicePath : devicesIter) {
            String deviceClass = Files.readString(devicePath.resolve("class")).trim();
            if (LINUX_PCI_CLASSES.contains(deviceClass)) {
               String pciVendorId = Files.readString(devicePath.resolve("vendor")).trim();
               String pciDeviceId = Files.readString(devicePath.resolve("device")).trim();
               GraphicsAdapterVendor adapterVendor = GraphicsAdapterVendor.fromPciVendorId(pciVendorId);
               String adapterName = getPciDeviceName$Linux(pciVendorId, pciDeviceId);
               if (adapterName == null) {
                  adapterName = "<unknown>";
               }

               GraphicsAdapterInfo.LinuxPciAdapterInfo info = new GraphicsAdapterInfo.LinuxPciAdapterInfo(adapterVendor, adapterName, pciVendorId, pciDeviceId);
               results.add(info);
            }
         }
      } catch (IOException var13) {
      }

      return results;
   }

   @Nullable
   private static String getPciDeviceName$Linux(String vendorId, String deviceId) {
      String deviceFilter = vendorId.substring(2) + ":" + deviceId.substring(2);

      try {
         Process process = Runtime.getRuntime().exec(new String[]{"lspci", "-vmm", "-d", deviceFilter});
         int result = process.waitFor();
         if (result != 0) {
            throw new IOException("lspci exited with error code: %s".formatted(result));
         } else {
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
               while ((line = reader.readLine()) != null) {
                  if (line.startsWith("Device:")) {
                     return line.substring("Device:".length()).trim();
                  }
               }
            }

            throw new IOException("lspci did not return a device name");
         }
      } catch (Throwable var10) {
         LOGGER.warn("Failed to query PCI device name for %s:%s".formatted(vendorId, deviceId), var10);
         return null;
      }
   }

   public static Collection<? extends GraphicsAdapterInfo> getAdapters() {
      if (ADAPTERS == null) {
         LOGGER.error("Graphics adapters not probed yet; returning an empty list.");
         return Collections.emptyList();
      } else {
         return ADAPTERS;
      }
   }
}
