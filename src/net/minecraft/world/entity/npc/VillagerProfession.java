package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public record VillagerProfession(
	Component name,
	Predicate<Holder<PoiType>> heldJobSite,
	Predicate<Holder<PoiType>> acquirableJobSite,
	ImmutableSet<Item> requestedItems,
	ImmutableSet<Block> secondaryPoi,
	@Nullable SoundEvent workSound
) {
	public static final Predicate<Holder<PoiType>> ALL_ACQUIRABLE_JOBS = holder -> holder.is(PoiTypeTags.ACQUIRABLE_JOB_SITE);
	public static final ResourceKey<VillagerProfession> NONE = createKey("none");
	public static final ResourceKey<VillagerProfession> ARMORER = createKey("armorer");
	public static final ResourceKey<VillagerProfession> BUTCHER = createKey("butcher");
	public static final ResourceKey<VillagerProfession> CARTOGRAPHER = createKey("cartographer");
	public static final ResourceKey<VillagerProfession> CLERIC = createKey("cleric");
	public static final ResourceKey<VillagerProfession> FARMER = createKey("farmer");
	public static final ResourceKey<VillagerProfession> FISHERMAN = createKey("fisherman");
	public static final ResourceKey<VillagerProfession> FLETCHER = createKey("fletcher");
	public static final ResourceKey<VillagerProfession> LEATHERWORKER = createKey("leatherworker");
	public static final ResourceKey<VillagerProfession> LIBRARIAN = createKey("librarian");
	public static final ResourceKey<VillagerProfession> MASON = createKey("mason");
	public static final ResourceKey<VillagerProfession> NITWIT = createKey("nitwit");
	public static final ResourceKey<VillagerProfession> SHEPHERD = createKey("shepherd");
	public static final ResourceKey<VillagerProfession> TOOLSMITH = createKey("toolsmith");
	public static final ResourceKey<VillagerProfession> WEAPONSMITH = createKey("weaponsmith");

	private static ResourceKey<VillagerProfession> createKey(String string) {
		return ResourceKey.create(Registries.VILLAGER_PROFESSION, ResourceLocation.withDefaultNamespace(string));
	}

	private static VillagerProfession register(
		Registry<VillagerProfession> registry, ResourceKey<VillagerProfession> resourceKey, ResourceKey<PoiType> resourceKey2, @Nullable SoundEvent soundEvent
	) {
		return register(registry, resourceKey, holder -> holder.is(resourceKey2), holder -> holder.is(resourceKey2), soundEvent);
	}

	private static VillagerProfession register(
		Registry<VillagerProfession> registry,
		ResourceKey<VillagerProfession> resourceKey,
		Predicate<Holder<PoiType>> predicate,
		Predicate<Holder<PoiType>> predicate2,
		@Nullable SoundEvent soundEvent
	) {
		return register(registry, resourceKey, predicate, predicate2, ImmutableSet.of(), ImmutableSet.of(), soundEvent);
	}

	private static VillagerProfession register(
		Registry<VillagerProfession> registry,
		ResourceKey<VillagerProfession> resourceKey,
		ResourceKey<PoiType> resourceKey2,
		ImmutableSet<Item> immutableSet,
		ImmutableSet<Block> immutableSet2,
		@Nullable SoundEvent soundEvent
	) {
		return register(registry, resourceKey, holder -> holder.is(resourceKey2), holder -> holder.is(resourceKey2), immutableSet, immutableSet2, soundEvent);
	}

	private static VillagerProfession register(
		Registry<VillagerProfession> registry,
		ResourceKey<VillagerProfession> resourceKey,
		Predicate<Holder<PoiType>> predicate,
		Predicate<Holder<PoiType>> predicate2,
		ImmutableSet<Item> immutableSet,
		ImmutableSet<Block> immutableSet2,
		@Nullable SoundEvent soundEvent
	) {
		return Registry.register(
			registry,
			resourceKey,
			new VillagerProfession(
				Component.translatable("entity." + resourceKey.location().getNamespace() + ".villager." + resourceKey.location().getPath()),
				predicate,
				predicate2,
				immutableSet,
				immutableSet2,
				soundEvent
			)
		);
	}

	public static VillagerProfession bootstrap(Registry<VillagerProfession> registry) {
		register(registry, NONE, PoiType.NONE, ALL_ACQUIRABLE_JOBS, null);
		register(registry, ARMORER, PoiTypes.ARMORER, SoundEvents.VILLAGER_WORK_ARMORER);
		register(registry, BUTCHER, PoiTypes.BUTCHER, SoundEvents.VILLAGER_WORK_BUTCHER);
		register(registry, CARTOGRAPHER, PoiTypes.CARTOGRAPHER, SoundEvents.VILLAGER_WORK_CARTOGRAPHER);
		register(registry, CLERIC, PoiTypes.CLERIC, SoundEvents.VILLAGER_WORK_CLERIC);
		register(
			registry,
			FARMER,
			PoiTypes.FARMER,
			ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.BONE_MEAL),
			ImmutableSet.of(Blocks.FARMLAND),
			SoundEvents.VILLAGER_WORK_FARMER
		);
		register(registry, FISHERMAN, PoiTypes.FISHERMAN, SoundEvents.VILLAGER_WORK_FISHERMAN);
		register(registry, FLETCHER, PoiTypes.FLETCHER, SoundEvents.VILLAGER_WORK_FLETCHER);
		register(registry, LEATHERWORKER, PoiTypes.LEATHERWORKER, SoundEvents.VILLAGER_WORK_LEATHERWORKER);
		register(registry, LIBRARIAN, PoiTypes.LIBRARIAN, SoundEvents.VILLAGER_WORK_LIBRARIAN);
		register(registry, MASON, PoiTypes.MASON, SoundEvents.VILLAGER_WORK_MASON);
		register(registry, NITWIT, PoiType.NONE, PoiType.NONE, null);
		register(registry, SHEPHERD, PoiTypes.SHEPHERD, SoundEvents.VILLAGER_WORK_SHEPHERD);
		register(registry, TOOLSMITH, PoiTypes.TOOLSMITH, SoundEvents.VILLAGER_WORK_TOOLSMITH);
		return register(registry, WEAPONSMITH, PoiTypes.WEAPONSMITH, SoundEvents.VILLAGER_WORK_WEAPONSMITH);
	}
}
