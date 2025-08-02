package net.minecraft.world;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum Difficulty implements StringRepresentable {
	PEACEFUL(0, "peaceful"),
	EASY(1, "easy"),
	NORMAL(2, "normal"),
	HARD(3, "hard");

	public static final StringRepresentable.EnumCodec<Difficulty> CODEC = StringRepresentable.fromEnum(Difficulty::values);
	private static final IntFunction<Difficulty> BY_ID = ByIdMap.continuous(Difficulty::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
	public static final StreamCodec<ByteBuf, Difficulty> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Difficulty::getId);
	private final int id;
	private final String key;

	private Difficulty(final int j, final String string2) {
		this.id = j;
		this.key = string2;
	}

	public int getId() {
		return this.id;
	}

	public Component getDisplayName() {
		return Component.translatable("options.difficulty." + this.key);
	}

	public Component getInfo() {
		return Component.translatable("options.difficulty." + this.key + ".info");
	}

	@Deprecated
	public static Difficulty byId(int i) {
		return (Difficulty)BY_ID.apply(i);
	}

	@Nullable
	public static Difficulty byName(String string) {
		return (Difficulty)CODEC.byName(string);
	}

	public String getKey() {
		return this.key;
	}

	@Override
	public String getSerializedName() {
		return this.key;
	}
}
