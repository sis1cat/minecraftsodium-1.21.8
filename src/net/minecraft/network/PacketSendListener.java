package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFutureListener;
import java.util.function.Supplier;
import net.minecraft.network.protocol.Packet;
import org.slf4j.Logger;

public class PacketSendListener {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static ChannelFutureListener thenRun(Runnable runnable) {
		return channelFuture -> {
			runnable.run();
			if (!channelFuture.isSuccess()) {
				channelFuture.channel().pipeline().fireExceptionCaught(channelFuture.cause());
			}
		};
	}

	public static ChannelFutureListener exceptionallySend(Supplier<Packet<?>> supplier) {
		return channelFuture -> {
			if (!channelFuture.isSuccess()) {
				Packet<?> packet = (Packet<?>)supplier.get();
				if (packet != null) {
					LOGGER.warn("Failed to deliver packet, sending fallback {}", packet.type(), channelFuture.cause());
					channelFuture.channel().writeAndFlush(packet, channelFuture.channel().voidPromise());
				} else {
					channelFuture.channel().pipeline().fireExceptionCaught(channelFuture.cause());
				}
			}
		};
	}
}
