package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class NbtContents implements ComponentContents {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<NbtContents> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				Codec.STRING.fieldOf("nbt").forGetter(NbtContents::getNbtPath),
				Codec.BOOL.lenientOptionalFieldOf("interpret", false).forGetter(NbtContents::isInterpreting),
				ComponentSerialization.CODEC.lenientOptionalFieldOf("separator").forGetter(NbtContents::getSeparator),
				DataSource.CODEC.forGetter(NbtContents::getDataSource)
			)
			.apply(instance, NbtContents::new)
	);
	public static final ComponentContents.Type<NbtContents> TYPE = new ComponentContents.Type<>(CODEC, "nbt");
	private final boolean interpreting;
	private final Optional<Component> separator;
	private final String nbtPathPattern;
	private final DataSource dataSource;
	@Nullable
	protected final NbtPathArgument.NbtPath compiledNbtPath;

	public NbtContents(String string, boolean bl, Optional<Component> optional, DataSource dataSource) {
		this(string, compileNbtPath(string), bl, optional, dataSource);
	}

	private NbtContents(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, Optional<Component> optional, DataSource dataSource) {
		this.nbtPathPattern = string;
		this.compiledNbtPath = nbtPath;
		this.interpreting = bl;
		this.separator = optional;
		this.dataSource = dataSource;
	}

	@Nullable
	private static NbtPathArgument.NbtPath compileNbtPath(String string) {
		try {
			return new NbtPathArgument().parse(new StringReader(string));
		} catch (CommandSyntaxException var2) {
			return null;
		}
	}

	public String getNbtPath() {
		return this.nbtPathPattern;
	}

	public boolean isInterpreting() {
		return this.interpreting;
	}

	public Optional<Component> getSeparator() {
		return this.separator;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public boolean equals(Object object) {
		return this == object
			? true
			: object instanceof NbtContents nbtContents
				&& this.dataSource.equals(nbtContents.dataSource)
				&& this.separator.equals(nbtContents.separator)
				&& this.interpreting == nbtContents.interpreting
				&& this.nbtPathPattern.equals(nbtContents.nbtPathPattern);
	}

	public int hashCode() {
		int i = this.interpreting ? 1 : 0;
		i = 31 * i + this.separator.hashCode();
		i = 31 * i + this.nbtPathPattern.hashCode();
		return 31 * i + this.dataSource.hashCode();
	}

	public String toString() {
		return "nbt{" + this.dataSource + ", interpreting=" + this.interpreting + ", separator=" + this.separator + "}";
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (commandSourceStack != null && this.compiledNbtPath != null) {
			Stream<Tag> stream = this.dataSource.getData(commandSourceStack).flatMap(compoundTag -> {
				try {
					return this.compiledNbtPath.get(compoundTag).stream();
				} catch (CommandSyntaxException var3) {
					return Stream.empty();
				}
			});
			if (this.interpreting) {
				RegistryOps<Tag> registryOps = commandSourceStack.registryAccess().createSerializationContext(NbtOps.INSTANCE);
				Component component = DataFixUtils.orElse(
					ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR
				);
				return (MutableComponent)stream.flatMap(tag -> {
					try {
						Component componentx = ComponentSerialization.CODEC.parse(registryOps, tag).getOrThrow();
						return Stream.of(ComponentUtils.updateForEntity(commandSourceStack, componentx, entity, i));
					} catch (Exception var6x) {
						LOGGER.warn("Failed to parse component: {}", tag, var6x);
						return Stream.of();
					}
				}).reduce((mutableComponent, mutableComponent2) -> mutableComponent.append(component).append(mutableComponent2)).orElseGet(Component::empty);
			} else {
				Stream<String> stream2 = stream.map(NbtContents::asString);
				return (MutableComponent)ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i)
					.map(
						mutableComponent -> (MutableComponent)stream2.map(Component::literal)
							.reduce((mutableComponent2, mutableComponent3) -> mutableComponent2.append(mutableComponent).append(mutableComponent3))
							.orElseGet(Component::empty)
					)
					.orElseGet(() -> Component.literal((String)stream2.collect(Collectors.joining(", "))));
			}
		} else {
			return Component.empty();
		}
	}

	private static String asString(Tag tag) {
		return tag instanceof StringTag(String var5) ? var5 : tag.toString();
	}

	@Override
	public ComponentContents.Type<?> type() {
		return TYPE;
	}
}
