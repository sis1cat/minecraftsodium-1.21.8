package net.caffeinemc.mods.sodium.client.gui;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.caffeinemc.mods.sodium.client.util.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.client.GraphicsStatus;
import org.jetbrains.annotations.NotNull;

public class SodiumGameOptions {
   private static final String DEFAULT_FILE_NAME = "sodium-options.json";
   public final SodiumGameOptions.QualitySettings quality = new SodiumGameOptions.QualitySettings();
   public final SodiumGameOptions.AdvancedSettings advanced = new SodiumGameOptions.AdvancedSettings();
   public final SodiumGameOptions.PerformanceSettings performance = new SodiumGameOptions.PerformanceSettings();
   public final SodiumGameOptions.NotificationSettings notifications = new SodiumGameOptions.NotificationSettings();
   @NotNull
   public SodiumGameOptions.DebugSettings debug = new SodiumGameOptions.DebugSettings();
   private boolean readOnly;
   private static final Gson GSON = new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .setPrettyPrinting()
      .excludeFieldsWithModifiers(new int[]{2})
      .create();

   private SodiumGameOptions() {
   }

   public static SodiumGameOptions defaults() {
      return new SodiumGameOptions();
   }

   public static SodiumGameOptions loadFromDisk() {
      Path path = getConfigPath();
      SodiumGameOptions config;
      if (Files.exists(path)) {
         try (FileReader reader = new FileReader(path.toFile())) {
            config = (SodiumGameOptions)GSON.fromJson(reader, SodiumGameOptions.class);
         } catch (IOException var8) {
            throw new RuntimeException("Could not parse config", var8);
         }
      } else {
         config = new SodiumGameOptions();
      }

      try {
         writeToDisk(config);
         return config;
      } catch (IOException var6) {
         throw new RuntimeException("Couldn't update config file", var6);
      }
   }

   private static Path getConfigPath() {
      return Path.of("./config/sodium-options.json");//PlatformRuntimeInformation.getInstance().getConfigDirectory().resolve("sodium-options.json");
   }

   public static void writeToDisk(SodiumGameOptions config) throws IOException {
      if (config.isReadOnly()) {
         throw new IllegalStateException("Config file is read-only");
      } else {
         Path path = getConfigPath();
         Path dir = path.getParent();
         if (!Files.exists(dir)) {
            Files.createDirectories(dir);
         } else if (!Files.isDirectory(dir)) {
            throw new IOException("Not a directory: " + dir);
         }

         FileUtil.writeTextRobustly(GSON.toJson(config), path);
      }
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }

   public void setReadOnly() {
      this.readOnly = true;
   }

   public static class AdvancedSettings {
      public boolean enableMemoryTracing = false;
      public boolean useAdvancedStagingBuffers = true;
      public int cpuRenderAheadLimit = 3;
   }

   public static class DebugSettings {
      public boolean terrainSortingEnabled = true;

      @Deprecated(
         forRemoval = true
      )
      public SortBehavior getSortBehavior() {
         if (PlatformRuntimeInformation.getInstance().isDevelopmentEnvironment()) {
            return this.terrainSortingEnabled ? SortBehavior.DYNAMIC_DEFER_NEARBY_ZERO_FRAMES : SortBehavior.OFF;
         } else {
            return SortBehavior.DYNAMIC_DEFER_NEARBY_ZERO_FRAMES;
         }
      }
   }

   public static enum GraphicsQuality implements TextProvider {
      DEFAULT("options.gamma.default"),
      FANCY("options.clouds.fancy"),
      FAST("options.clouds.fast");

      private final Component name;

      private GraphicsQuality(String name) {
         this.name = Component.translatable(name);
      }

      @Override
      public Component getLocalizedName() {
         return this.name;
      }

      public boolean isFancy(GraphicsStatus graphicsStatus) {
         return this == FANCY || this == DEFAULT && (graphicsStatus == GraphicsStatus.FANCY || graphicsStatus == GraphicsStatus.FABULOUS);
      }
   }

   public static class NotificationSettings {
      public boolean hasClearedDonationButton = false;
      public boolean hasSeenDonationPrompt = false;
   }

   public static class PerformanceSettings {
      public int chunkBuilderThreads = 0;
      @SerializedName("always_defer_chunk_updates_v2")
      public boolean alwaysDeferChunkUpdates = true;
      public boolean animateOnlyVisibleTextures = true;
      public boolean useEntityCulling = true;
      public boolean useFogOcclusion = true;
      public boolean useBlockFaceCulling = true;
      public boolean useNoErrorGLContext = true;
   }

   public static class QualitySettings {
      public SodiumGameOptions.GraphicsQuality weatherQuality = SodiumGameOptions.GraphicsQuality.DEFAULT;
      public SodiumGameOptions.GraphicsQuality leavesQuality = SodiumGameOptions.GraphicsQuality.DEFAULT;
      public boolean enableVignette = true;
   }
}
