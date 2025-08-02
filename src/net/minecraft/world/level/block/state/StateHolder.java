package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public abstract class StateHolder<O, S> {
	public static final String NAME_TAG = "Name";
	public static final String PROPERTIES_TAG = "Properties";
	private static final Function<Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Entry<Property<?>, Comparable<?>>, String>() {
		public String apply(@Nullable Entry<Property<?>, Comparable<?>> entry) {
			if (entry == null) {
				return "<NULL>";
			} else {
				Property<?> property = (Property<?>)entry.getKey();
				return property.getName() + "=" + this.getName(property, (Comparable<?>)entry.getValue());
			}
		}

		private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
			return property.getName((T)comparable);
		}
	};
	protected final O owner;
	private final Reference2ObjectArrayMap<Property<?>, Comparable<?>> values;
	private Map<Property<?>, S[]> neighbours;
	protected final MapCodec<S> propertiesCodec;

	protected StateHolder(O object, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<S> mapCodec) {
		this.owner = object;
		this.values = reference2ObjectArrayMap;
		this.propertiesCodec = mapCodec;
	}

	public <T extends Comparable<T>> S cycle(Property<T> property) {
		return this.setValue(property, findNextInCollection(property.getPossibleValues(), this.getValue(property)));
	}

	protected static <T> T findNextInCollection(List<T> list, T object) {
		int i = list.indexOf(object) + 1;
		return (T)(i == list.size() ? list.getFirst() : list.get(i));
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(this.owner);
		if (!this.getValues().isEmpty()) {
			stringBuilder.append('[');
			stringBuilder.append((String)this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
			stringBuilder.append(']');
		}

		return stringBuilder.toString();
	}

	public final boolean equals(Object object) {
		return super.equals(object);
	}

	public int hashCode() {
		return super.hashCode();
	}

	public Collection<Property<?>> getProperties() {
		return Collections.unmodifiableCollection(this.values.keySet());
	}

	public boolean hasProperty(Property<?> property) {
		return this.values.containsKey(property);
	}

	public <T extends Comparable<T>> T getValue(Property<T> property) {
		Comparable<?> comparable = this.values.get(property);
		if (comparable == null) {
			throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this.owner);
		} else {
			return (T)property.getValueClass().cast(comparable);
		}
	}

	public <T extends Comparable<T>> Optional<T> getOptionalValue(Property<T> property) {
		return Optional.ofNullable(this.getNullableValue(property));
	}

	public <T extends Comparable<T>> T getValueOrElse(Property<T> property, T comparable) {
		return (T)Objects.requireNonNullElse(this.getNullableValue(property), comparable);
	}

	@Nullable
	private <T extends Comparable<T>> T getNullableValue(Property<T> property) {
		Comparable<?> comparable = this.values.get(property);
		return (T)(comparable == null ? null : property.getValueClass().cast(comparable));
	}

	public <T extends Comparable<T>, V extends T> S setValue(Property<T> property, V comparable) {
		Comparable<?> comparable2 = this.values.get(property);
		if (comparable2 == null) {
			throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.owner);
		} else {
			return this.setValueInternal(property, comparable, comparable2);
		}
	}

	public <T extends Comparable<T>, V extends T> S trySetValue(Property<T> property, V comparable) {
		Comparable<?> comparable2 = this.values.get(property);
		return (S)(comparable2 == null ? this : this.setValueInternal(property, comparable, comparable2));
	}

	private <T extends Comparable<T>, V extends T> S setValueInternal(Property<T> property, V comparable, Comparable<?> comparable2) {
		if (comparable2.equals(comparable)) {
			return (S)this;
		} else {
			int i = property.getInternalIndex((T)comparable);
			if (i < 0) {
				throw new IllegalArgumentException("Cannot set property " + property + " to " + comparable + " on " + this.owner + ", it is not an allowed value");
			} else {
				return (S)this.neighbours.get(property)[i];
			}
		}
	}

	public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> pPossibleStateMap) {
		if (this.neighbours != null) {
			throw new IllegalStateException();
		} else {
			Map<Property<?>, S[]> map = new Reference2ObjectArrayMap<>(this.values.size());

			for (Entry<Property<?>, Comparable<?>> entry : this.values.entrySet()) {
				Property<?> property = entry.getKey();
				map.put(property, (S[])property.getPossibleValues().stream().map(p_360554_ -> pPossibleStateMap.get(this.makeNeighbourValues(property, p_360554_))).toArray());
			}

			this.neighbours = map;
		}
	}

	private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> property, Comparable<?> comparable) {
		Map<Property<?>, Comparable<?>> map = new Reference2ObjectArrayMap<>(this.values);
		map.put(property, comparable);
		return map;
	}

	public Map<Property<?>, Comparable<?>> getValues() {
		return this.values;
	}

	protected static <O, S extends StateHolder<O, S>> Codec<S> codec(Codec<O> pPropertyMap, Function<O, S> pHolderFunction) {
		return pPropertyMap.dispatch(
				"Name",
				p_61121_ -> p_61121_.owner,
				p_327407_ -> {
					S s = pHolderFunction.apply((O)p_327407_);
					return s.getValues().isEmpty()
							? MapCodec.unit(s)
							: s.propertiesCodec.codec().lenientOptionalFieldOf("Properties").xmap(p_187544_ -> p_187544_.orElse(s), Optional::of);
				}
		);
	}
}
