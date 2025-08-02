package net.minecraft.advancements.critereon;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface MinMaxBounds<T extends Number> {
	SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
	SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));

	Optional<T> min();

	Optional<T> max();

	default boolean isAny() {
		return this.min().isEmpty() && this.max().isEmpty();
	}

	default Optional<T> unwrapPoint() {
		Optional<T> optional = this.min();
		Optional<T> optional2 = this.max();
		return optional.equals(optional2) ? optional : Optional.empty();
	}

	static <T extends Number, R extends MinMaxBounds<T>> Codec<R> createCodec(Codec<T> codec, MinMaxBounds.BoundsFactory<T, R> boundsFactory) {
		Codec<R> codec2 = RecordCodecBuilder.create(
			instance -> instance.group(codec.optionalFieldOf("min").forGetter(MinMaxBounds::min), codec.optionalFieldOf("max").forGetter(MinMaxBounds::max))
				.apply(instance, boundsFactory::create)
		);
		return Codec.either(codec2, codec)
			.xmap(either -> either.map(minMaxBounds -> minMaxBounds, number -> boundsFactory.create(Optional.of(number), Optional.of(number))), minMaxBounds -> {
				Optional<T> optional = minMaxBounds.unwrapPoint();
				return optional.isPresent() ? Either.right(optional.get()) : Either.left(minMaxBounds);
			});
	}

	static <B extends ByteBuf, T extends Number, R extends MinMaxBounds<T>> StreamCodec<B, R> createStreamCodec(
		StreamCodec<B, T> streamCodec, MinMaxBounds.BoundsFactory<T, R> boundsFactory
	) {
		return new StreamCodec<B, R>() {
			private static final int MIN_FLAG = 1;
			public static final int MAX_FLAG = 2;

			public R decode(B byteBuf) {
				byte b = byteBuf.readByte();
				Optional<T> optional = (b & 1) != 0 ? Optional.of(streamCodec.decode(byteBuf)) : Optional.empty();
				Optional<T> optional2 = (b & 2) != 0 ? Optional.of(streamCodec.decode(byteBuf)) : Optional.empty();
				return boundsFactory.create(optional, optional2);
			}

			public void encode(B byteBuf, R minMaxBounds) {
				Optional<T> optional = minMaxBounds.min();
				Optional<T> optional2 = minMaxBounds.max();
				byteBuf.writeByte((optional.isPresent() ? 1 : 0) | (optional2.isPresent() ? 2 : 0));
				optional.ifPresent(number -> streamCodec.encode(byteBuf, (T)number));
				optional2.ifPresent(number -> streamCodec.encode(byteBuf, (T)number));
			}
		};
	}

	static <T extends Number, R extends MinMaxBounds<T>> R fromReader(
		StringReader stringReader,
		MinMaxBounds.BoundsFromReaderFactory<T, R> boundsFromReaderFactory,
		Function<String, T> function,
		Supplier<DynamicCommandExceptionType> supplier,
		Function<T, T> function2
	) throws CommandSyntaxException {
		if (!stringReader.canRead()) {
			throw ERROR_EMPTY.createWithContext(stringReader);
		} else {
			int i = stringReader.getCursor();

			try {
				Optional<T> optional = readNumber(stringReader, function, supplier).map(function2);
				Optional<T> optional2;
				if (stringReader.canRead(2) && stringReader.peek() == '.' && stringReader.peek(1) == '.') {
					stringReader.skip();
					stringReader.skip();
					optional2 = readNumber(stringReader, function, supplier).map(function2);
					if (optional.isEmpty() && optional2.isEmpty()) {
						throw ERROR_EMPTY.createWithContext(stringReader);
					}
				} else {
					optional2 = optional;
				}

				if (optional.isEmpty() && optional2.isEmpty()) {
					throw ERROR_EMPTY.createWithContext(stringReader);
				} else {
					return boundsFromReaderFactory.create(stringReader, optional, optional2);
				}
			} catch (CommandSyntaxException var8) {
				stringReader.setCursor(i);
				throw new CommandSyntaxException(var8.getType(), var8.getRawMessage(), var8.getInput(), i);
			}
		}
	}

	private static <T extends Number> Optional<T> readNumber(
		StringReader stringReader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier
	) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		while (stringReader.canRead() && isAllowedInputChat(stringReader)) {
			stringReader.skip();
		}

		String string = stringReader.getString().substring(i, stringReader.getCursor());
		if (string.isEmpty()) {
			return Optional.empty();
		} else {
			try {
				return Optional.of(function.apply(string));
			} catch (NumberFormatException var6) {
				throw ((DynamicCommandExceptionType)supplier.get()).createWithContext(stringReader, string);
			}
		}
	}

	private static boolean isAllowedInputChat(StringReader stringReader) {
		char c = stringReader.peek();
		if ((c < '0' || c > '9') && c != '-') {
			return c != '.' ? false : !stringReader.canRead(2) || stringReader.peek(1) != '.';
		} else {
			return true;
		}
	}

	@FunctionalInterface
	public interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
		R create(Optional<T> optional, Optional<T> optional2);
	}

	@FunctionalInterface
	public interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
		R create(StringReader stringReader, Optional<T> optional, Optional<T> optional2) throws CommandSyntaxException;
	}

	public record Doubles(Optional<Double> min, Optional<Double> max, Optional<Double> minSq, Optional<Double> maxSq)
			implements MinMaxBounds<Double> {
		public static final MinMaxBounds.Doubles ANY = new MinMaxBounds.Doubles(Optional.empty(), Optional.empty());
		public static final Codec<MinMaxBounds.Doubles> CODEC = MinMaxBounds.<Double, Doubles>createCodec(Codec.DOUBLE, MinMaxBounds.Doubles::new);
		public static final StreamCodec<ByteBuf, MinMaxBounds.Doubles> STREAM_CODEC = MinMaxBounds.<ByteBuf, Double, Doubles>createStreamCodec(ByteBufCodecs.DOUBLE, MinMaxBounds.Doubles::new);

		private Doubles(Optional<Double> p_299492_, Optional<Double> p_300933_) {
			this(p_299492_, p_300933_, squareOpt(p_299492_), squareOpt(p_300933_));
		}

		private static MinMaxBounds.Doubles create(StringReader p_154796_, Optional<Double> p_299495_, Optional<Double> p_301206_) throws CommandSyntaxException {
			if (p_299495_.isPresent() && p_301206_.isPresent() && p_299495_.get() > p_301206_.get()) {
				throw ERROR_SWAPPED.createWithContext(p_154796_);
			} else {
				return new MinMaxBounds.Doubles(p_299495_, p_301206_);
			}
		}

		private static Optional<Double> squareOpt(Optional<Double> p_299805_) {
			return p_299805_.map(p_296138_ -> p_296138_ * p_296138_);
		}

		public static MinMaxBounds.Doubles exactly(double p_154787_) {
			return new MinMaxBounds.Doubles(Optional.of(p_154787_), Optional.of(p_154787_));
		}

		public static MinMaxBounds.Doubles between(double p_154789_, double p_154790_) {
			return new MinMaxBounds.Doubles(Optional.of(p_154789_), Optional.of(p_154790_));
		}

		public static MinMaxBounds.Doubles atLeast(double p_154805_) {
			return new MinMaxBounds.Doubles(Optional.of(p_154805_), Optional.empty());
		}

		public static MinMaxBounds.Doubles atMost(double p_154809_) {
			return new MinMaxBounds.Doubles(Optional.empty(), Optional.of(p_154809_));
		}

		public boolean matches(double p_154811_) {
			return this.min.isPresent() && this.min.get() > p_154811_ ? false : this.max.isEmpty() || !(this.max.get() < p_154811_);
		}

		public boolean matchesSqr(double p_154813_) {
			return this.minSq.isPresent() && this.minSq.get() > p_154813_ ? false : this.maxSq.isEmpty() || !(this.maxSq.get() < p_154813_);
		}

		public static MinMaxBounds.Doubles fromReader(StringReader p_154794_) throws CommandSyntaxException {
			return fromReader(p_154794_, p_154807_ -> p_154807_);
		}

		public static MinMaxBounds.Doubles fromReader(StringReader p_154800_, Function<Double, Double> p_154801_) throws CommandSyntaxException {
			return MinMaxBounds.fromReader(
					p_154800_, MinMaxBounds.Doubles::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, p_154801_
			);
		}

		@Override
		public Optional<Double> min() {
			return this.min;
		}

		@Override
		public Optional<Double> max() {
			return this.max;
		}
	}

	public record Ints(Optional<Integer> min, Optional<Integer> max, Optional<Long> minSq, Optional<Long> maxSq) implements MinMaxBounds<Integer> {
		public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints(Optional.empty(), Optional.empty());
		public static final Codec<MinMaxBounds.Ints> CODEC = MinMaxBounds.<Integer, Ints>createCodec(Codec.INT, MinMaxBounds.Ints::new);
		public static final StreamCodec<ByteBuf, MinMaxBounds.Ints> STREAM_CODEC = MinMaxBounds.<ByteBuf, Integer, Ints>createStreamCodec(ByteBufCodecs.INT, MinMaxBounds.Ints::new);


		private Ints(Optional<Integer> optional, Optional<Integer> optional2) {
			this(optional, optional2, optional.map(integer -> integer.longValue() * integer.longValue()), squareOpt(optional2));
		}

		private static MinMaxBounds.Ints create(StringReader stringReader, Optional<Integer> optional, Optional<Integer> optional2) throws CommandSyntaxException {
			if (optional.isPresent() && optional2.isPresent() && (Integer)optional.get() > (Integer)optional2.get()) {
				throw ERROR_SWAPPED.createWithContext(stringReader);
			} else {
				return new MinMaxBounds.Ints(optional, optional2);
			}
		}

		private static Optional<Long> squareOpt(Optional<Integer> optional) {
			return optional.map(integer -> integer.longValue() * integer.longValue());
		}

		public static MinMaxBounds.Ints exactly(int i) {
			return new MinMaxBounds.Ints(Optional.of(i), Optional.of(i));
		}

		public static MinMaxBounds.Ints between(int i, int j) {
			return new MinMaxBounds.Ints(Optional.of(i), Optional.of(j));
		}

		public static MinMaxBounds.Ints atLeast(int i) {
			return new MinMaxBounds.Ints(Optional.of(i), Optional.empty());
		}

		public static MinMaxBounds.Ints atMost(int i) {
			return new MinMaxBounds.Ints(Optional.empty(), Optional.of(i));
		}

		public boolean matches(int i) {
			return this.min.isPresent() && this.min.get() > i ? false : this.max.isEmpty() || (Integer)this.max.get() >= i;
		}

		public boolean matchesSqr(long l) {
			return this.minSq.isPresent() && this.minSq.get() > l ? false : this.maxSq.isEmpty() || (Long)this.maxSq.get() >= l;
		}

		public static MinMaxBounds.Ints fromReader(StringReader stringReader) throws CommandSyntaxException {
			return fromReader(stringReader, integer -> integer);
		}

		public static MinMaxBounds.Ints fromReader(StringReader stringReader, Function<Integer, Integer> function) throws CommandSyntaxException {
			return MinMaxBounds.fromReader(
				stringReader, MinMaxBounds.Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, function
			);
		}
	}
}
