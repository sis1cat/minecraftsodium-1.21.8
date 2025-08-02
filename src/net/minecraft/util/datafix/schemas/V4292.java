package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V4292 extends NamespacedSchema {
	public V4292(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		super.registerTypes(schema, map, map2);
		schema.registerType(
			true,
			References.TEXT_COMPONENT,
			() -> DSL.or(
				DSL.or(DSL.constType(DSL.string()), DSL.list(References.TEXT_COMPONENT.in(schema))),
				DSL.optionalFields(
					"extra",
					DSL.list(References.TEXT_COMPONENT.in(schema)),
					"separator",
					References.TEXT_COMPONENT.in(schema),
					"hover_event",
					DSL.taggedChoice(
						"action",
						DSL.string(),
						Map.of(
							"show_text",
							DSL.optionalFields("value", References.TEXT_COMPONENT.in(schema)),
							"show_item",
							References.ITEM_STACK.in(schema),
							"show_entity",
							DSL.optionalFields("id", References.ENTITY_NAME.in(schema), "name", References.TEXT_COMPONENT.in(schema))
						)
					)
				)
			)
		);
	}
}
