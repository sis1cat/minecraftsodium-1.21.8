package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityUUIDFix extends AbstractUUIDFix {
	public BlockEntityUUIDFix(Schema schema) {
		super(schema, References.BLOCK_ENTITY);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped("BlockEntityUUIDFix", this.getInputSchema().getType(this.typeReference), typed -> {
			typed = this.updateNamedChoice(typed, "minecraft:conduit", this::updateConduit);
			return this.updateNamedChoice(typed, "minecraft:skull", this::updateSkull);
		});
	}

	private Dynamic<?> updateSkull(Dynamic<?> pSkullTag) {
		return pSkullTag.get("Owner")
				.get()
				.map(p_14894_ -> replaceUUIDString((Dynamic<?>)p_14894_, "Id", "Id").orElse((Dynamic<?>)p_14894_))
				.<Dynamic<?>>map(p_14888_ -> pSkullTag.remove("Owner").set("SkullOwner", (Dynamic<?>)p_14888_))
				.result()
				.orElse(pSkullTag);
	}

	private Dynamic<?> updateConduit(Dynamic<?> dynamic) {
		return (Dynamic<?>)replaceUUIDMLTag(dynamic, "target_uuid", "Target").orElse(dynamic);
	}
}
