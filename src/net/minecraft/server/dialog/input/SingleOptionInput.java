package net.minecraft.server.dialog.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.ExtraCodecs;

public record SingleOptionInput(int width, List<SingleOptionInput.Entry> entries, Component label, boolean labelVisible) implements InputControl {
	public static final MapCodec<SingleOptionInput> MAP_CODEC = RecordCodecBuilder.<SingleOptionInput>mapCodec(
			instance -> instance.group(
					Dialog.WIDTH_CODEC.optionalFieldOf("width", 200).forGetter(SingleOptionInput::width),
					ExtraCodecs.nonEmptyList(SingleOptionInput.Entry.CODEC.listOf()).fieldOf("options").forGetter(SingleOptionInput::entries),
					ComponentSerialization.CODEC.fieldOf("label").forGetter(SingleOptionInput::label),
					Codec.BOOL.optionalFieldOf("label_visible", true).forGetter(SingleOptionInput::labelVisible)
				)
				.apply(instance, SingleOptionInput::new)
		)
		.validate(singleOptionInput -> {
			long l = singleOptionInput.entries.stream().filter(SingleOptionInput.Entry::initial).count();
			return l > 1L ? DataResult.error(() -> "Multiple initial values") : DataResult.success(singleOptionInput);
		});

	@Override
	public MapCodec<SingleOptionInput> mapCodec() {
		return MAP_CODEC;
	}

	public Optional<SingleOptionInput.Entry> initial() {
		return this.entries.stream().filter(SingleOptionInput.Entry::initial).findFirst();
	}

	public record Entry(String id, Optional<Component> display, boolean initial) {
		public static final Codec<SingleOptionInput.Entry> FULL_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codec.STRING.fieldOf("id").forGetter(SingleOptionInput.Entry::id),
					ComponentSerialization.CODEC.optionalFieldOf("display").forGetter(SingleOptionInput.Entry::display),
					Codec.BOOL.optionalFieldOf("initial", false).forGetter(SingleOptionInput.Entry::initial)
				)
				.apply(instance, SingleOptionInput.Entry::new)
		);
		public static final Codec<SingleOptionInput.Entry> CODEC = Codec.withAlternative(
			FULL_CODEC, Codec.STRING, string -> new SingleOptionInput.Entry(string, Optional.empty(), false)
		);

		public Component displayOrDefault() {
			return (Component)this.display.orElseGet(() -> Component.literal(this.id));
		}
	}
}
