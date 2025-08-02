package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.PngInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_ICON_SIZE = 1024;
	public String name;
	public String ip;
	public Component status;
	public Component motd;
	@Nullable
	public ServerStatus.Players players;
	public long ping;
	public int protocol = SharedConstants.getCurrentVersion().protocolVersion();
	public Component version = Component.literal(SharedConstants.getCurrentVersion().name());
	public List<Component> playerList = Collections.emptyList();
	private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
	@Nullable
	private byte[] iconBytes;
	private ServerData.Type type;
	private ServerData.State state = ServerData.State.INITIAL;

	public ServerData(String string, String string2, ServerData.Type type) {
		this.name = string;
		this.ip = string2;
		this.type = type;
	}

	public CompoundTag write() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("name", this.name);
		compoundTag.putString("ip", this.ip);
		compoundTag.storeNullable("icon", ExtraCodecs.BASE64_STRING, this.iconBytes);
		compoundTag.store(ServerData.ServerPackStatus.FIELD_CODEC, this.packStatus);
		return compoundTag;
	}

	public ServerData.ServerPackStatus getResourcePackStatus() {
		return this.packStatus;
	}

	public void setResourcePackStatus(ServerData.ServerPackStatus serverPackStatus) {
		this.packStatus = serverPackStatus;
	}

	public static ServerData read(CompoundTag compoundTag) {
		ServerData serverData = new ServerData(compoundTag.getStringOr("name", ""), compoundTag.getStringOr("ip", ""), ServerData.Type.OTHER);
		serverData.setIconBytes((byte[])compoundTag.read("icon", ExtraCodecs.BASE64_STRING).orElse(null));
		serverData.setResourcePackStatus(
			(ServerData.ServerPackStatus)compoundTag.read(ServerData.ServerPackStatus.FIELD_CODEC).orElse(ServerData.ServerPackStatus.PROMPT)
		);
		return serverData;
	}

	@Nullable
	public byte[] getIconBytes() {
		return this.iconBytes;
	}

	public void setIconBytes(@Nullable byte[] bs) {
		this.iconBytes = bs;
	}

	public boolean isLan() {
		return this.type == ServerData.Type.LAN;
	}

	public boolean isRealm() {
		return this.type == ServerData.Type.REALM;
	}

	public ServerData.Type type() {
		return this.type;
	}

	public void copyNameIconFrom(ServerData serverData) {
		this.ip = serverData.ip;
		this.name = serverData.name;
		this.iconBytes = serverData.iconBytes;
	}

	public void copyFrom(ServerData serverData) {
		this.copyNameIconFrom(serverData);
		this.setResourcePackStatus(serverData.getResourcePackStatus());
		this.type = serverData.type;
	}

	public ServerData.State state() {
		return this.state;
	}

	public void setState(ServerData.State state) {
		this.state = state;
	}

	@Nullable
	public static byte[] validateIcon(@Nullable byte[] bs) {
		if (bs != null) {
			try {
				PngInfo pngInfo = PngInfo.fromBytes(bs);
				if (pngInfo.width() <= 1024 && pngInfo.height() <= 1024) {
					return bs;
				}
			} catch (IOException var2) {
				LOGGER.warn("Failed to decode server icon", (Throwable)var2);
			}
		}

		return null;
	}

	@Environment(EnvType.CLIENT)
	public static enum ServerPackStatus {
		ENABLED("enabled"),
		DISABLED("disabled"),
		PROMPT("prompt");

		public static final MapCodec<ServerData.ServerPackStatus> FIELD_CODEC = Codec.BOOL
			.optionalFieldOf("acceptTextures")
			.xmap(optional -> (ServerData.ServerPackStatus)optional.map(boolean_ -> boolean_ ? ENABLED : DISABLED).orElse(PROMPT), serverPackStatus -> {
				return switch (serverPackStatus) {
					case ENABLED -> Optional.of(true);
					case DISABLED -> Optional.of(false);
					case PROMPT -> Optional.empty();
				};
			});
		private final Component name;

		private ServerPackStatus(final String string2) {
			this.name = Component.translatable("addServer.resourcePack." + string2);
		}

		public Component getName() {
			return this.name;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum State {
		INITIAL,
		PINGING,
		UNREACHABLE,
		INCOMPATIBLE,
		SUCCESSFUL;
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		LAN,
		REALM,
		OTHER;
	}
}
