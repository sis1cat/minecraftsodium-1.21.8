package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractPackResources implements PackResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final PackLocationInfo location;

	protected AbstractPackResources(PackLocationInfo packLocationInfo) {
		this.location = packLocationInfo;
	}

	@Nullable
	@Override
	public <T> T getMetadataSection(MetadataSectionType<T> metadataSectionType) throws IOException {
		IoSupplier<InputStream> ioSupplier = this.getRootResource(new String[]{"pack.mcmeta"});
		if (ioSupplier == null) {
			return null;
		} else {
			InputStream inputStream = ioSupplier.get();

			Object var4;
			try {
				var4 = getMetadataFromStream(metadataSectionType, inputStream);
			} catch (Throwable var7) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}
				}

				throw var7;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return (T)var4;
		}
	}

	@Nullable
	public static <T> T getMetadataFromStream(MetadataSectionType<T> metadataSectionType, InputStream inputStream) {
		JsonObject jsonObject;
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

			try {
				jsonObject = GsonHelper.parse(bufferedReader);
			} catch (Throwable var7) {
				try {
					bufferedReader.close();
				} catch (Throwable var6) {
					var7.addSuppressed(var6);
				}

				throw var7;
			}

			bufferedReader.close();
		} catch (Exception var8) {
			LOGGER.error("Couldn't load {} metadata", metadataSectionType.name(), var8);
			return null;
		}

		return (T)(!jsonObject.has(metadataSectionType.name())
			? null
			: metadataSectionType.codec()
				.parse(JsonOps.INSTANCE, jsonObject.get(metadataSectionType.name()))
				.ifError(error -> LOGGER.error("Couldn't load {} metadata: {}", metadataSectionType.name(), error))
				.result()
				.orElse(null));
	}

	@Override
	public PackLocationInfo location() {
		return this.location;
	}
}
