package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1460 extends NamespacedSchema {
	public V1460(int i, Schema schema) {
		super(i, schema);
	}

	protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
		schema.registerSimple(map, string);
	}

	protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
		schema.register(map, string, (Supplier<TypeTemplate>)(() -> V1458.nameableInventory(schema)));
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = Maps.<String, Supplier<TypeTemplate>>newHashMap();
		schema.register(
			map, "minecraft:area_effect_cloud", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("Particle", References.PARTICLE.in(schema)))
		);
		registerMob(schema, map, "minecraft:armor_stand");
		schema.register(map, "minecraft:arrow", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(schema))));
		registerMob(schema, map, "minecraft:bat");
		registerMob(schema, map, "minecraft:blaze");
		schema.registerSimple(map, "minecraft:boat");
		registerMob(schema, map, "minecraft:cave_spider");
		schema.register(
			map,
			"minecraft:chest_minecart",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
				"DisplayState", References.BLOCK_STATE.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))
			))
		);
		registerMob(schema, map, "minecraft:chicken");
		schema.register(
			map,
			"minecraft:commandblock_minecart",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
				"DisplayState", References.BLOCK_STATE.in(schema), "LastOutput", References.TEXT_COMPONENT.in(schema)
			))
		);
		registerMob(schema, map, "minecraft:cow");
		registerMob(schema, map, "minecraft:creeper");
		schema.register(
			map,
			"minecraft:donkey",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
				"Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema)
			))
		);
		schema.registerSimple(map, "minecraft:dragon_fireball");
		schema.registerSimple(map, "minecraft:egg");
		registerMob(schema, map, "minecraft:elder_guardian");
		schema.registerSimple(map, "minecraft:ender_crystal");
		registerMob(schema, map, "minecraft:ender_dragon");
		schema.register(
			map, "minecraft:enderman", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("carriedBlockState", References.BLOCK_STATE.in(schema)))
		);
		registerMob(schema, map, "minecraft:endermite");
		schema.registerSimple(map, "minecraft:ender_pearl");
		schema.registerSimple(map, "minecraft:evocation_fangs");
		registerMob(schema, map, "minecraft:evocation_illager");
		schema.registerSimple(map, "minecraft:eye_of_ender_signal");
		schema.register(
			map,
			"minecraft:falling_block",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
				"BlockState", References.BLOCK_STATE.in(schema), "TileEntityData", References.BLOCK_ENTITY.in(schema)
			))
		);
		schema.registerSimple(map, "minecraft:fireball");
		schema.register(
			map, "minecraft:fireworks_rocket", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(schema)))
		);
		schema.register(
			map, "minecraft:furnace_minecart", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema)))
		);
		registerMob(schema, map, "minecraft:ghast");
		registerMob(schema, map, "minecraft:giant");
		registerMob(schema, map, "minecraft:guardian");
		schema.register(
			map,
			"minecraft:hopper_minecart",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
				"DisplayState", References.BLOCK_STATE.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))
			))
		);
		schema.register(
			map,
			"minecraft:horse",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema)))
		);
		registerMob(schema, map, "minecraft:husk");
		registerMob(schema, map, "minecraft:illusion_illager");
		schema.register(map, "minecraft:item", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema))));
		schema.register(map, "minecraft:item_frame", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema))));
		schema.registerSimple(map, "minecraft:leash_knot");
		schema.register(
			map,
			"minecraft:llama",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
				"Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), "DecorItem", References.ITEM_STACK.in(schema)
			))
		);
		schema.registerSimple(map, "minecraft:llama_spit");
		registerMob(schema, map, "minecraft:magma_cube");
		schema.register(map, "minecraft:minecart", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema))));
		registerMob(schema, map, "minecraft:mooshroom");
		schema.register(
			map,
			"minecraft:mule",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
				"Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema)
			))
		);
		registerMob(schema, map, "minecraft:ocelot");
		schema.registerSimple(map, "minecraft:painting");
		registerMob(schema, map, "minecraft:parrot");
		registerMob(schema, map, "minecraft:pig");
		registerMob(schema, map, "minecraft:polar_bear");
		schema.register(map, "minecraft:potion", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("Potion", References.ITEM_STACK.in(schema))));
		registerMob(schema, map, "minecraft:rabbit");
		registerMob(schema, map, "minecraft:sheep");
		registerMob(schema, map, "minecraft:shulker");
		schema.registerSimple(map, "minecraft:shulker_bullet");
		registerMob(schema, map, "minecraft:silverfish");
		registerMob(schema, map, "minecraft:skeleton");
		schema.register(
			map, "minecraft:skeleton_horse", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema)))
		);
		registerMob(schema, map, "minecraft:slime");
		schema.registerSimple(map, "minecraft:small_fireball");
		schema.registerSimple(map, "minecraft:snowball");
		registerMob(schema, map, "minecraft:snowman");
		schema.register(
			map,
			"minecraft:spawner_minecart",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema), References.UNTAGGED_SPAWNER.in(schema)))
		);
		schema.register(
			map, "minecraft:spectral_arrow", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(schema)))
		);
		registerMob(schema, map, "minecraft:spider");
		registerMob(schema, map, "minecraft:squid");
		registerMob(schema, map, "minecraft:stray");
		schema.registerSimple(map, "minecraft:tnt");
		schema.register(
			map, "minecraft:tnt_minecart", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema)))
		);
		registerMob(schema, map, "minecraft:vex");
		schema.register(
			map,
			"minecraft:villager",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
				"Inventory", DSL.list(References.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in(schema)))
			))
		);
		registerMob(schema, map, "minecraft:villager_golem");
		registerMob(schema, map, "minecraft:vindication_illager");
		registerMob(schema, map, "minecraft:witch");
		registerMob(schema, map, "minecraft:wither");
		registerMob(schema, map, "minecraft:wither_skeleton");
		schema.registerSimple(map, "minecraft:wither_skull");
		registerMob(schema, map, "minecraft:wolf");
		schema.registerSimple(map, "minecraft:xp_bottle");
		schema.registerSimple(map, "minecraft:xp_orb");
		registerMob(schema, map, "minecraft:zombie");
		schema.register(map, "minecraft:zombie_horse", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema))));
		registerMob(schema, map, "minecraft:zombie_pigman");
		schema.register(
			map,
			"minecraft:zombie_villager",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields("Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in(schema)))))
		);
		return map;
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = Maps.<String, Supplier<TypeTemplate>>newHashMap();
		registerInventory(schema, map, "minecraft:furnace");
		registerInventory(schema, map, "minecraft:chest");
		registerInventory(schema, map, "minecraft:trapped_chest");
		schema.registerSimple(map, "minecraft:ender_chest");
		schema.register(map, "minecraft:jukebox", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(schema))));
		registerInventory(schema, map, "minecraft:dispenser");
		registerInventory(schema, map, "minecraft:dropper");
		schema.register(map, "minecraft:sign", (Supplier<TypeTemplate>)(() -> V99.sign(schema)));
		schema.register(map, "minecraft:mob_spawner", (Function<String, TypeTemplate>)(string -> References.UNTAGGED_SPAWNER.in(schema)));
		schema.register(map, "minecraft:piston", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("blockState", References.BLOCK_STATE.in(schema))));
		registerInventory(schema, map, "minecraft:brewing_stand");
		schema.register(map, "minecraft:enchanting_table", (Supplier<TypeTemplate>)(() -> V1458.nameable(schema)));
		schema.registerSimple(map, "minecraft:end_portal");
		schema.register(map, "minecraft:beacon", (Supplier<TypeTemplate>)(() -> V1458.nameable(schema)));
		schema.register(map, "minecraft:skull", (Supplier<TypeTemplate>)(() -> DSL.optionalFields("custom_name", References.TEXT_COMPONENT.in(schema))));
		schema.registerSimple(map, "minecraft:daylight_detector");
		registerInventory(schema, map, "minecraft:hopper");
		schema.registerSimple(map, "minecraft:comparator");
		schema.register(map, "minecraft:banner", (Supplier<TypeTemplate>)(() -> V1458.nameable(schema)));
		schema.registerSimple(map, "minecraft:structure_block");
		schema.registerSimple(map, "minecraft:end_gateway");
		schema.register(map, "minecraft:command_block", (Supplier<TypeTemplate>)(() -> DSL.optionalFields("LastOutput", References.TEXT_COMPONENT.in(schema))));
		registerInventory(schema, map, "minecraft:shulker_box");
		schema.registerSimple(map, "minecraft:bed");
		return map;
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		schema.registerType(
			false,
			References.LEVEL,
			() -> DSL.optionalFields(
				"CustomBossEvents", DSL.compoundList(DSL.optionalFields("Name", References.TEXT_COMPONENT.in(schema))), References.LIGHTWEIGHT_LEVEL.in(schema)
			)
		);
		schema.registerType(false, References.LIGHTWEIGHT_LEVEL, DSL::remainder);
		schema.registerType(false, References.RECIPE, () -> DSL.constType(namespacedString()));
		schema.registerType(
			false,
			References.PLAYER,
			() -> DSL.optionalFields(
				Pair.of("RootVehicle", DSL.optionalFields("Entity", References.ENTITY_TREE.in(schema))),
				Pair.of("ender_pearls", DSL.list(References.ENTITY_TREE.in(schema))),
				Pair.of("Inventory", DSL.list(References.ITEM_STACK.in(schema))),
				Pair.of("EnderItems", DSL.list(References.ITEM_STACK.in(schema))),
				Pair.of("ShoulderEntityLeft", References.ENTITY_TREE.in(schema)),
				Pair.of("ShoulderEntityRight", References.ENTITY_TREE.in(schema)),
				Pair.of("recipeBook", DSL.optionalFields("recipes", DSL.list(References.RECIPE.in(schema)), "toBeDisplayed", DSL.list(References.RECIPE.in(schema))))
			)
		);
		schema.registerType(
			false,
			References.CHUNK,
			() -> DSL.fields(
				"Level",
				DSL.optionalFields(
					"Entities",
					DSL.list(References.ENTITY_TREE.in(schema)),
					"TileEntities",
					DSL.list(DSL.or(References.BLOCK_ENTITY.in(schema), DSL.remainder())),
					"TileTicks",
					DSL.list(DSL.fields("i", References.BLOCK_NAME.in(schema))),
					"Sections",
					DSL.list(DSL.optionalFields("Palette", DSL.list(References.BLOCK_STATE.in(schema))))
				)
			)
		);
		schema.registerType(
			true,
			References.BLOCK_ENTITY,
			() -> DSL.optionalFields("components", References.DATA_COMPONENTS.in(schema), DSL.taggedChoiceLazy("id", namespacedString(), map2))
		);
		schema.registerType(
			true, References.ENTITY_TREE, () -> DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in(schema)), References.ENTITY.in(schema))
		);
		schema.registerType(
			true,
			References.ENTITY,
			() -> DSL.and(
				References.ENTITY_EQUIPMENT.in(schema),
				DSL.optionalFields("CustomName", References.TEXT_COMPONENT.in(schema), DSL.taggedChoiceLazy("id", namespacedString(), map))
			)
		);
		schema.registerType(
			true,
			References.ITEM_STACK,
			() -> DSL.hook(DSL.optionalFields("id", References.ITEM_NAME.in(schema), "tag", V99.itemStackTag(schema)), V705.ADD_NAMES, HookFunction.IDENTITY)
		);
		schema.registerType(false, References.HOTBAR, () -> DSL.compoundList(DSL.list(References.ITEM_STACK.in(schema))));
		schema.registerType(false, References.OPTIONS, DSL::remainder);
		schema.registerType(
			false,
			References.STRUCTURE,
			() -> DSL.optionalFields(
				"entities",
				DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in(schema))),
				"blocks",
				DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in(schema))),
				"palette",
				DSL.list(References.BLOCK_STATE.in(schema))
			)
		);
		schema.registerType(false, References.BLOCK_NAME, () -> DSL.constType(namespacedString()));
		schema.registerType(false, References.ITEM_NAME, () -> DSL.constType(namespacedString()));
		schema.registerType(false, References.BLOCK_STATE, DSL::remainder);
		schema.registerType(false, References.FLAT_BLOCK_STATE, DSL::remainder);
		Supplier<TypeTemplate> supplier = () -> DSL.compoundList(References.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
		schema.registerType(
			false,
			References.STATS,
			() -> DSL.optionalFields(
				"stats",
				DSL.optionalFields(
					Pair.of("minecraft:mined", DSL.compoundList(References.BLOCK_NAME.in(schema), DSL.constType(DSL.intType()))),
					Pair.of("minecraft:crafted", (TypeTemplate)supplier.get()),
					Pair.of("minecraft:used", (TypeTemplate)supplier.get()),
					Pair.of("minecraft:broken", (TypeTemplate)supplier.get()),
					Pair.of("minecraft:picked_up", (TypeTemplate)supplier.get()),
					Pair.of("minecraft:dropped", (TypeTemplate)supplier.get()),
					Pair.of("minecraft:killed", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))),
					Pair.of("minecraft:killed_by", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))),
					Pair.of("minecraft:custom", DSL.compoundList(DSL.constType(namespacedString()), DSL.constType(DSL.intType())))
				)
			)
		);
		schema.registerType(false, References.SAVED_DATA_COMMAND_STORAGE, DSL::remainder);
		schema.registerType(false, References.SAVED_DATA_TICKETS, DSL::remainder);
		schema.registerType(
			false,
			References.SAVED_DATA_MAP_DATA,
			() -> DSL.optionalFields("data", DSL.optionalFields("banners", DSL.list(DSL.optionalFields("Name", References.TEXT_COMPONENT.in(schema)))))
		);
		schema.registerType(false, References.SAVED_DATA_MAP_INDEX, DSL::remainder);
		schema.registerType(false, References.SAVED_DATA_RAIDS, DSL::remainder);
		schema.registerType(false, References.SAVED_DATA_RANDOM_SEQUENCES, DSL::remainder);
		schema.registerType(
			false,
			References.SAVED_DATA_SCOREBOARD,
			() -> DSL.optionalFields(
				"data",
				DSL.optionalFields(
					"Objectives",
					DSL.list(References.OBJECTIVE.in(schema)),
					"Teams",
					DSL.list(References.TEAM.in(schema)),
					"PlayerScores",
					DSL.list(DSL.optionalFields("display", References.TEXT_COMPONENT.in(schema)))
				)
			)
		);
		schema.registerType(
			false,
			References.SAVED_DATA_STRUCTURE_FEATURE_INDICES,
			() -> DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(References.STRUCTURE_FEATURE.in(schema))))
		);
		schema.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
		Map<String, Supplier<TypeTemplate>> map3 = V1451_6.createCriterionTypes(schema);
		schema.registerType(
			false,
			References.OBJECTIVE,
			() -> DSL.hook(
				DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map3), "DisplayName", References.TEXT_COMPONENT.in(schema)),
				V1451_6.UNPACK_OBJECTIVE_ID,
				V1451_6.REPACK_OBJECTIVE_ID
			)
		);
		schema.registerType(
			false,
			References.TEAM,
			() -> DSL.optionalFields(
				"MemberNamePrefix",
				References.TEXT_COMPONENT.in(schema),
				"MemberNameSuffix",
				References.TEXT_COMPONENT.in(schema),
				"DisplayName",
				References.TEXT_COMPONENT.in(schema)
			)
		);
		schema.registerType(
			true,
			References.UNTAGGED_SPAWNER,
			() -> DSL.optionalFields(
				"SpawnPotentials", DSL.list(DSL.fields("Entity", References.ENTITY_TREE.in(schema))), "SpawnData", References.ENTITY_TREE.in(schema)
			)
		);
		schema.registerType(
			false,
			References.ADVANCEMENTS,
			() -> DSL.optionalFields(
				"minecraft:adventure/adventuring_time",
				DSL.optionalFields("criteria", DSL.compoundList(References.BIOME.in(schema), DSL.constType(DSL.string()))),
				"minecraft:adventure/kill_a_mob",
				DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))),
				"minecraft:adventure/kill_all_mobs",
				DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))),
				"minecraft:husbandry/bred_all_animals",
				DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.string())))
			)
		);
		schema.registerType(false, References.BIOME, () -> DSL.constType(namespacedString()));
		schema.registerType(false, References.ENTITY_NAME, () -> DSL.constType(namespacedString()));
		schema.registerType(false, References.POI_CHUNK, DSL::remainder);
		schema.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
		schema.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema))));
		schema.registerType(true, References.DATA_COMPONENTS, DSL::remainder);
		schema.registerType(
			true,
			References.VILLAGER_TRADE,
			() -> DSL.optionalFields("buy", References.ITEM_STACK.in(schema), "buyB", References.ITEM_STACK.in(schema), "sell", References.ITEM_STACK.in(schema))
		);
		schema.registerType(true, References.PARTICLE, () -> DSL.constType(DSL.string()));
		schema.registerType(true, References.TEXT_COMPONENT, () -> DSL.constType(DSL.string()));
		schema.registerType(
			true,
			References.ENTITY_EQUIPMENT,
			() -> DSL.and(
				DSL.optional(DSL.field("ArmorItems", DSL.list(References.ITEM_STACK.in(schema)))),
				DSL.optional(DSL.field("HandItems", DSL.list(References.ITEM_STACK.in(schema)))),
				DSL.optional(DSL.field("body_armor_item", References.ITEM_STACK.in(schema))),
				DSL.optional(DSL.field("saddle", References.ITEM_STACK.in(schema)))
			)
		);
	}
}
