package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class ChunkProtoTickListFix extends DataFix {
	private static final int SECTION_WIDTH = 16;
	private static final ImmutableSet<String> ALWAYS_WATERLOGGED = ImmutableSet.of(
		"minecraft:bubble_column", "minecraft:kelp", "minecraft:kelp_plant", "minecraft:seagrass", "minecraft:tall_seagrass"
	);

	public ChunkProtoTickListFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.CHUNK);
		OpticFinder<?> opticFinder = type.findField("Level");
		OpticFinder<?> opticFinder2 = opticFinder.type().findField("Sections");
		OpticFinder<?> opticFinder3 = ((ListType)opticFinder2.type()).getElement().finder();
		OpticFinder<?> opticFinder4 = opticFinder3.type().findField("block_states");
		OpticFinder<?> opticFinder5 = opticFinder3.type().findField("biomes");
		OpticFinder<?> opticFinder6 = opticFinder4.type().findField("palette");
		OpticFinder<?> opticFinder7 = opticFinder.type().findField("TileTicks");
		return this.fixTypeEverywhereTyped(
			"ChunkProtoTickListFix",
			type,
			typed -> typed.updateTyped(
				opticFinder,
				typedx -> {
					typedx = typedx.update(
						DSL.remainderFinder(),
						dynamicx -> DataFixUtils.orElse(
							dynamicx.get("LiquidTicks").result().map(dynamic2x -> dynamicx.set("fluid_ticks", dynamic2x).remove("LiquidTicks")), dynamicx
						)
					);
					Dynamic<?> dynamic = typedx.get(DSL.remainderFinder());
					MutableInt mutableInt = new MutableInt();
					Int2ObjectMap<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> int2ObjectMap = new Int2ObjectArrayMap<>();
					typedx.getOptionalTyped(opticFinder2)
						.ifPresent(
							typedxx -> typedxx.getAllTyped(opticFinder3)
								.forEach(
									typedxxx -> {
										Dynamic<?> dynamicx = typedxxx.get(DSL.remainderFinder());
										int ix = dynamicx.get("Y").asInt(Integer.MAX_VALUE);
										if (ix != Integer.MAX_VALUE) {
											if (typedxxx.getOptionalTyped(opticFinder5).isPresent()) {
												mutableInt.setValue(Math.min(ix, mutableInt.getValue()));
											}

											typedxxx.getOptionalTyped(opticFinder4)
												.ifPresent(
													typedxxxx -> int2ObjectMap.put(
														ix,
														Suppliers.memoize(
															() -> {
																List<? extends Dynamic<?>> list = (List<? extends Dynamic<?>>)typedxxxx.getOptionalTyped(opticFinder6)
																	.map(typedxxxxxx -> (List)typedxxxxxx.write().result().map(dynamicxx -> dynamicxx.asList(Function.identity())).orElse(Collections.emptyList()))
																	.orElse(Collections.emptyList());
																long[] ls = typedxxxx.get(DSL.remainderFinder()).get("data").asLongStream().toArray();
																return new ChunkProtoTickListFix.PoorMansPalettedContainer(list, ls);
															}
														)
													)
												);
										}
									}
								)
						);
					byte b = mutableInt.getValue().byteValue();
					typedx = typedx.update(DSL.remainderFinder(), dynamicx -> dynamicx.update("yPos", dynamicxx -> dynamicxx.createByte(b)));
					if (!typedx.getOptionalTyped(opticFinder7).isPresent() && !dynamic.get("fluid_ticks").result().isPresent()) {
						int i = dynamic.get("xPos").asInt(0);
						int j = dynamic.get("zPos").asInt(0);
						Dynamic<?> dynamic2 = this.makeTickList(dynamic, int2ObjectMap, b, i, j, "LiquidsToBeTicked", ChunkProtoTickListFix::getLiquid);
						Dynamic<?> dynamic3 = this.makeTickList(dynamic, int2ObjectMap, b, i, j, "ToBeTicked", ChunkProtoTickListFix::getBlock);
						Optional<? extends Pair<? extends Typed<?>, ?>> optional = opticFinder7.type().readTyped(dynamic3).result();
						if (optional.isPresent()) {
							typedx = typedx.set(opticFinder7, (Typed)((Pair)optional.get()).getFirst());
						}

						return typedx.update(DSL.remainderFinder(), dynamic2x -> dynamic2x.remove("ToBeTicked").remove("LiquidsToBeTicked").set("fluid_ticks", dynamic2));
					} else {
						return typedx;
					}
				}
			)
		);
	}

	private Dynamic<?> makeTickList(
			Dynamic<?> pData,
			Int2ObjectMap<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> pPalette,
			byte pY,
			int pX,
			int pZ,
			String pName,
			Function<Dynamic<?>, String> pIdGetter
	) {
		Stream<Dynamic<?>> stream = Stream.empty();
		List<? extends Dynamic<?>> list = pData.get(pName).asList(Function.identity());

		for (int i = 0; i < list.size(); i++) {
			int j = i + pY;
			Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> supplier = pPalette.get(j);
			Stream<? extends Dynamic<?>> stream1 = list.get(i)
					.asStream()
					.mapToInt(p_185074_ -> p_185074_.asShort((short)-1))
					.filter(p_184993_ -> p_184993_ > 0)
					.mapToObj(p_185059_ -> this.createTick(pData, supplier, pX, j, pZ, p_185059_, pIdGetter));
			stream = Stream.concat(stream, stream1);
		}

		return pData.createList(stream);
	}

	private static String getBlock(@Nullable Dynamic<?> dynamic) {
		return dynamic != null ? dynamic.get("Name").asString("minecraft:air") : "minecraft:air";
	}

	private static String getLiquid(@Nullable Dynamic<?> dynamic) {
		if (dynamic == null) {
			return "minecraft:empty";
		} else {
			String string = dynamic.get("Name").asString("");
			if ("minecraft:water".equals(string)) {
				return dynamic.get("Properties").get("level").asInt(0) == 0 ? "minecraft:water" : "minecraft:flowing_water";
			} else if ("minecraft:lava".equals(string)) {
				return dynamic.get("Properties").get("level").asInt(0) == 0 ? "minecraft:lava" : "minecraft:flowing_lava";
			} else {
				return !ALWAYS_WATERLOGGED.contains(string) && !dynamic.get("Properties").get("waterlogged").asBoolean(false) ? "minecraft:empty" : "minecraft:water";
			}
		}
	}

	private Dynamic<?> createTick(
			Dynamic<?> pData,
			@javax.annotation.Nullable Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> pPalette,
			int pX,
			int pY,
			int pZ,
			int pIndex,
			Function<Dynamic<?>, String> pIdGetter
	) {
		int i = pIndex & 15;
		int j = pIndex >>> 4 & 15;
		int k = pIndex >>> 8 & 15;
		String s = pIdGetter.apply(pPalette != null ? pPalette.get().get(i, j, k) : null);
		return pData.createMap(
				ImmutableMap.<Dynamic<?>, Dynamic<?>>builder()
						.put(pData.createString("i"), pData.createString(s))
						.put(pData.createString("x"), pData.createInt(pX * 16 + i))
						.put(pData.createString("y"), pData.createInt(pY * 16 + j))
						.put(pData.createString("z"), pData.createInt(pZ * 16 + k))
						.put(pData.createString("t"), pData.createInt(0))
						.put(pData.createString("p"), pData.createInt(0))
						.build()
		);
	}

	public static final class PoorMansPalettedContainer {
		private static final long SIZE_BITS = 4L;
		private final List<? extends Dynamic<?>> palette;
		private final long[] data;
		private final int bits;
		private final long mask;
		private final int valuesPerLong;

		public PoorMansPalettedContainer(List<? extends Dynamic<?>> list, long[] ls) {
			this.palette = list;
			this.data = ls;
			this.bits = Math.max(4, ChunkHeightAndBiomeFix.ceillog2(list.size()));
			this.mask = (1L << this.bits) - 1L;
			this.valuesPerLong = (char)(64 / this.bits);
		}

		@Nullable
		public Dynamic<?> get(int i, int j, int k) {
			int l = this.palette.size();
			if (l < 1) {
				return null;
			} else if (l == 1) {
				return (Dynamic<?>)this.palette.get(0);
			} else {
				int m = this.getIndex(i, j, k);
				int n = m / this.valuesPerLong;
				if (n >= 0 && n < this.data.length) {
					long o = this.data[n];
					int p = (m - n * this.valuesPerLong) * this.bits;
					int q = (int)(o >> p & this.mask);
					return q >= 0 && q < l ? (Dynamic)this.palette.get(q) : null;
				} else {
					return null;
				}
			}
		}

		private int getIndex(int i, int j, int k) {
			return (j << 4 | k) << 4 | i;
		}

		public List<? extends Dynamic<?>> palette() {
			return this.palette;
		}

		public long[] data() {
			return this.data;
		}
	}
}
