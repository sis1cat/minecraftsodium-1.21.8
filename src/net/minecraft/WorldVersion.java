package net.minecraft;

import java.util.Date;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.DataVersion;

public interface WorldVersion {
	DataVersion dataVersion();

	String id();

	String name();

	int protocolVersion();

	int packVersion(PackType packType);

	Date buildTime();

	boolean stable();

	public record Simple(
		String id, String name, DataVersion dataVersion, int protocolVersion, int resourcePackVersion, int datapackVersion, Date buildTime, boolean stable
	) implements WorldVersion {
		@Override
		public int packVersion(PackType packType) {
			return switch (packType) {
				case CLIENT_RESOURCES -> this.resourcePackVersion;
				case SERVER_DATA -> this.datapackVersion;
			};
		}
	}
}
