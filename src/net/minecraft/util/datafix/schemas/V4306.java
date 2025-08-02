package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V4306 extends NamespacedSchema {
	public V4306(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
		map.remove("minecraft:potion");
		schema.register(map, "minecraft:splash_potion", (Supplier<TypeTemplate>)(() -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema))));
		schema.register(map, "minecraft:lingering_potion", (Supplier<TypeTemplate>)(() -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema))));
		return map;
	}
}
