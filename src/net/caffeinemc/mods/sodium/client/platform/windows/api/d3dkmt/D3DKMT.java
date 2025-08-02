package net.caffeinemc.mods.sodium.client.platform.windows.api.d3dkmt;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.caffeinemc.mods.sodium.client.compatibility.environment.probe.GraphicsAdapterInfo;
import net.caffeinemc.mods.sodium.client.compatibility.environment.probe.GraphicsAdapterVendor;
import net.caffeinemc.mods.sodium.client.platform.windows.WindowsFileVersion;
import net.caffeinemc.mods.sodium.client.platform.windows.api.Gdi32;
import net.caffeinemc.mods.sodium.client.platform.windows.api.version.Version;
import net.caffeinemc.mods.sodium.client.platform.windows.api.version.VersionFixedFileInfoStruct;
import net.caffeinemc.mods.sodium.client.platform.windows.api.version.VersionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class D3DKMT {
   private static final Logger LOGGER = LoggerFactory.getLogger("Sodium-D3DKMT");

   public static List<D3DKMT.WDDMAdapterInfo> findGraphicsAdapters() {
      if (!Gdi32.isD3DKMTSupported()) {
         LOGGER.warn("Unable to query graphics adapters when the operating system is older than Windows 8.0.");
         return List.of();
      } else {
         MemoryStack stack = MemoryStack.stackPush();

         ArrayList var3;
         try {
            D3DKMTEnumAdaptersStruct adapters = D3DKMTEnumAdaptersStruct.calloc(stack);
            apiCheckError("D3DKMTEnumAdapters", Gdi32.nD3DKMTEnumAdapters(adapters.address()));
            D3DKMTAdapterInfoStruct.Buffer adapterInfoBuffer = adapters.getAdapters();

            try {
               var3 = queryAdapters(adapterInfoBuffer);
            } finally {
               freeAdapters(adapterInfoBuffer);
            }
         } catch (Throwable var10) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var8) {
                  var10.addSuppressed(var8);
               }
            }

            throw var10;
         }

         if (stack != null) {
            stack.close();
         }

         return var3;
      }
   }

   @NotNull
   private static ArrayList<D3DKMT.WDDMAdapterInfo> queryAdapters(@NotNull D3DKMTAdapterInfoStruct.Buffer adapterInfoBuffer) {
      ArrayList<D3DKMT.WDDMAdapterInfo> results = new ArrayList<>();

      for (int adapterIndex = adapterInfoBuffer.position(); adapterIndex < adapterInfoBuffer.limit(); adapterIndex++) {
         D3DKMTAdapterInfoStruct pAdapterInfo = (D3DKMTAdapterInfoStruct)adapterInfoBuffer.get(adapterIndex);
         int pAdapter = pAdapterInfo.getAdapterHandle();
         D3DKMT.WDDMAdapterInfo parsed = getAdapterInfo(pAdapter);
         if (parsed != null) {
            results.add(parsed);
         }
      }

      return results;
   }

   private static void freeAdapters(@NotNull D3DKMTAdapterInfoStruct.Buffer adapterInfoBuffer) {
      for (int adapterIndex = adapterInfoBuffer.position(); adapterIndex < adapterInfoBuffer.limit(); adapterIndex++) {
         D3DKMTAdapterInfoStruct adapterInfo = (D3DKMTAdapterInfoStruct)adapterInfoBuffer.get(adapterIndex);
         apiCheckError("D3DKMTCloseAdapter", d3dkmtCloseAdapter(adapterInfo.getAdapterHandle()));
      }
   }

   @Nullable
   private static D3DKMT.WDDMAdapterInfo getAdapterInfo(int adapter) {
      int adapterType = queryAdapterType(adapter);
      if (!isSupportedAdapterType(adapterType)) {
         return null;
      } else {
         String adapterName = queryFriendlyName(adapter);
         String driverFileName = queryDriverFileName(adapter);
         WindowsFileVersion driverVersion = null;
         GraphicsAdapterVendor driverVendor = GraphicsAdapterVendor.UNKNOWN;
         if (driverFileName != null) {
            driverVersion = queryDriverVersion(driverFileName);
            driverVendor = GraphicsAdapterVendor.fromIcdName(getOpenGlIcdName(driverFileName));
         }

         return new D3DKMT.WDDMAdapterInfo(driverVendor, adapterName, adapterType, driverFileName, driverVersion);
      }
   }

   private static boolean isSupportedAdapterType(int adapterType) {
      return (adapterType & 1) == 0 ? false : (adapterType & 4) == 0;
   }

   @Nullable
   private static String queryDriverFileName(int adapter) {
      MemoryStack stack = MemoryStack.stackPush();

      String var3;
      try {
         D3DKMTOpenGLInfoStruct info = D3DKMTOpenGLInfoStruct.calloc(stack);
         d3dkmtQueryAdapterInfo(adapter, 2, MemoryUtil.memByteBuffer(info));
         var3 = info.getUserModeDriverFileName();
      } catch (Throwable var5) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (stack != null) {
         stack.close();
      }

      return var3;
   }

   @Nullable
   private static WindowsFileVersion queryDriverVersion(String file) {
      VersionInfo version = Version.getModuleFileVersion(file);
      if (version == null) {
         return null;
      } else {
         VersionFixedFileInfoStruct fileVersion = version.queryFixedFileInfo();
         return fileVersion == null ? null : WindowsFileVersion.fromFileVersion(fileVersion);
      }
   }

   @NotNull
   private static String queryFriendlyName(int adapter) {
      MemoryStack stack = MemoryStack.stackPush();

      String var4;
      try {
         D3DKMTAdapterRegistryInfoStruct registryInfo = D3DKMTAdapterRegistryInfoStruct.calloc(stack);
         d3dkmtQueryAdapterInfo(adapter, 8, MemoryUtil.memByteBuffer(registryInfo));
         String name = registryInfo.getAdapterString();
         if (name == null) {
            name = "<unknown>";
         }

         var4 = name;
      } catch (Throwable var6) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stack != null) {
         stack.close();
      }

      return var4;
   }

   private static int queryAdapterType(int adapter) {
      MemoryStack stack = MemoryStack.stackPush();

      int var3;
      try {
         IntBuffer info = stack.callocInt(1);
         d3dkmtQueryAdapterInfo(adapter, 15, MemoryUtil.memByteBuffer(info));
         var3 = info.get(0);
      } catch (Throwable var5) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (stack != null) {
         stack.close();
      }

      return var3;
   }

   private static void d3dkmtQueryAdapterInfo(int adapter, int type, ByteBuffer holder) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         D3DKMTQueryAdapterInfoStruct info = D3DKMTQueryAdapterInfoStruct.malloc(stack);
         info.setAdapterHandle(adapter);
         info.setType(type);
         info.setDataPointer(MemoryUtil.memAddress(holder));
         info.setDataLength(holder.remaining());
         apiCheckError("D3DKMTQueryAdapterInfo", Gdi32.nd3dKmtQueryAdapterInfo(info.address()));
      } catch (Throwable var7) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (stack != null) {
         stack.close();
      }
   }

   private static int d3dkmtCloseAdapter(int handle) {
      MemoryStack stack = MemoryStack.stackPush();

      int var3;
      try {
         IntBuffer info = stack.ints(handle);
         var3 = Gdi32.nD3DKMTCloseAdapter(MemoryUtil.memAddress(info));
      } catch (Throwable var5) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (stack != null) {
         stack.close();
      }

      return var3;
   }

   private static void apiCheckError(String name, int error) {
      if (error != 0) {
         throw new RuntimeException("%s returned non-zero result (error=%s)".formatted(name, Integer.toHexString(error)));
      }
   }

   private static String getOpenGlIcdName(@Nullable String filePath) {
      if (filePath == null) {
         return null;
      } else {
         Path fileName = Paths.get(filePath).getFileName();
         return fileName == null ? null : fileName.toString();
      }
   }

   public record WDDMAdapterInfo(
      @NotNull GraphicsAdapterVendor vendor,
      @NotNull String name,
      int adapterType,
      @Nullable String openglIcdFilePath,
      @Nullable WindowsFileVersion openglIcdVersion
   ) implements GraphicsAdapterInfo {
      @Nullable
      public String getOpenGlIcdName() {
         return D3DKMT.getOpenGlIcdName(this.openglIcdFilePath);
      }

      @Override
      public String toString() {
         return "AdapterInfo{vendor=%s, description='%s', adapterType=0x%08X, openglIcdFilePath='%s', openglIcdVersion=%s}"
            .formatted(this.vendor, this.name, this.adapterType, this.openglIcdFilePath, this.openglIcdVersion);
      }
   }
}
