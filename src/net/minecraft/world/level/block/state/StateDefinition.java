package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class StateDefinition<O, S extends StateHolder<O, S>> {
	static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
	private final O owner;
	private final ImmutableSortedMap<String, Property<?>> propertiesByName;
	private final ImmutableList<S> states;

	protected StateDefinition(Function<O, S> pStateValueFunction, O pOwner, StateDefinition.Factory<O, S> pValueFunction, Map<String, Property<?>> pPropertiesByName) {
		this.owner = pOwner;
		this.propertiesByName = ImmutableSortedMap.copyOf(pPropertiesByName);
		Supplier<S> supplier = () -> pStateValueFunction.apply(pOwner);
		MapCodec<S> mapcodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));

		for (Entry<String, Property<?>> entry : this.propertiesByName.entrySet()) {
			mapcodec = appendPropertyCodec(mapcodec, supplier, entry.getKey(), entry.getValue());
		}

		MapCodec<S> mapcodec1 = mapcodec;
		Map<Map<Property<?>, Comparable<?>>, S> map = Maps.newLinkedHashMap();
		List<S> list = Lists.newArrayList();
		Stream<List<Pair<Property<?>, Comparable<?>>>> stream = Stream.of(Collections.emptyList());

		for (Property<?> property : this.propertiesByName.values()) {
			stream = stream.flatMap(p_360551_ -> property.getPossibleValues().stream().map(p_155961_ -> {
				List<Pair<Property<?>, Comparable<?>>> list1 = Lists.newArrayList(p_360551_);
				list1.add(Pair.of(property, p_155961_));
				return list1;
			}));
		}

		stream.forEach(p_327405_ -> {
			Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2objectarraymap = new Reference2ObjectArrayMap<>(p_327405_.size());

			for (Pair<Property<?>, Comparable<?>> pair : p_327405_) {
				reference2objectarraymap.put(pair.getFirst(), pair.getSecond());
			}

			S s1 = pValueFunction.create(pOwner, reference2objectarraymap, mapcodec1);
			map.put(reference2objectarraymap, s1);
			list.add(s1);
		});

		for (S s : list) {
			s.populateNeighbours(map);
		}

		this.states = ImmutableList.copyOf(list);
	}

	private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(
			MapCodec<S> pPropertyCodec, Supplier<S> pHolderSupplier, String pValue, Property<T> pProperty
	) {
		return Codec.mapPair(pPropertyCodec, pProperty.valueCodec().fieldOf(pValue).orElseGet(p_187541_ -> {
				}, () -> pProperty.value(pHolderSupplier.get())))
				.xmap(
						p_187536_ -> p_187536_.getFirst().setValue(pProperty, p_187536_.getSecond().value()),
						p_187533_ -> Pair.of((S)p_187533_, pProperty.value(p_187533_))
				);
	}

	public ImmutableList<S> getPossibleStates() {
		return this.states;
	}

	public S any() {
		return (S)this.states.get(0);
	}

	public O getOwner() {
		return this.owner;
	}

	public Collection<Property<?>> getProperties() {
		return this.propertiesByName.values();
	}

	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("block", this.owner)
			.add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList()))
			.toString();
	}

	@Nullable
	public Property<?> getProperty(String string) {
		return this.propertiesByName.get(string);
	}

	public static class Builder<O, S extends StateHolder<O, S>> {
		private final O owner;
		private final Map<String, Property<?>> properties = Maps.<String, Property<?>>newHashMap();

		public Builder(O object) {
			this.owner = object;
		}

		public StateDefinition.Builder<O, S> add(Property<?>... propertys) {
			for (Property<?> property : propertys) {
				this.validateProperty(property);
				this.properties.put(property.getName(), property);
			}

			return this;
		}

		private <T extends Comparable<T>> void validateProperty(Property<T> property) {
			String string = property.getName();
			if (!StateDefinition.NAME_PATTERN.matcher(string).matches()) {
				throw new IllegalArgumentException(this.owner + " has invalidly named property: " + string);
			} else {
				Collection<T> collection = property.getPossibleValues();
				if (collection.size() <= 1) {
					throw new IllegalArgumentException(this.owner + " attempted use property " + string + " with <= 1 possible values");
				} else {
					for (T comparable : collection) {
						String string2 = property.getName(comparable);
						if (!StateDefinition.NAME_PATTERN.matcher(string2).matches()) {
							throw new IllegalArgumentException(this.owner + " has property: " + string + " with invalidly named value: " + string2);
						}
					}

					if (this.properties.containsKey(string)) {
						throw new IllegalArgumentException(this.owner + " has duplicate property: " + string);
					}
				}
			}
		}

		public StateDefinition<O, S> create(Function<O, S> function, StateDefinition.Factory<O, S> factory) {
			return new StateDefinition<>(function, this.owner, factory, this.properties);
		}
	}

	public interface Factory<O, S> {
		S create(O object, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<S> mapCodec);
	}
}
