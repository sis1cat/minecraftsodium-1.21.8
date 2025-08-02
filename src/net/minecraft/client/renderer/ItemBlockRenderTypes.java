package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Environment(EnvType.CLIENT)
public class ItemBlockRenderTypes {

	private static boolean leavesFancy;
	private static boolean redirectLeavesShouldBeFancy() {
		return leavesFancy;
	}
	private static Map<Block, ChunkSectionLayer> TYPE_BY_BLOCK = Util.make(Maps.<Block, ChunkSectionLayer>newHashMap(), hashMap -> {
		ChunkSectionLayer chunkSectionLayer = ChunkSectionLayer.TRIPWIRE;
		hashMap.put(Blocks.TRIPWIRE, chunkSectionLayer);
		ChunkSectionLayer chunkSectionLayer2 = ChunkSectionLayer.CUTOUT_MIPPED;
		hashMap.put(Blocks.GRASS_BLOCK, chunkSectionLayer2);
		hashMap.put(Blocks.IRON_BARS, chunkSectionLayer2);
		hashMap.put(Blocks.GLASS_PANE, chunkSectionLayer2);
		hashMap.put(Blocks.TRIPWIRE_HOOK, chunkSectionLayer2);
		hashMap.put(Blocks.HOPPER, chunkSectionLayer2);
		hashMap.put(Blocks.CHAIN, chunkSectionLayer2);
		hashMap.put(Blocks.JUNGLE_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.OAK_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.SPRUCE_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.ACACIA_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.CHERRY_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.BIRCH_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.DARK_OAK_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.PALE_OAK_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.AZALEA_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.FLOWERING_AZALEA_LEAVES, chunkSectionLayer2);
		hashMap.put(Blocks.MANGROVE_ROOTS, chunkSectionLayer2);
		hashMap.put(Blocks.MANGROVE_LEAVES, chunkSectionLayer2);
		ChunkSectionLayer chunkSectionLayer3 = ChunkSectionLayer.CUTOUT;
		hashMap.put(Blocks.OAK_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.SPRUCE_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.BIRCH_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.JUNGLE_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.ACACIA_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.CHERRY_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.DARK_OAK_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.PALE_OAK_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.GLASS, chunkSectionLayer3);
		hashMap.put(Blocks.WHITE_BED, chunkSectionLayer3);
		hashMap.put(Blocks.ORANGE_BED, chunkSectionLayer3);
		hashMap.put(Blocks.MAGENTA_BED, chunkSectionLayer3);
		hashMap.put(Blocks.LIGHT_BLUE_BED, chunkSectionLayer3);
		hashMap.put(Blocks.YELLOW_BED, chunkSectionLayer3);
		hashMap.put(Blocks.LIME_BED, chunkSectionLayer3);
		hashMap.put(Blocks.PINK_BED, chunkSectionLayer3);
		hashMap.put(Blocks.GRAY_BED, chunkSectionLayer3);
		hashMap.put(Blocks.LIGHT_GRAY_BED, chunkSectionLayer3);
		hashMap.put(Blocks.CYAN_BED, chunkSectionLayer3);
		hashMap.put(Blocks.PURPLE_BED, chunkSectionLayer3);
		hashMap.put(Blocks.BLUE_BED, chunkSectionLayer3);
		hashMap.put(Blocks.BROWN_BED, chunkSectionLayer3);
		hashMap.put(Blocks.GREEN_BED, chunkSectionLayer3);
		hashMap.put(Blocks.RED_BED, chunkSectionLayer3);
		hashMap.put(Blocks.BLACK_BED, chunkSectionLayer3);
		hashMap.put(Blocks.POWERED_RAIL, chunkSectionLayer3);
		hashMap.put(Blocks.DETECTOR_RAIL, chunkSectionLayer3);
		hashMap.put(Blocks.COBWEB, chunkSectionLayer3);
		hashMap.put(Blocks.SHORT_GRASS, chunkSectionLayer3);
		hashMap.put(Blocks.FERN, chunkSectionLayer3);
		hashMap.put(Blocks.BUSH, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_BUSH, chunkSectionLayer3);
		hashMap.put(Blocks.SHORT_DRY_GRASS, chunkSectionLayer3);
		hashMap.put(Blocks.TALL_DRY_GRASS, chunkSectionLayer3);
		hashMap.put(Blocks.SEAGRASS, chunkSectionLayer3);
		hashMap.put(Blocks.TALL_SEAGRASS, chunkSectionLayer3);
		hashMap.put(Blocks.DANDELION, chunkSectionLayer3);
		hashMap.put(Blocks.OPEN_EYEBLOSSOM, chunkSectionLayer3);
		hashMap.put(Blocks.CLOSED_EYEBLOSSOM, chunkSectionLayer3);
		hashMap.put(Blocks.POPPY, chunkSectionLayer3);
		hashMap.put(Blocks.BLUE_ORCHID, chunkSectionLayer3);
		hashMap.put(Blocks.ALLIUM, chunkSectionLayer3);
		hashMap.put(Blocks.AZURE_BLUET, chunkSectionLayer3);
		hashMap.put(Blocks.RED_TULIP, chunkSectionLayer3);
		hashMap.put(Blocks.ORANGE_TULIP, chunkSectionLayer3);
		hashMap.put(Blocks.WHITE_TULIP, chunkSectionLayer3);
		hashMap.put(Blocks.PINK_TULIP, chunkSectionLayer3);
		hashMap.put(Blocks.OXEYE_DAISY, chunkSectionLayer3);
		hashMap.put(Blocks.CORNFLOWER, chunkSectionLayer3);
		hashMap.put(Blocks.WITHER_ROSE, chunkSectionLayer3);
		hashMap.put(Blocks.LILY_OF_THE_VALLEY, chunkSectionLayer3);
		hashMap.put(Blocks.BROWN_MUSHROOM, chunkSectionLayer3);
		hashMap.put(Blocks.RED_MUSHROOM, chunkSectionLayer3);
		hashMap.put(Blocks.TORCH, chunkSectionLayer3);
		hashMap.put(Blocks.WALL_TORCH, chunkSectionLayer3);
		hashMap.put(Blocks.SOUL_TORCH, chunkSectionLayer3);
		hashMap.put(Blocks.SOUL_WALL_TORCH, chunkSectionLayer3);
		hashMap.put(Blocks.FIRE, chunkSectionLayer3);
		hashMap.put(Blocks.SOUL_FIRE, chunkSectionLayer3);
		hashMap.put(Blocks.SPAWNER, chunkSectionLayer3);
		hashMap.put(Blocks.TRIAL_SPAWNER, chunkSectionLayer3);
		hashMap.put(Blocks.VAULT, chunkSectionLayer3);
		hashMap.put(Blocks.REDSTONE_WIRE, chunkSectionLayer3);
		hashMap.put(Blocks.WHEAT, chunkSectionLayer3);
		hashMap.put(Blocks.OAK_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.LADDER, chunkSectionLayer3);
		hashMap.put(Blocks.RAIL, chunkSectionLayer3);
		hashMap.put(Blocks.IRON_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.REDSTONE_TORCH, chunkSectionLayer3);
		hashMap.put(Blocks.REDSTONE_WALL_TORCH, chunkSectionLayer3);
		hashMap.put(Blocks.CACTUS, chunkSectionLayer3);
		hashMap.put(Blocks.SUGAR_CANE, chunkSectionLayer3);
		hashMap.put(Blocks.REPEATER, chunkSectionLayer3);
		hashMap.put(Blocks.OAK_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.SPRUCE_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.BIRCH_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.JUNGLE_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.ACACIA_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.CHERRY_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.DARK_OAK_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.PALE_OAK_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.CRIMSON_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WARPED_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.MANGROVE_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.BAMBOO_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.COPPER_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.EXPOSED_COPPER_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WEATHERED_COPPER_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.OXIDIZED_COPPER_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_COPPER_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.ATTACHED_PUMPKIN_STEM, chunkSectionLayer3);
		hashMap.put(Blocks.ATTACHED_MELON_STEM, chunkSectionLayer3);
		hashMap.put(Blocks.PUMPKIN_STEM, chunkSectionLayer3);
		hashMap.put(Blocks.MELON_STEM, chunkSectionLayer3);
		hashMap.put(Blocks.VINE, chunkSectionLayer3);
		hashMap.put(Blocks.PALE_MOSS_CARPET, chunkSectionLayer3);
		hashMap.put(Blocks.PALE_HANGING_MOSS, chunkSectionLayer3);
		hashMap.put(Blocks.GLOW_LICHEN, chunkSectionLayer3);
		hashMap.put(Blocks.RESIN_CLUMP, chunkSectionLayer3);
		hashMap.put(Blocks.LILY_PAD, chunkSectionLayer3);
		hashMap.put(Blocks.NETHER_WART, chunkSectionLayer3);
		hashMap.put(Blocks.BREWING_STAND, chunkSectionLayer3);
		hashMap.put(Blocks.COCOA, chunkSectionLayer3);
		hashMap.put(Blocks.BEACON, chunkSectionLayer3);
		hashMap.put(Blocks.FLOWER_POT, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_OAK_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_SPRUCE_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_BIRCH_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_JUNGLE_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_ACACIA_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_CHERRY_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_DARK_OAK_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_PALE_OAK_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_MANGROVE_PROPAGULE, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_FERN, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_DANDELION, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_POPPY, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_OPEN_EYEBLOSSOM, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_CLOSED_EYEBLOSSOM, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_BLUE_ORCHID, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_ALLIUM, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_AZURE_BLUET, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_RED_TULIP, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_ORANGE_TULIP, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_WHITE_TULIP, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_PINK_TULIP, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_OXEYE_DAISY, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_CORNFLOWER, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_LILY_OF_THE_VALLEY, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_WITHER_ROSE, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_RED_MUSHROOM, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_BROWN_MUSHROOM, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_DEAD_BUSH, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_CACTUS, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_AZALEA, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_FLOWERING_AZALEA, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_TORCHFLOWER, chunkSectionLayer3);
		hashMap.put(Blocks.CARROTS, chunkSectionLayer3);
		hashMap.put(Blocks.POTATOES, chunkSectionLayer3);
		hashMap.put(Blocks.COMPARATOR, chunkSectionLayer3);
		hashMap.put(Blocks.ACTIVATOR_RAIL, chunkSectionLayer3);
		hashMap.put(Blocks.IRON_TRAPDOOR, chunkSectionLayer3);
		hashMap.put(Blocks.SUNFLOWER, chunkSectionLayer3);
		hashMap.put(Blocks.LILAC, chunkSectionLayer3);
		hashMap.put(Blocks.ROSE_BUSH, chunkSectionLayer3);
		hashMap.put(Blocks.PEONY, chunkSectionLayer3);
		hashMap.put(Blocks.TALL_GRASS, chunkSectionLayer3);
		hashMap.put(Blocks.LARGE_FERN, chunkSectionLayer3);
		hashMap.put(Blocks.SPRUCE_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.BIRCH_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.JUNGLE_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.ACACIA_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.CHERRY_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.DARK_OAK_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.PALE_OAK_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.MANGROVE_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.BAMBOO_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.COPPER_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.EXPOSED_COPPER_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WEATHERED_COPPER_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.OXIDIZED_COPPER_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_COPPER_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_EXPOSED_COPPER_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_WEATHERED_COPPER_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_OXIDIZED_COPPER_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.END_ROD, chunkSectionLayer3);
		hashMap.put(Blocks.CHORUS_PLANT, chunkSectionLayer3);
		hashMap.put(Blocks.CHORUS_FLOWER, chunkSectionLayer3);
		hashMap.put(Blocks.TORCHFLOWER, chunkSectionLayer3);
		hashMap.put(Blocks.TORCHFLOWER_CROP, chunkSectionLayer3);
		hashMap.put(Blocks.PITCHER_PLANT, chunkSectionLayer3);
		hashMap.put(Blocks.PITCHER_CROP, chunkSectionLayer3);
		hashMap.put(Blocks.BEETROOTS, chunkSectionLayer3);
		hashMap.put(Blocks.KELP, chunkSectionLayer3);
		hashMap.put(Blocks.KELP_PLANT, chunkSectionLayer3);
		hashMap.put(Blocks.TURTLE_EGG, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_TUBE_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_FIRE_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_HORN_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.TUBE_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.BRAIN_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.BUBBLE_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.FIRE_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.HORN_CORAL, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_TUBE_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_FIRE_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_HORN_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.TUBE_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.BRAIN_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.BUBBLE_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.FIRE_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.HORN_CORAL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.TUBE_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.BRAIN_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.BUBBLE_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.FIRE_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.HORN_CORAL_WALL_FAN, chunkSectionLayer3);
		hashMap.put(Blocks.SEA_PICKLE, chunkSectionLayer3);
		hashMap.put(Blocks.CONDUIT, chunkSectionLayer3);
		hashMap.put(Blocks.BAMBOO_SAPLING, chunkSectionLayer3);
		hashMap.put(Blocks.BAMBOO, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_BAMBOO, chunkSectionLayer3);
		hashMap.put(Blocks.SCAFFOLDING, chunkSectionLayer3);
		hashMap.put(Blocks.STONECUTTER, chunkSectionLayer3);
		hashMap.put(Blocks.LANTERN, chunkSectionLayer3);
		hashMap.put(Blocks.SOUL_LANTERN, chunkSectionLayer3);
		hashMap.put(Blocks.CAMPFIRE, chunkSectionLayer3);
		hashMap.put(Blocks.SOUL_CAMPFIRE, chunkSectionLayer3);
		hashMap.put(Blocks.SWEET_BERRY_BUSH, chunkSectionLayer3);
		hashMap.put(Blocks.WEEPING_VINES, chunkSectionLayer3);
		hashMap.put(Blocks.WEEPING_VINES_PLANT, chunkSectionLayer3);
		hashMap.put(Blocks.TWISTING_VINES, chunkSectionLayer3);
		hashMap.put(Blocks.TWISTING_VINES_PLANT, chunkSectionLayer3);
		hashMap.put(Blocks.NETHER_SPROUTS, chunkSectionLayer3);
		hashMap.put(Blocks.CRIMSON_FUNGUS, chunkSectionLayer3);
		hashMap.put(Blocks.WARPED_FUNGUS, chunkSectionLayer3);
		hashMap.put(Blocks.CRIMSON_ROOTS, chunkSectionLayer3);
		hashMap.put(Blocks.WARPED_ROOTS, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_CRIMSON_FUNGUS, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_WARPED_FUNGUS, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_CRIMSON_ROOTS, chunkSectionLayer3);
		hashMap.put(Blocks.POTTED_WARPED_ROOTS, chunkSectionLayer3);
		hashMap.put(Blocks.CRIMSON_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.WARPED_DOOR, chunkSectionLayer3);
		hashMap.put(Blocks.POINTED_DRIPSTONE, chunkSectionLayer3);
		hashMap.put(Blocks.SMALL_AMETHYST_BUD, chunkSectionLayer3);
		hashMap.put(Blocks.MEDIUM_AMETHYST_BUD, chunkSectionLayer3);
		hashMap.put(Blocks.LARGE_AMETHYST_BUD, chunkSectionLayer3);
		hashMap.put(Blocks.AMETHYST_CLUSTER, chunkSectionLayer3);
		hashMap.put(Blocks.LIGHTNING_ROD, chunkSectionLayer3);
		hashMap.put(Blocks.CAVE_VINES, chunkSectionLayer3);
		hashMap.put(Blocks.CAVE_VINES_PLANT, chunkSectionLayer3);
		hashMap.put(Blocks.SPORE_BLOSSOM, chunkSectionLayer3);
		hashMap.put(Blocks.FLOWERING_AZALEA, chunkSectionLayer3);
		hashMap.put(Blocks.AZALEA, chunkSectionLayer3);
		hashMap.put(Blocks.PINK_PETALS, chunkSectionLayer3);
		hashMap.put(Blocks.WILDFLOWERS, chunkSectionLayer3);
		hashMap.put(Blocks.LEAF_LITTER, chunkSectionLayer3);
		hashMap.put(Blocks.BIG_DRIPLEAF, chunkSectionLayer3);
		hashMap.put(Blocks.BIG_DRIPLEAF_STEM, chunkSectionLayer3);
		hashMap.put(Blocks.SMALL_DRIPLEAF, chunkSectionLayer3);
		hashMap.put(Blocks.HANGING_ROOTS, chunkSectionLayer3);
		hashMap.put(Blocks.SCULK_SENSOR, chunkSectionLayer3);
		hashMap.put(Blocks.CALIBRATED_SCULK_SENSOR, chunkSectionLayer3);
		hashMap.put(Blocks.SCULK_VEIN, chunkSectionLayer3);
		hashMap.put(Blocks.SCULK_SHRIEKER, chunkSectionLayer3);
		hashMap.put(Blocks.MANGROVE_PROPAGULE, chunkSectionLayer3);
		hashMap.put(Blocks.FROGSPAWN, chunkSectionLayer3);
		hashMap.put(Blocks.COPPER_GRATE, chunkSectionLayer3);
		hashMap.put(Blocks.EXPOSED_COPPER_GRATE, chunkSectionLayer3);
		hashMap.put(Blocks.WEATHERED_COPPER_GRATE, chunkSectionLayer3);
		hashMap.put(Blocks.OXIDIZED_COPPER_GRATE, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_COPPER_GRATE, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_EXPOSED_COPPER_GRATE, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_WEATHERED_COPPER_GRATE, chunkSectionLayer3);
		hashMap.put(Blocks.WAXED_OXIDIZED_COPPER_GRATE, chunkSectionLayer3);
		hashMap.put(Blocks.FIREFLY_BUSH, chunkSectionLayer3);
		hashMap.put(Blocks.CACTUS_FLOWER, chunkSectionLayer3);
		ChunkSectionLayer chunkSectionLayer4 = ChunkSectionLayer.TRANSLUCENT;
		hashMap.put(Blocks.ICE, chunkSectionLayer4);
		hashMap.put(Blocks.NETHER_PORTAL, chunkSectionLayer4);
		hashMap.put(Blocks.WHITE_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.ORANGE_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.MAGENTA_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.YELLOW_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.LIME_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.PINK_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.GRAY_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.CYAN_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.PURPLE_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.BLUE_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.BROWN_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.GREEN_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.RED_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.BLACK_STAINED_GLASS, chunkSectionLayer4);
		hashMap.put(Blocks.WHITE_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.ORANGE_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.MAGENTA_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.YELLOW_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.LIME_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.PINK_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.GRAY_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.CYAN_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.PURPLE_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.BLUE_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.BROWN_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.GREEN_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.RED_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.BLACK_STAINED_GLASS_PANE, chunkSectionLayer4);
		hashMap.put(Blocks.SLIME_BLOCK, chunkSectionLayer4);
		hashMap.put(Blocks.HONEY_BLOCK, chunkSectionLayer4);
		hashMap.put(Blocks.FROSTED_ICE, chunkSectionLayer4);
		hashMap.put(Blocks.BUBBLE_COLUMN, chunkSectionLayer4);
		hashMap.put(Blocks.TINTED_GLASS, chunkSectionLayer4);
	});
	private static Map<Fluid, ChunkSectionLayer> LAYER_BY_FLUID = Util.make(Maps.<Fluid, ChunkSectionLayer>newHashMap(), hashMap -> {
		hashMap.put(Fluids.FLOWING_WATER, ChunkSectionLayer.TRANSLUCENT);
		hashMap.put(Fluids.WATER, ChunkSectionLayer.TRANSLUCENT);
	});

	private static boolean renderCutout;

	public static ChunkSectionLayer getChunkRenderType(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof LeavesBlock) {
			return redirectLeavesShouldBeFancy() ? ChunkSectionLayer.CUTOUT_MIPPED : ChunkSectionLayer.SOLID;
		} else {
			ChunkSectionLayer chunkSectionLayer = (ChunkSectionLayer)TYPE_BY_BLOCK.get(block);
			return chunkSectionLayer != null ? chunkSectionLayer : ChunkSectionLayer.SOLID;
		}
	}

	public static RenderType getMovingBlockRenderType(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof LeavesBlock) {
			return renderCutout ? RenderType.cutoutMipped() : RenderType.solid();
		} else {
			ChunkSectionLayer chunkSectionLayer = (ChunkSectionLayer)TYPE_BY_BLOCK.get(block);
			if (chunkSectionLayer != null) {
				return switch (chunkSectionLayer) {
					case SOLID -> RenderType.solid();
					case CUTOUT_MIPPED -> RenderType.cutoutMipped();
					case CUTOUT -> RenderType.cutout();
					case TRANSLUCENT -> RenderType.translucentMovingBlock();
					case TRIPWIRE -> RenderType.tripwire();
				};
			} else {
				return RenderType.solid();
			}
		}
	}

	public static RenderType getRenderType(BlockState blockState) {
		ChunkSectionLayer chunkSectionLayer = getChunkRenderType(blockState);
		return chunkSectionLayer == ChunkSectionLayer.TRANSLUCENT ? Sheets.translucentItemSheet() : Sheets.cutoutBlockSheet();
	}

	public static RenderType getRenderType(ItemStack itemStack) {
		if (itemStack.getItem() instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			return getRenderType(block.defaultBlockState());
		} else {
			return Sheets.translucentItemSheet();
		}
	}

	public static ChunkSectionLayer getRenderLayer(FluidState fluidState) {
		ChunkSectionLayer chunkSectionLayer = (ChunkSectionLayer)LAYER_BY_FLUID.get(fluidState.getType());
		return chunkSectionLayer != null ? chunkSectionLayer : ChunkSectionLayer.SOLID;
	}

	public static void setFancy(boolean bl) {
		renderCutout = bl;
		leavesFancy = SodiumClientMod.options().quality.leavesQuality.isFancy(bl ? GraphicsStatus.FANCY : GraphicsStatus.FAST);
	}
}
