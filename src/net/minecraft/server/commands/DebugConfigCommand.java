package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.Nullable;

public class DebugConfigCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("debugconfig")
				.requires(Commands.hasPermission(3))
				.then(
					Commands.literal("config")
						.then(
							Commands.argument("target", EntityArgument.player())
								.executes(commandContext -> config(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "target")))
						)
				)
				.then(
					Commands.literal("unconfig")
						.then(
							Commands.argument("target", UuidArgument.uuid())
								.suggests(
									(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(getUuidsInConfig(commandContext.getSource().getServer()), suggestionsBuilder)
								)
								.executes(commandContext -> unconfig(commandContext.getSource(), UuidArgument.getUuid(commandContext, "target")))
						)
				)
				.then(
					Commands.literal("dialog")
						.then(
							Commands.argument("target", UuidArgument.uuid())
								.suggests(
									(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(getUuidsInConfig(commandContext.getSource().getServer()), suggestionsBuilder)
								)
								.then(
									Commands.argument("dialog", ResourceOrIdArgument.dialog(commandBuildContext))
										.executes(
											commandContext -> showDialog(
												(CommandSourceStack)commandContext.getSource(),
												UuidArgument.getUuid(commandContext, "target"),
												ResourceOrIdArgument.getDialog(commandContext, "dialog")
											)
										)
								)
						)
				)
		);
	}

	private static Iterable<String> getUuidsInConfig(MinecraftServer minecraftServer) {
		Set<String> set = new HashSet();

		for (Connection connection : minecraftServer.getConnection().getConnections()) {
			if (connection.getPacketListener() instanceof ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl) {
				set.add(serverConfigurationPacketListenerImpl.getOwner().getId().toString());
			}
		}

		return set;
	}

	private static int config(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer) {
		GameProfile gameProfile = serverPlayer.getGameProfile();
		serverPlayer.connection.switchToConfig();
		commandSourceStack.sendSuccess(() -> Component.literal("Switched player " + gameProfile.getName() + "(" + gameProfile.getId() + ") to config mode"), false);
		return 1;
	}

	@Nullable
	private static ServerConfigurationPacketListenerImpl findConfigPlayer(MinecraftServer minecraftServer, UUID uUID) {
		for (Connection connection : minecraftServer.getConnection().getConnections()) {
			if (connection.getPacketListener() instanceof ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl
				&& serverConfigurationPacketListenerImpl.getOwner().getId().equals(uUID)) {
				return serverConfigurationPacketListenerImpl;
			}
		}

		return null;
	}

	private static int unconfig(CommandSourceStack commandSourceStack, UUID uUID) {
		ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl = findConfigPlayer(commandSourceStack.getServer(), uUID);
		if (serverConfigurationPacketListenerImpl != null) {
			serverConfigurationPacketListenerImpl.returnToWorld();
			return 1;
		} else {
			commandSourceStack.sendFailure(Component.literal("Can't find player to unconfig"));
			return 0;
		}
	}

	private static int showDialog(CommandSourceStack commandSourceStack, UUID uUID, Holder<Dialog> holder) {
		ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl = findConfigPlayer(commandSourceStack.getServer(), uUID);
		if (serverConfigurationPacketListenerImpl != null) {
			serverConfigurationPacketListenerImpl.send(new ClientboundShowDialogPacket(holder));
			return 1;
		} else {
			commandSourceStack.sendFailure(Component.literal("Can't find player to talk to"));
			return 0;
		}
	}
}
