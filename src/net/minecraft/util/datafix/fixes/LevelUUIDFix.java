package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import org.slf4j.Logger;

public class LevelUUIDFix extends AbstractUUIDFix {
	private static final Logger LOGGER = LogUtils.getLogger();

	public LevelUUIDFix(Schema schema) {
		super(schema, References.LEVEL);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(this.typeReference);
		OpticFinder<?> opticFinder = type.findField("CustomBossEvents");
		OpticFinder<?> opticFinder2 = DSL.typeFinder(
			DSL.and(DSL.optional(DSL.field("Name", this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT))), DSL.remainderType())
		);
		return this.fixTypeEverywhereTyped("LevelUUIDFix", type, typed -> typed.update(DSL.remainderFinder(), dynamic -> {
			dynamic = this.updateDragonFight(dynamic);
			return this.updateWanderingTrader(dynamic);
		}).updateTyped(opticFinder, typedx -> typedx.updateTyped(opticFinder2, typedxx -> typedxx.update(DSL.remainderFinder(), this::updateCustomBossEvent))));
	}

	private Dynamic<?> updateWanderingTrader(Dynamic<?> dynamic) {
		return (Dynamic<?>)replaceUUIDString(dynamic, "WanderingTraderId", "WanderingTraderId").orElse(dynamic);
	}

	private Dynamic<?> updateDragonFight(Dynamic<?> dynamic) {
		return dynamic.update(
			"DimensionData",
			dynamicx -> dynamicx.updateMapValues(
				pair -> pair.mapSecond(
					dynamicxx -> dynamicxx.update("DragonFight", dynamicxxx -> (Dynamic)replaceUUIDLeastMost(dynamicxxx, "DragonUUID", "Dragon").orElse(dynamicxxx))
				)
			)
		);
	}

	private Dynamic<?> updateCustomBossEvent(Dynamic<?> pDynamic) {
		return pDynamic.update(
				"CustomBossEvents",
				p_16379_ -> p_16379_.updateMapValues(
						p_145491_ -> p_145491_.mapSecond(
								p_145500_ -> p_145500_.update(
										"Players",
										p_145494_ -> p_145500_.createList(p_145494_.asStream().map(p_145502_ -> createUUIDFromML((Dynamic<?>)p_145502_).orElseGet(() -> {
											LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
											return p_145502_;
										})))
								)
						)
				)
		);
	}
}
