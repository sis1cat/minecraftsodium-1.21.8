package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class EquipmentFormatFix extends DataFix {
	public EquipmentFormatFix(Schema schema) {
		super(schema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getTypeRaw(References.ITEM_STACK);
		Type<?> type2 = this.getOutputSchema().getTypeRaw(References.ITEM_STACK);
		OpticFinder<?> opticFinder = type.findField("id");
		return this.fix(type, type2, opticFinder);
	}

	private <ItemStackOld, ItemStackNew> TypeRewriteRule fix(Type<ItemStackOld> p_391706_, Type<ItemStackNew> p_395571_, OpticFinder<?> p_397910_) {
		Type<Pair<String, Pair<Either<List<ItemStackOld>, Unit>, Pair<Either<List<ItemStackOld>, Unit>, Pair<Either<ItemStackOld, Unit>, Either<ItemStackOld, Unit>>>>>> type = DSL.named(
				References.ENTITY_EQUIPMENT.typeName(),
				DSL.and(
						DSL.optional(DSL.field("ArmorItems", DSL.list(p_391706_))),
						DSL.optional(DSL.field("HandItems", DSL.list(p_391706_))),
						DSL.optional(DSL.field("body_armor_item", p_391706_)),
						DSL.optional(DSL.field("saddle", p_391706_))
				)
		);
		Type<Pair<String, Either<Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Pair<Either<ItemStackNew, Unit>, Dynamic<?>>>>>>>>>, Unit>>> type1 = DSL.named(
				References.ENTITY_EQUIPMENT.typeName(),
				DSL.optional(
						DSL.field(
								"equipment",
								DSL.and(
										DSL.optional(DSL.field("mainhand", p_395571_)),
										DSL.optional(DSL.field("offhand", p_395571_)),
										DSL.optional(DSL.field("feet", p_395571_)),
										DSL.and(
												DSL.optional(DSL.field("legs", p_395571_)),
												DSL.optional(DSL.field("chest", p_395571_)),
												DSL.optional(DSL.field("head", p_395571_)),
												DSL.and(DSL.optional(DSL.field("body", p_395571_)), DSL.optional(DSL.field("saddle", p_395571_)), DSL.remainderType())
										)
								)
						)
				)
		);
		if (!type.equals(this.getInputSchema().getType(References.ENTITY_EQUIPMENT))) {
			throw new IllegalStateException("Input entity_equipment type does not match expected");
		} else if (!type1.equals(this.getOutputSchema().getType(References.ENTITY_EQUIPMENT))) {
			throw new IllegalStateException("Output entity_equipment type does not match expected");
		} else {
			return this.fixTypeEverywhere(
					"EquipmentFormatFix",
					type,
					type1,
					p_395730_ -> {
						Predicate<ItemStackOld> predicate = p_397158_ -> {
							Typed<ItemStackOld> typed = new Typed<>(p_391706_, p_395730_, p_397158_);
							return typed.getOptional(p_397910_).isEmpty();
						};
						return p_396735_ -> {
							String s = p_396735_.getFirst();
							Pair<Either<List<ItemStackOld>, Unit>, Pair<Either<List<ItemStackOld>, Unit>, Pair<Either<ItemStackOld, Unit>, Either<ItemStackOld, Unit>>>> pair = p_396735_.getSecond();
							List<ItemStackOld> list = pair.getFirst().map(Function.identity(), p_397016_ -> List.of());
							List<ItemStackOld> list1 = pair.getSecond().getFirst().map(Function.identity(), p_397095_ -> List.of());
							Either<ItemStackOld, Unit> either = pair.getSecond().getSecond().getFirst();
							Either<ItemStackOld, Unit> either1 = pair.getSecond().getSecond().getSecond();
							Either<ItemStackOld, Unit> either2 = getItemFromList(0, list, predicate);
							Either<ItemStackOld, Unit> either3 = getItemFromList(1, list, predicate);
							Either<ItemStackOld, Unit> either4 = getItemFromList(2, list, predicate);
							Either<ItemStackOld, Unit> either5 = getItemFromList(3, list, predicate);
							Either<ItemStackOld, Unit> either6 = getItemFromList(0, list1, predicate);
							Either<ItemStackOld, Unit> either7 = getItemFromList(1, list1, predicate);
							return areAllEmpty(either, either1, either2, either3, either4, either5, either6, either7)
									? Pair.of(s, Either.right(Unit.INSTANCE))
									: Pair.of(
									s,
									Either.left(
											Pair.of(
													(Either<ItemStackNew, Unit>)either6,
													Pair.of(
															(Either<ItemStackNew, Unit>)either7,
															Pair.of(
																	(Either<ItemStackNew, Unit>)either2,
																	Pair.of(
																			(Either<ItemStackNew, Unit>)either3,
																			Pair.of(
																					(Either<ItemStackNew, Unit>)either4,
																					Pair.of(
																							(Either<ItemStackNew, Unit>)either5,
																							Pair.of(
																									(Either<ItemStackNew, Unit>)either,
																									Pair.of((Either<ItemStackNew, Unit>)either1, new Dynamic(p_395730_))
																							)
																					)
																			)
																	)
															)
													)
											)
									)
							);
						};
					}
			);
		}
	}

	@SafeVarargs
	private static boolean areAllEmpty(Either<?, Unit>... eithers) {
		for (Either<?, Unit> either : eithers) {
			if (either.right().isEmpty()) {
				return false;
			}
		}

		return true;
	}

	private static <ItemStack> Either<ItemStack, Unit> getItemFromList(int i, List<ItemStack> list, Predicate<ItemStack> predicate) {
		if (i >= list.size()) {
			return Either.right(Unit.INSTANCE);
		} else {
			ItemStack object = (ItemStack)list.get(i);
			return predicate.test(object) ? Either.right(Unit.INSTANCE) : Either.left(object);
		}
	}
}
