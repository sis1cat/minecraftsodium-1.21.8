package net.minecraft.world.level.block.state.properties;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum TestBlockMode implements StringRepresentable {
	START(0, "start"),
	LOG(1, "log"),
	FAIL(2, "fail"),
	ACCEPT(3, "accept");

	private static final IntFunction<TestBlockMode> BY_ID = ByIdMap.continuous(testBlockMode -> testBlockMode.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	public static final Codec<TestBlockMode> CODEC = StringRepresentable.fromEnum(TestBlockMode::values);
	public static final StreamCodec<ByteBuf, TestBlockMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, testBlockMode -> testBlockMode.id);
	private final int id;
	private final String name;
	private final Component displayName;
	private final Component detailedMessage;

	private TestBlockMode(final int j, final String string2) {
		this.id = j;
		this.name = string2;
		this.displayName = Component.translatable("test_block.mode." + string2);
		this.detailedMessage = Component.translatable("test_block.mode_info." + string2);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	public Component getDetailedMessage() {
		return this.detailedMessage;
	}
}
