package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.VisibleForDebug;
import org.jetbrains.annotations.Nullable;

public interface ProtocolInfo<T extends PacketListener> {
	ConnectionProtocol id();

	PacketFlow flow();

	StreamCodec<ByteBuf, Packet<? super T>> codec();

	@Nullable
	BundlerInfo bundlerInfo();

	public interface Details {
		ConnectionProtocol id();

		PacketFlow flow();

		@VisibleForDebug
		void listPackets(ProtocolInfo.Details.PacketVisitor packetVisitor);

		@FunctionalInterface
		public interface PacketVisitor {
			void accept(PacketType<?> packetType, int i);
		}
	}

	public interface DetailsProvider {
		ProtocolInfo.Details details();
	}
}
