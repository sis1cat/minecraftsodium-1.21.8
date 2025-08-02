package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackUUIDFix extends AbstractUUIDFix {
	public ItemStackUUIDFix(Schema schema) {
		super(schema, References.ITEM_STACK);
	}

	@Override
	public TypeRewriteRule makeRule() {
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		return this.fixTypeEverywhereTyped("ItemStackUUIDFix", this.getInputSchema().getType(this.typeReference), typed -> {
			OpticFinder<?> opticFinder2 = typed.getType().findField("tag");
			return typed.updateTyped(opticFinder2, typed2 -> typed2.update(DSL.remainderFinder(), dynamic -> {
				dynamic = this.updateAttributeModifiers(dynamic);
				if ((Boolean)typed.getOptional(opticFinder).map(pair -> "minecraft:player_head".equals(pair.getSecond())).orElse(false)) {
					dynamic = this.updateSkullOwner(dynamic);
				}

				return dynamic;
			}));
		});
	}

	private Dynamic<?> updateAttributeModifiers(Dynamic<?> pDynamic) {
		return pDynamic.update(
				"AttributeModifiers",
				p_16145_ -> pDynamic.createList(p_16145_.asStream().map(p_145437_ -> replaceUUIDLeastMost((Dynamic<?>)p_145437_, "UUID", "UUID").orElse((Dynamic<?>)p_145437_)))
		);
	}

	private Dynamic<?> updateSkullOwner(Dynamic<?> dynamic) {
		return dynamic.update("SkullOwner", dynamicx -> (Dynamic)replaceUUIDString(dynamicx, "Id", "Id").orElse(dynamicx));
	}
}
