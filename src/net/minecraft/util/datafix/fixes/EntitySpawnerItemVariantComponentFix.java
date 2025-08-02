package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntitySpawnerItemVariantComponentFix extends DataFix {
	public EntitySpawnerItemVariantComponentFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public final TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticfinder1 = type.findField("components");
		return this.fixTypeEverywhereTyped(
				"ItemStack bucket_entity_data variants to separate components",
				type,
				p_392674_ -> {
					String s = p_392674_.getOptional(opticfinder).map(Pair::getSecond).orElse("");

					return switch (s) {
						case "minecraft:salmon_bucket" -> p_392674_.updateTyped(opticfinder1, (Fixer)EntitySpawnerItemVariantComponentFix::fixSalmonBucket);
						case "minecraft:axolotl_bucket" -> p_392674_.updateTyped(opticfinder1, (Fixer)EntitySpawnerItemVariantComponentFix::fixAxolotlBucket);
						case "minecraft:tropical_fish_bucket" -> p_392674_.updateTyped(opticfinder1, (Fixer)EntitySpawnerItemVariantComponentFix::fixTropicalFishBucket);
						case "minecraft:painting" -> p_392674_.updateTyped(
								opticfinder1, p_395765_ -> Util.writeAndReadTypedOrThrow(p_395765_, p_395765_.getType(), EntitySpawnerItemVariantComponentFix::fixPainting)
						);
						default -> p_392674_;
					};
				}
		);
	}


	private static String getBaseColor(int i) {
		return ExtraDataFixUtils.dyeColorIdToName(i >> 16 & 0xFF);
	}

	private static String getPatternColor(int i) {
		return ExtraDataFixUtils.dyeColorIdToName(i >> 24 & 0xFF);
	}

	private static String getPattern(int i) {
		return switch (i & 65535) {
			case 1 -> "flopper";
			case 256 -> "sunstreak";
			case 257 -> "stripey";
			case 512 -> "snooper";
			case 513 -> "glitter";
			case 768 -> "dasher";
			case 769 -> "blockfish";
			case 1024 -> "brinely";
			case 1025 -> "betty";
			case 1280 -> "spotty";
			case 1281 -> "clayfish";
			default -> "kob";
		};
	}

	private static <T> Dynamic<T> fixTropicalFishBucket(Dynamic<T> dynamic, Dynamic<T> dynamic2) {
		Optional<Number> optional = dynamic2.get("BucketVariantTag").asNumber().result();
		if (optional.isEmpty()) {
			return dynamic;
		} else {
			int i = ((Number)optional.get()).intValue();
			String string = getPattern(i);
			String string2 = getBaseColor(i);
			String string3 = getPatternColor(i);
			return dynamic.update("minecraft:bucket_entity_data", dynamicx -> dynamicx.remove("BucketVariantTag"))
				.set("minecraft:tropical_fish/pattern", dynamic.createString(string))
				.set("minecraft:tropical_fish/base_color", dynamic.createString(string2))
				.set("minecraft:tropical_fish/pattern_color", dynamic.createString(string3));
		}
	}

	private static <T> Dynamic<T> fixAxolotlBucket(Dynamic<T> dynamic, Dynamic<T> dynamic2) {
		Optional<Number> optional = dynamic2.get("Variant").asNumber().result();
		if (optional.isEmpty()) {
			return dynamic;
		} else {
			String string = switch (((Number)optional.get()).intValue()) {
				case 1 -> "wild";
				case 2 -> "gold";
				case 3 -> "cyan";
				case 4 -> "blue";
				default -> "lucy";
			};
			return dynamic.update("minecraft:bucket_entity_data", dynamicx -> dynamicx.remove("Variant")).set("minecraft:axolotl/variant", dynamic.createString(string));
		}
	}

	private static <T> Dynamic<T> fixSalmonBucket(Dynamic<T> dynamic, Dynamic<T> dynamic2) {
		Optional<Dynamic<T>> optional = dynamic2.get("type").result();
		return optional.isEmpty()
			? dynamic
			: dynamic.update("minecraft:bucket_entity_data", dynamicx -> dynamicx.remove("type")).set("minecraft:salmon/size", (Dynamic<?>)optional.get());
	}

	private static <T> Dynamic<T> fixPainting(Dynamic<T> dynamic) {
		Optional<Dynamic<T>> optional = dynamic.get("minecraft:entity_data").result();
		if (optional.isEmpty()) {
			return dynamic;
		} else if (((Dynamic)optional.get()).get("id").asString().result().filter(string -> string.equals("minecraft:painting")).isEmpty()) {
			return dynamic;
		} else {
			Optional<Dynamic<T>> optional2 = ((Dynamic)optional.get()).get("variant").result();
			Dynamic<T> dynamic2 = ((Dynamic)optional.get()).remove("variant");
			if (dynamic2.remove("id").equals(dynamic2.emptyMap())) {
				dynamic = dynamic.remove("minecraft:entity_data");
			} else {
				dynamic = dynamic.set("minecraft:entity_data", dynamic2);
			}

			if (optional2.isPresent()) {
				dynamic = dynamic.set("minecraft:painting/variant", (Dynamic<?>)optional2.get());
			}

			return dynamic;
		}
	}

	@FunctionalInterface
	interface Fixer extends Function<Typed<?>, Typed<?>> {
		default Typed<?> apply(Typed<?> typed) {
			return typed.update(DSL.remainderFinder(), this::fixRemainder);
		}

		default <T> Dynamic<T> fixRemainder(Dynamic<T> dynamic) {
			return (Dynamic<T>)dynamic.get("minecraft:bucket_entity_data").result().map(dynamic2 -> this.fixRemainder(dynamic, dynamic2)).orElse(dynamic);
		}

		<T> Dynamic<T> fixRemainder(Dynamic<T> dynamic, Dynamic<T> dynamic2);
	}
}
