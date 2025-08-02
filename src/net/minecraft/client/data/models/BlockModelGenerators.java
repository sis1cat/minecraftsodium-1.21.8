package net.minecraft.client.data.models;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quadrant;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.client.renderer.block.model.multipart.CombinedCondition;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.BannerSpecialRenderer;
import net.minecraft.client.renderer.special.BedSpecialRenderer;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.renderer.special.ConduitSpecialRenderer;
import net.minecraft.client.renderer.special.DecoratedPotSpecialRenderer;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.client.renderer.special.ShulkerBoxSpecialRenderer;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.DriedGhastBlock;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.TestBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.TestBlockMode;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.block.state.properties.WallSide;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlockModelGenerators {
	final Consumer<BlockModelDefinitionGenerator> blockStateOutput;
	final ItemModelOutput itemModelOutput;
	final BiConsumer<ResourceLocation, ModelInstance> modelOutput;
	static final List<Block> NON_ORIENTABLE_TRAPDOOR = List.of(Blocks.OAK_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.IRON_TRAPDOOR);
	public static final VariantMutator NOP = variant -> variant;
	public static final VariantMutator UV_LOCK = VariantMutator.UV_LOCK.withValue(true);
	public static final VariantMutator X_ROT_90 = VariantMutator.X_ROT.withValue(Quadrant.R90);
	public static final VariantMutator X_ROT_180 = VariantMutator.X_ROT.withValue(Quadrant.R180);
	public static final VariantMutator X_ROT_270 = VariantMutator.X_ROT.withValue(Quadrant.R270);
	public static final VariantMutator Y_ROT_90 = VariantMutator.Y_ROT.withValue(Quadrant.R90);
	public static final VariantMutator Y_ROT_180 = VariantMutator.Y_ROT.withValue(Quadrant.R180);
	public static final VariantMutator Y_ROT_270 = VariantMutator.Y_ROT.withValue(Quadrant.R270);
	private static final Function<ConditionBuilder, ConditionBuilder> FLOWER_BED_MODEL_1_SEGMENT_CONDITION = conditionBuilder -> conditionBuilder;
	private static final Function<ConditionBuilder, ConditionBuilder> FLOWER_BED_MODEL_2_SEGMENT_CONDITION = conditionBuilder -> conditionBuilder.term(
		BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4
	);
	private static final Function<ConditionBuilder, ConditionBuilder> FLOWER_BED_MODEL_3_SEGMENT_CONDITION = conditionBuilder -> conditionBuilder.term(
		BlockStateProperties.FLOWER_AMOUNT, 3, 4
	);
	private static final Function<ConditionBuilder, ConditionBuilder> FLOWER_BED_MODEL_4_SEGMENT_CONDITION = conditionBuilder -> conditionBuilder.term(
		BlockStateProperties.FLOWER_AMOUNT, 4
	);
	private static final Function<ConditionBuilder, ConditionBuilder> LEAF_LITTER_MODEL_1_SEGMENT_CONDITION = conditionBuilder -> conditionBuilder.term(
		BlockStateProperties.SEGMENT_AMOUNT, 1
	);
	private static final Function<ConditionBuilder, ConditionBuilder> LEAF_LITTER_MODEL_2_SEGMENT_CONDITION = conditionBuilder -> conditionBuilder.term(
		BlockStateProperties.SEGMENT_AMOUNT, 2, 3
	);
	private static final Function<ConditionBuilder, ConditionBuilder> LEAF_LITTER_MODEL_3_SEGMENT_CONDITION = conditionBuilder -> conditionBuilder.term(
		BlockStateProperties.SEGMENT_AMOUNT, 3
	);
	private static final Function<ConditionBuilder, ConditionBuilder> LEAF_LITTER_MODEL_4_SEGMENT_CONDITION = conditionBuilder -> conditionBuilder.term(
		BlockStateProperties.SEGMENT_AMOUNT, 4
	);
	static final Map<Block, BlockModelGenerators.BlockStateGeneratorSupplier> FULL_BLOCK_MODEL_CUSTOM_GENERATORS = Map.of(
		Blocks.STONE,
		BlockModelGenerators::createMirroredCubeGenerator,
		Blocks.DEEPSLATE,
		BlockModelGenerators::createMirroredColumnGenerator,
		Blocks.MUD_BRICKS,
		BlockModelGenerators::createNorthWestMirroredCubeGenerator
	);
	private static final PropertyDispatch<VariantMutator> ROTATION_FACING = PropertyDispatch.modify(BlockStateProperties.FACING)
		.select(Direction.DOWN, X_ROT_90)
		.select(Direction.UP, X_ROT_270)
		.select(Direction.NORTH, NOP)
		.select(Direction.SOUTH, Y_ROT_180)
		.select(Direction.WEST, Y_ROT_270)
		.select(Direction.EAST, Y_ROT_90);
	private static final PropertyDispatch<VariantMutator> ROTATIONS_COLUMN_WITH_FACING = PropertyDispatch.modify(BlockStateProperties.FACING)
		.select(Direction.DOWN, X_ROT_180)
		.select(Direction.UP, NOP)
		.select(Direction.NORTH, X_ROT_90)
		.select(Direction.SOUTH, X_ROT_90.then(Y_ROT_180))
		.select(Direction.WEST, X_ROT_90.then(Y_ROT_270))
		.select(Direction.EAST, X_ROT_90.then(Y_ROT_90));
	private static final PropertyDispatch<VariantMutator> ROTATION_TORCH = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
		.select(Direction.EAST, NOP)
		.select(Direction.SOUTH, Y_ROT_90)
		.select(Direction.WEST, Y_ROT_180)
		.select(Direction.NORTH, Y_ROT_270);
	private static final PropertyDispatch<VariantMutator> ROTATION_HORIZONTAL_FACING_ALT = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
		.select(Direction.SOUTH, NOP)
		.select(Direction.WEST, Y_ROT_90)
		.select(Direction.NORTH, Y_ROT_180)
		.select(Direction.EAST, Y_ROT_270);
	private static final PropertyDispatch<VariantMutator> ROTATION_HORIZONTAL_FACING = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
		.select(Direction.EAST, Y_ROT_90)
		.select(Direction.SOUTH, Y_ROT_180)
		.select(Direction.WEST, Y_ROT_270)
		.select(Direction.NORTH, NOP);
	static final Map<Block, TexturedModel> TEXTURED_MODELS = ImmutableMap.<Block, TexturedModel>builder()
		.put(Blocks.SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.SANDSTONE))
		.put(Blocks.RED_SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.RED_SANDSTONE))
		.put(Blocks.SMOOTH_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top")))
		.put(Blocks.SMOOTH_RED_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top")))
		.put(
			Blocks.CUT_SANDSTONE,
			TexturedModel.COLUMN
				.get(Blocks.SANDSTONE)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_SANDSTONE)))
		)
		.put(
			Blocks.CUT_RED_SANDSTONE,
			TexturedModel.COLUMN
				.get(Blocks.RED_SANDSTONE)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_RED_SANDSTONE)))
		)
		.put(Blocks.QUARTZ_BLOCK, TexturedModel.COLUMN.get(Blocks.QUARTZ_BLOCK))
		.put(Blocks.SMOOTH_QUARTZ, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.QUARTZ_BLOCK, "_bottom")))
		.put(Blocks.BLACKSTONE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.BLACKSTONE))
		.put(Blocks.DEEPSLATE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.DEEPSLATE))
		.put(
			Blocks.CHISELED_QUARTZ_BLOCK,
			TexturedModel.COLUMN
				.get(Blocks.CHISELED_QUARTZ_BLOCK)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_QUARTZ_BLOCK)))
		)
		.put(Blocks.CHISELED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_SANDSTONE).updateTextures(textureMapping -> {
			textureMapping.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top"));
			textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_SANDSTONE));
		}))
		.put(Blocks.CHISELED_RED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_RED_SANDSTONE).updateTextures(textureMapping -> {
			textureMapping.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top"));
			textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_RED_SANDSTONE));
		}))
		.put(Blocks.CHISELED_TUFF_BRICKS, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF_BRICKS))
		.put(Blocks.CHISELED_TUFF, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF))
		.build();
	static final Map<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>> SHAPE_CONSUMERS = ImmutableMap.<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>>builder()
		.put(BlockFamily.Variant.BUTTON, BlockModelGenerators.BlockFamilyProvider::button)
		.put(BlockFamily.Variant.DOOR, BlockModelGenerators.BlockFamilyProvider::door)
		.put(BlockFamily.Variant.CHISELED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
		.put(BlockFamily.Variant.CRACKED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
		.put(BlockFamily.Variant.CUSTOM_FENCE, BlockModelGenerators.BlockFamilyProvider::customFence)
		.put(BlockFamily.Variant.FENCE, BlockModelGenerators.BlockFamilyProvider::fence)
		.put(BlockFamily.Variant.CUSTOM_FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::customFenceGate)
		.put(BlockFamily.Variant.FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::fenceGate)
		.put(BlockFamily.Variant.SIGN, BlockModelGenerators.BlockFamilyProvider::sign)
		.put(BlockFamily.Variant.SLAB, BlockModelGenerators.BlockFamilyProvider::slab)
		.put(BlockFamily.Variant.STAIRS, BlockModelGenerators.BlockFamilyProvider::stairs)
		.put(BlockFamily.Variant.PRESSURE_PLATE, BlockModelGenerators.BlockFamilyProvider::pressurePlate)
		.put(BlockFamily.Variant.TRAPDOOR, BlockModelGenerators.BlockFamilyProvider::trapdoor)
		.put(BlockFamily.Variant.WALL, BlockModelGenerators.BlockFamilyProvider::wall)
		.build();
	private static final Map<Direction, VariantMutator> MULTIFACE_GENERATOR = ImmutableMap.of(
		Direction.NORTH,
		NOP,
		Direction.EAST,
		Y_ROT_90.then(UV_LOCK),
		Direction.SOUTH,
		Y_ROT_180.then(UV_LOCK),
		Direction.WEST,
		Y_ROT_270.then(UV_LOCK),
		Direction.UP,
		X_ROT_270.then(UV_LOCK),
		Direction.DOWN,
		X_ROT_90.then(UV_LOCK)
	);
	private static final Map<BlockModelGenerators.BookSlotModelCacheKey, ResourceLocation> CHISELED_BOOKSHELF_SLOT_MODEL_CACHE = new HashMap();

	static Variant plainModel(ResourceLocation resourceLocation) {
		return new Variant(resourceLocation);
	}

	static MultiVariant variant(Variant variant) {
		return new MultiVariant(WeightedList.of(variant));
	}

	private static MultiVariant variants(Variant... variants) {
		return new MultiVariant(WeightedList.of(Arrays.stream(variants).map(variant -> new Weighted<>(variant, 1)).toList()));
	}

	static MultiVariant plainVariant(ResourceLocation resourceLocation) {
		return variant(plainModel(resourceLocation));
	}

	private static ConditionBuilder condition() {
		return new ConditionBuilder();
	}

	private static Condition or(ConditionBuilder... conditionBuilders) {
		return new CombinedCondition(CombinedCondition.Operation.OR, Stream.of(conditionBuilders).map(ConditionBuilder::build).toList());
	}

	private static BlockModelDefinitionGenerator createMirroredCubeGenerator(
		Block block, Variant variant, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer
	) {
		Variant variant2 = plainModel(ModelTemplates.CUBE_MIRRORED_ALL.create(block, textureMapping, biConsumer));
		return MultiVariantGenerator.dispatch(block, createRotatedVariants(variant, variant2));
	}

	private static BlockModelDefinitionGenerator createNorthWestMirroredCubeGenerator(
		Block block, Variant variant, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer
	) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.CUBE_NORTH_WEST_MIRRORED_ALL.create(block, textureMapping, biConsumer));
		return createSimpleBlock(block, multiVariant);
	}

	private static BlockModelDefinitionGenerator createMirroredColumnGenerator(
		Block block, Variant variant, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer
	) {
		Variant variant2 = plainModel(ModelTemplates.CUBE_COLUMN_MIRRORED.create(block, textureMapping, biConsumer));
		return MultiVariantGenerator.dispatch(block, createRotatedVariants(variant, variant2)).with(createRotatedPillar());
	}

	public BlockModelGenerators(
		Consumer<BlockModelDefinitionGenerator> consumer, ItemModelOutput itemModelOutput, BiConsumer<ResourceLocation, ModelInstance> biConsumer
	) {
		this.blockStateOutput = consumer;
		this.itemModelOutput = itemModelOutput;
		this.modelOutput = biConsumer;
	}

	private void registerSimpleItemModel(Item item, ResourceLocation resourceLocation) {
		this.itemModelOutput.accept(item, ItemModelUtils.plainModel(resourceLocation));
	}

	void registerSimpleItemModel(Block block, ResourceLocation resourceLocation) {
		this.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(resourceLocation));
	}

	private void registerSimpleTintedItemModel(Block block, ResourceLocation resourceLocation, ItemTintSource itemTintSource) {
		this.itemModelOutput.accept(block.asItem(), ItemModelUtils.tintedModel(resourceLocation, itemTintSource));
	}

	private ResourceLocation createFlatItemModel(Item item) {
		return ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), this.modelOutput);
	}

	ResourceLocation createFlatItemModelWithBlockTexture(Item item, Block block) {
		return ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(block), this.modelOutput);
	}

	private ResourceLocation createFlatItemModelWithBlockTexture(Item item, Block block, String string) {
		return ModelTemplates.FLAT_ITEM
			.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(TextureMapping.getBlockTexture(block, string)), this.modelOutput);
	}

	ResourceLocation createFlatItemModelWithBlockTextureAndOverlay(Item item, Block block, String string) {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(block);
		ResourceLocation resourceLocation2 = TextureMapping.getBlockTexture(block, string);
		return ModelTemplates.TWO_LAYERED_ITEM
			.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layered(resourceLocation, resourceLocation2), this.modelOutput);
	}

	void registerSimpleFlatItemModel(Item item) {
		this.registerSimpleItemModel(item, this.createFlatItemModel(item));
	}

	private void registerSimpleFlatItemModel(Block block) {
		Item item = block.asItem();
		if (item != Items.AIR) {
			this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, block));
		}
	}

	private void registerSimpleFlatItemModel(Block block, String string) {
		Item item = block.asItem();
		if (item != Items.AIR) {
			this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, block, string));
		}
	}

	private void registerTwoLayerFlatItemModel(Block block, String string) {
		Item item = block.asItem();
		if (item != Items.AIR) {
			ResourceLocation resourceLocation = this.createFlatItemModelWithBlockTextureAndOverlay(item, block, string);
			this.registerSimpleItemModel(item, resourceLocation);
		}
	}

	private static MultiVariant createRotatedVariants(Variant variant) {
		return variants(variant, variant.with(Y_ROT_90), variant.with(Y_ROT_180), variant.with(Y_ROT_270));
	}

	private static MultiVariant createRotatedVariants(Variant variant, Variant variant2) {
		return variants(variant, variant2, variant.with(Y_ROT_180), variant2.with(Y_ROT_180));
	}

	private static PropertyDispatch<MultiVariant> createBooleanModelDispatch(
		BooleanProperty booleanProperty, MultiVariant multiVariant, MultiVariant multiVariant2
	) {
		return PropertyDispatch.initial(booleanProperty).select(true, multiVariant).select(false, multiVariant2);
	}

	private void createRotatedMirroredVariantBlock(Block block) {
		Variant variant = plainModel(TexturedModel.CUBE.create(block, this.modelOutput));
		Variant variant2 = plainModel(TexturedModel.CUBE_MIRRORED.create(block, this.modelOutput));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, createRotatedVariants(variant, variant2)));
	}

	private void createRotatedVariantBlock(Block block) {
		Variant variant = plainModel(TexturedModel.CUBE.create(block, this.modelOutput));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, createRotatedVariants(variant)));
	}

	private void createBrushableBlock(Block block) {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(
						PropertyDispatch.initial(BlockStateProperties.DUSTED)
							.generate(
								integer -> {
									String string = "_" + integer;
									ResourceLocation resourceLocation = TextureMapping.getBlockTexture(block, string);
									ResourceLocation resourceLocation2 = ModelTemplates.CUBE_ALL
										.createWithSuffix(block, string, new TextureMapping().put(TextureSlot.ALL, resourceLocation), this.modelOutput);
									return plainVariant(resourceLocation2);
								}
							)
					)
			);
		this.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block, "_0"));
	}

	static BlockModelDefinitionGenerator createButton(Block block, MultiVariant multiVariant, MultiVariant multiVariant2) {
		return MultiVariantGenerator.dispatch(block)
			.with(PropertyDispatch.initial(BlockStateProperties.POWERED).select(false, multiVariant).select(true, multiVariant2))
			.with(
				PropertyDispatch.modify(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
					.select(AttachFace.FLOOR, Direction.EAST, Y_ROT_90)
					.select(AttachFace.FLOOR, Direction.WEST, Y_ROT_270)
					.select(AttachFace.FLOOR, Direction.SOUTH, Y_ROT_180)
					.select(AttachFace.FLOOR, Direction.NORTH, NOP)
					.select(AttachFace.WALL, Direction.EAST, Y_ROT_90.then(X_ROT_90).then(UV_LOCK))
					.select(AttachFace.WALL, Direction.WEST, Y_ROT_270.then(X_ROT_90).then(UV_LOCK))
					.select(AttachFace.WALL, Direction.SOUTH, Y_ROT_180.then(X_ROT_90).then(UV_LOCK))
					.select(AttachFace.WALL, Direction.NORTH, X_ROT_90.then(UV_LOCK))
					.select(AttachFace.CEILING, Direction.EAST, Y_ROT_270.then(X_ROT_180))
					.select(AttachFace.CEILING, Direction.WEST, Y_ROT_90.then(X_ROT_180))
					.select(AttachFace.CEILING, Direction.SOUTH, X_ROT_180)
					.select(AttachFace.CEILING, Direction.NORTH, Y_ROT_180.then(X_ROT_180))
			);
	}

	private static BlockModelDefinitionGenerator createDoor(
		Block block,
		MultiVariant multiVariant,
		MultiVariant multiVariant2,
		MultiVariant multiVariant3,
		MultiVariant multiVariant4,
		MultiVariant multiVariant5,
		MultiVariant multiVariant6,
		MultiVariant multiVariant7,
		MultiVariant multiVariant8
	) {
		return MultiVariantGenerator.dispatch(block)
			.with(
				PropertyDispatch.initial(
						BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.DOUBLE_BLOCK_HALF, BlockStateProperties.DOOR_HINGE, BlockStateProperties.OPEN
					)
					.select(Direction.EAST, DoubleBlockHalf.LOWER, DoorHingeSide.LEFT, false, multiVariant)
					.select(Direction.SOUTH, DoubleBlockHalf.LOWER, DoorHingeSide.LEFT, false, multiVariant.with(Y_ROT_90))
					.select(Direction.WEST, DoubleBlockHalf.LOWER, DoorHingeSide.LEFT, false, multiVariant.with(Y_ROT_180))
					.select(Direction.NORTH, DoubleBlockHalf.LOWER, DoorHingeSide.LEFT, false, multiVariant.with(Y_ROT_270))
					.select(Direction.EAST, DoubleBlockHalf.LOWER, DoorHingeSide.RIGHT, false, multiVariant3)
					.select(Direction.SOUTH, DoubleBlockHalf.LOWER, DoorHingeSide.RIGHT, false, multiVariant3.with(Y_ROT_90))
					.select(Direction.WEST, DoubleBlockHalf.LOWER, DoorHingeSide.RIGHT, false, multiVariant3.with(Y_ROT_180))
					.select(Direction.NORTH, DoubleBlockHalf.LOWER, DoorHingeSide.RIGHT, false, multiVariant3.with(Y_ROT_270))
					.select(Direction.EAST, DoubleBlockHalf.LOWER, DoorHingeSide.LEFT, true, multiVariant2.with(Y_ROT_90))
					.select(Direction.SOUTH, DoubleBlockHalf.LOWER, DoorHingeSide.LEFT, true, multiVariant2.with(Y_ROT_180))
					.select(Direction.WEST, DoubleBlockHalf.LOWER, DoorHingeSide.LEFT, true, multiVariant2.with(Y_ROT_270))
					.select(Direction.NORTH, DoubleBlockHalf.LOWER, DoorHingeSide.LEFT, true, multiVariant2)
					.select(Direction.EAST, DoubleBlockHalf.LOWER, DoorHingeSide.RIGHT, true, multiVariant4.with(Y_ROT_270))
					.select(Direction.SOUTH, DoubleBlockHalf.LOWER, DoorHingeSide.RIGHT, true, multiVariant4)
					.select(Direction.WEST, DoubleBlockHalf.LOWER, DoorHingeSide.RIGHT, true, multiVariant4.with(Y_ROT_90))
					.select(Direction.NORTH, DoubleBlockHalf.LOWER, DoorHingeSide.RIGHT, true, multiVariant4.with(Y_ROT_180))
					.select(Direction.EAST, DoubleBlockHalf.UPPER, DoorHingeSide.LEFT, false, multiVariant5)
					.select(Direction.SOUTH, DoubleBlockHalf.UPPER, DoorHingeSide.LEFT, false, multiVariant5.with(Y_ROT_90))
					.select(Direction.WEST, DoubleBlockHalf.UPPER, DoorHingeSide.LEFT, false, multiVariant5.with(Y_ROT_180))
					.select(Direction.NORTH, DoubleBlockHalf.UPPER, DoorHingeSide.LEFT, false, multiVariant5.with(Y_ROT_270))
					.select(Direction.EAST, DoubleBlockHalf.UPPER, DoorHingeSide.RIGHT, false, multiVariant7)
					.select(Direction.SOUTH, DoubleBlockHalf.UPPER, DoorHingeSide.RIGHT, false, multiVariant7.with(Y_ROT_90))
					.select(Direction.WEST, DoubleBlockHalf.UPPER, DoorHingeSide.RIGHT, false, multiVariant7.with(Y_ROT_180))
					.select(Direction.NORTH, DoubleBlockHalf.UPPER, DoorHingeSide.RIGHT, false, multiVariant7.with(Y_ROT_270))
					.select(Direction.EAST, DoubleBlockHalf.UPPER, DoorHingeSide.LEFT, true, multiVariant6.with(Y_ROT_90))
					.select(Direction.SOUTH, DoubleBlockHalf.UPPER, DoorHingeSide.LEFT, true, multiVariant6.with(Y_ROT_180))
					.select(Direction.WEST, DoubleBlockHalf.UPPER, DoorHingeSide.LEFT, true, multiVariant6.with(Y_ROT_270))
					.select(Direction.NORTH, DoubleBlockHalf.UPPER, DoorHingeSide.LEFT, true, multiVariant6)
					.select(Direction.EAST, DoubleBlockHalf.UPPER, DoorHingeSide.RIGHT, true, multiVariant8.with(Y_ROT_270))
					.select(Direction.SOUTH, DoubleBlockHalf.UPPER, DoorHingeSide.RIGHT, true, multiVariant8)
					.select(Direction.WEST, DoubleBlockHalf.UPPER, DoorHingeSide.RIGHT, true, multiVariant8.with(Y_ROT_90))
					.select(Direction.NORTH, DoubleBlockHalf.UPPER, DoorHingeSide.RIGHT, true, multiVariant8.with(Y_ROT_180))
			);
	}

	static BlockModelDefinitionGenerator createCustomFence(
		Block block, MultiVariant multiVariant, MultiVariant multiVariant2, MultiVariant multiVariant3, MultiVariant multiVariant4, MultiVariant multiVariant5
	) {
		return MultiPartGenerator.multiPart(block)
			.with(multiVariant)
			.with(condition().term(BlockStateProperties.NORTH, true), multiVariant2)
			.with(condition().term(BlockStateProperties.EAST, true), multiVariant3)
			.with(condition().term(BlockStateProperties.SOUTH, true), multiVariant4)
			.with(condition().term(BlockStateProperties.WEST, true), multiVariant5);
	}

	static BlockModelDefinitionGenerator createFence(Block block, MultiVariant multiVariant, MultiVariant multiVariant2) {
		return MultiPartGenerator.multiPart(block)
			.with(multiVariant)
			.with(condition().term(BlockStateProperties.NORTH, true), multiVariant2.with(UV_LOCK))
			.with(condition().term(BlockStateProperties.EAST, true), multiVariant2.with(Y_ROT_90).with(UV_LOCK))
			.with(condition().term(BlockStateProperties.SOUTH, true), multiVariant2.with(Y_ROT_180).with(UV_LOCK))
			.with(condition().term(BlockStateProperties.WEST, true), multiVariant2.with(Y_ROT_270).with(UV_LOCK));
	}

	static BlockModelDefinitionGenerator createWall(Block block, MultiVariant multiVariant, MultiVariant multiVariant2, MultiVariant multiVariant3) {
		return MultiPartGenerator.multiPart(block)
			.with(condition().term(BlockStateProperties.UP, true), multiVariant)
			.with(condition().term(BlockStateProperties.NORTH_WALL, WallSide.LOW), multiVariant2.with(UV_LOCK))
			.with(condition().term(BlockStateProperties.EAST_WALL, WallSide.LOW), multiVariant2.with(Y_ROT_90).with(UV_LOCK))
			.with(condition().term(BlockStateProperties.SOUTH_WALL, WallSide.LOW), multiVariant2.with(Y_ROT_180).with(UV_LOCK))
			.with(condition().term(BlockStateProperties.WEST_WALL, WallSide.LOW), multiVariant2.with(Y_ROT_270).with(UV_LOCK))
			.with(condition().term(BlockStateProperties.NORTH_WALL, WallSide.TALL), multiVariant3.with(UV_LOCK))
			.with(condition().term(BlockStateProperties.EAST_WALL, WallSide.TALL), multiVariant3.with(Y_ROT_90).with(UV_LOCK))
			.with(condition().term(BlockStateProperties.SOUTH_WALL, WallSide.TALL), multiVariant3.with(Y_ROT_180).with(UV_LOCK))
			.with(condition().term(BlockStateProperties.WEST_WALL, WallSide.TALL), multiVariant3.with(Y_ROT_270).with(UV_LOCK));
	}

	static BlockModelDefinitionGenerator createFenceGate(
		Block block, MultiVariant multiVariant, MultiVariant multiVariant2, MultiVariant multiVariant3, MultiVariant multiVariant4, boolean bl
	) {
		return MultiVariantGenerator.dispatch(block)
			.with(
				PropertyDispatch.initial(BlockStateProperties.IN_WALL, BlockStateProperties.OPEN)
					.select(false, false, multiVariant2)
					.select(true, false, multiVariant4)
					.select(false, true, multiVariant)
					.select(true, true, multiVariant3)
			)
			.with(bl ? UV_LOCK : NOP)
			.with(ROTATION_HORIZONTAL_FACING_ALT);
	}

	static BlockModelDefinitionGenerator createStairs(Block block, MultiVariant multiVariant, MultiVariant multiVariant2, MultiVariant multiVariant3) {
		return MultiVariantGenerator.dispatch(block)
			.with(
				PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
					.select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, multiVariant2)
					.select(Direction.WEST, Half.BOTTOM, StairsShape.STRAIGHT, multiVariant2.with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.SOUTH, Half.BOTTOM, StairsShape.STRAIGHT, multiVariant2.with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.NORTH, Half.BOTTOM, StairsShape.STRAIGHT, multiVariant2.with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, multiVariant3)
					.select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_RIGHT, multiVariant3.with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, multiVariant3.with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_RIGHT, multiVariant3.with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_LEFT, multiVariant3.with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.WEST, Half.BOTTOM, StairsShape.OUTER_LEFT, multiVariant3.with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, multiVariant3)
					.select(Direction.NORTH, Half.BOTTOM, StairsShape.OUTER_LEFT, multiVariant3.with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, multiVariant)
					.select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_RIGHT, multiVariant.with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_RIGHT, multiVariant.with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_RIGHT, multiVariant.with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_LEFT, multiVariant.with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.WEST, Half.BOTTOM, StairsShape.INNER_LEFT, multiVariant.with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, multiVariant)
					.select(Direction.NORTH, Half.BOTTOM, StairsShape.INNER_LEFT, multiVariant.with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.EAST, Half.TOP, StairsShape.STRAIGHT, multiVariant2.with(X_ROT_180).with(UV_LOCK))
					.select(Direction.WEST, Half.TOP, StairsShape.STRAIGHT, multiVariant2.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.SOUTH, Half.TOP, StairsShape.STRAIGHT, multiVariant2.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.NORTH, Half.TOP, StairsShape.STRAIGHT, multiVariant2.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.EAST, Half.TOP, StairsShape.OUTER_RIGHT, multiVariant3.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.WEST, Half.TOP, StairsShape.OUTER_RIGHT, multiVariant3.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_RIGHT, multiVariant3.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.NORTH, Half.TOP, StairsShape.OUTER_RIGHT, multiVariant3.with(X_ROT_180).with(UV_LOCK))
					.select(Direction.EAST, Half.TOP, StairsShape.OUTER_LEFT, multiVariant3.with(X_ROT_180).with(UV_LOCK))
					.select(Direction.WEST, Half.TOP, StairsShape.OUTER_LEFT, multiVariant3.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.SOUTH, Half.TOP, StairsShape.OUTER_LEFT, multiVariant3.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.NORTH, Half.TOP, StairsShape.OUTER_LEFT, multiVariant3.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.EAST, Half.TOP, StairsShape.INNER_RIGHT, multiVariant.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.WEST, Half.TOP, StairsShape.INNER_RIGHT, multiVariant.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
					.select(Direction.SOUTH, Half.TOP, StairsShape.INNER_RIGHT, multiVariant.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.NORTH, Half.TOP, StairsShape.INNER_RIGHT, multiVariant.with(X_ROT_180).with(UV_LOCK))
					.select(Direction.EAST, Half.TOP, StairsShape.INNER_LEFT, multiVariant.with(X_ROT_180).with(UV_LOCK))
					.select(Direction.WEST, Half.TOP, StairsShape.INNER_LEFT, multiVariant.with(X_ROT_180).with(Y_ROT_180).with(UV_LOCK))
					.select(Direction.SOUTH, Half.TOP, StairsShape.INNER_LEFT, multiVariant.with(X_ROT_180).with(Y_ROT_90).with(UV_LOCK))
					.select(Direction.NORTH, Half.TOP, StairsShape.INNER_LEFT, multiVariant.with(X_ROT_180).with(Y_ROT_270).with(UV_LOCK))
			);
	}

	private static BlockModelDefinitionGenerator createOrientableTrapdoor(
		Block block, MultiVariant multiVariant, MultiVariant multiVariant2, MultiVariant multiVariant3
	) {
		return MultiVariantGenerator.dispatch(block)
			.with(
				PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
					.select(Direction.NORTH, Half.BOTTOM, false, multiVariant2)
					.select(Direction.SOUTH, Half.BOTTOM, false, multiVariant2.with(Y_ROT_180))
					.select(Direction.EAST, Half.BOTTOM, false, multiVariant2.with(Y_ROT_90))
					.select(Direction.WEST, Half.BOTTOM, false, multiVariant2.with(Y_ROT_270))
					.select(Direction.NORTH, Half.TOP, false, multiVariant)
					.select(Direction.SOUTH, Half.TOP, false, multiVariant.with(Y_ROT_180))
					.select(Direction.EAST, Half.TOP, false, multiVariant.with(Y_ROT_90))
					.select(Direction.WEST, Half.TOP, false, multiVariant.with(Y_ROT_270))
					.select(Direction.NORTH, Half.BOTTOM, true, multiVariant3)
					.select(Direction.SOUTH, Half.BOTTOM, true, multiVariant3.with(Y_ROT_180))
					.select(Direction.EAST, Half.BOTTOM, true, multiVariant3.with(Y_ROT_90))
					.select(Direction.WEST, Half.BOTTOM, true, multiVariant3.with(Y_ROT_270))
					.select(Direction.NORTH, Half.TOP, true, multiVariant3.with(X_ROT_180).with(Y_ROT_180))
					.select(Direction.SOUTH, Half.TOP, true, multiVariant3.with(X_ROT_180))
					.select(Direction.EAST, Half.TOP, true, multiVariant3.with(X_ROT_180).with(Y_ROT_270))
					.select(Direction.WEST, Half.TOP, true, multiVariant3.with(X_ROT_180).with(Y_ROT_90))
			);
	}

	private static BlockModelDefinitionGenerator createTrapdoor(Block block, MultiVariant multiVariant, MultiVariant multiVariant2, MultiVariant multiVariant3) {
		return MultiVariantGenerator.dispatch(block)
			.with(
				PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
					.select(Direction.NORTH, Half.BOTTOM, false, multiVariant2)
					.select(Direction.SOUTH, Half.BOTTOM, false, multiVariant2)
					.select(Direction.EAST, Half.BOTTOM, false, multiVariant2)
					.select(Direction.WEST, Half.BOTTOM, false, multiVariant2)
					.select(Direction.NORTH, Half.TOP, false, multiVariant)
					.select(Direction.SOUTH, Half.TOP, false, multiVariant)
					.select(Direction.EAST, Half.TOP, false, multiVariant)
					.select(Direction.WEST, Half.TOP, false, multiVariant)
					.select(Direction.NORTH, Half.BOTTOM, true, multiVariant3)
					.select(Direction.SOUTH, Half.BOTTOM, true, multiVariant3.with(Y_ROT_180))
					.select(Direction.EAST, Half.BOTTOM, true, multiVariant3.with(Y_ROT_90))
					.select(Direction.WEST, Half.BOTTOM, true, multiVariant3.with(Y_ROT_270))
					.select(Direction.NORTH, Half.TOP, true, multiVariant3)
					.select(Direction.SOUTH, Half.TOP, true, multiVariant3.with(Y_ROT_180))
					.select(Direction.EAST, Half.TOP, true, multiVariant3.with(Y_ROT_90))
					.select(Direction.WEST, Half.TOP, true, multiVariant3.with(Y_ROT_270))
			);
	}

	static MultiVariantGenerator createSimpleBlock(Block block, MultiVariant multiVariant) {
		return MultiVariantGenerator.dispatch(block, multiVariant);
	}

	private static PropertyDispatch<VariantMutator> createRotatedPillar() {
		return PropertyDispatch.modify(BlockStateProperties.AXIS)
			.select(Direction.Axis.Y, NOP)
			.select(Direction.Axis.Z, X_ROT_90)
			.select(Direction.Axis.X, X_ROT_90.then(Y_ROT_90));
	}

	static BlockModelDefinitionGenerator createPillarBlockUVLocked(
		Block block, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer
	) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.CUBE_COLUMN_UV_LOCKED_X.create(block, textureMapping, biConsumer));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.CUBE_COLUMN_UV_LOCKED_Y.create(block, textureMapping, biConsumer));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.CUBE_COLUMN_UV_LOCKED_Z.create(block, textureMapping, biConsumer));
		return MultiVariantGenerator.dispatch(block)
			.with(
				PropertyDispatch.initial(BlockStateProperties.AXIS)
					.select(Direction.Axis.X, multiVariant)
					.select(Direction.Axis.Y, multiVariant2)
					.select(Direction.Axis.Z, multiVariant3)
			);
	}

	static BlockModelDefinitionGenerator createAxisAlignedPillarBlock(Block block, MultiVariant multiVariant) {
		return MultiVariantGenerator.dispatch(block, multiVariant).with(createRotatedPillar());
	}

	private void createAxisAlignedPillarBlockCustomModel(Block block, MultiVariant multiVariant) {
		this.blockStateOutput.accept(createAxisAlignedPillarBlock(block, multiVariant));
	}

	public void createAxisAlignedPillarBlock(Block block, TexturedModel.Provider provider) {
		MultiVariant multiVariant = plainVariant(provider.create(block, this.modelOutput));
		this.blockStateOutput.accept(createAxisAlignedPillarBlock(block, multiVariant));
	}

	private void createHorizontallyRotatedBlock(Block block, TexturedModel.Provider provider) {
		MultiVariant multiVariant = plainVariant(provider.create(block, this.modelOutput));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, multiVariant).with(ROTATION_HORIZONTAL_FACING));
	}

	static BlockModelDefinitionGenerator createRotatedPillarWithHorizontalVariant(Block block, MultiVariant multiVariant, MultiVariant multiVariant2) {
		return MultiVariantGenerator.dispatch(block)
			.with(
				PropertyDispatch.initial(BlockStateProperties.AXIS)
					.select(Direction.Axis.Y, multiVariant)
					.select(Direction.Axis.Z, multiVariant2.with(X_ROT_90))
					.select(Direction.Axis.X, multiVariant2.with(X_ROT_90).with(Y_ROT_90))
			);
	}

	private void createRotatedPillarWithHorizontalVariant(Block block, TexturedModel.Provider provider, TexturedModel.Provider provider2) {
		MultiVariant multiVariant = plainVariant(provider.create(block, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(provider2.create(block, this.modelOutput));
		this.blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(block, multiVariant, multiVariant2));
	}

	private void createCreakingHeart(Block block) {
		MultiVariant multiVariant = plainVariant(TexturedModel.COLUMN_ALT.create(block, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(TexturedModel.COLUMN_HORIZONTAL_ALT.create(block, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(this.createCreakingHeartModel(TexturedModel.COLUMN_ALT, block, "_awake"));
		MultiVariant multiVariant4 = plainVariant(this.createCreakingHeartModel(TexturedModel.COLUMN_HORIZONTAL_ALT, block, "_awake"));
		MultiVariant multiVariant5 = plainVariant(this.createCreakingHeartModel(TexturedModel.COLUMN_ALT, block, "_dormant"));
		MultiVariant multiVariant6 = plainVariant(this.createCreakingHeartModel(TexturedModel.COLUMN_HORIZONTAL_ALT, block, "_dormant"));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(
						PropertyDispatch.initial(BlockStateProperties.AXIS, CreakingHeartBlock.STATE)
							.select(Direction.Axis.Y, CreakingHeartState.UPROOTED, multiVariant)
							.select(Direction.Axis.Z, CreakingHeartState.UPROOTED, multiVariant2.with(X_ROT_90))
							.select(Direction.Axis.X, CreakingHeartState.UPROOTED, multiVariant2.with(X_ROT_90).with(Y_ROT_90))
							.select(Direction.Axis.Y, CreakingHeartState.DORMANT, multiVariant5)
							.select(Direction.Axis.Z, CreakingHeartState.DORMANT, multiVariant6.with(X_ROT_90))
							.select(Direction.Axis.X, CreakingHeartState.DORMANT, multiVariant6.with(X_ROT_90).with(Y_ROT_90))
							.select(Direction.Axis.Y, CreakingHeartState.AWAKE, multiVariant3)
							.select(Direction.Axis.Z, CreakingHeartState.AWAKE, multiVariant4.with(X_ROT_90))
							.select(Direction.Axis.X, CreakingHeartState.AWAKE, multiVariant4.with(X_ROT_90).with(Y_ROT_90))
					)
			);
	}

	private ResourceLocation createCreakingHeartModel(TexturedModel.Provider provider, Block block, String string) {
		return provider.updateTexture(
				textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, string))
					.put(TextureSlot.END, TextureMapping.getBlockTexture(block, "_top" + string))
			)
			.createWithSuffix(block, string, this.modelOutput);
	}

	private ResourceLocation createSuffixedVariant(Block block, String string, ModelTemplate modelTemplate, Function<ResourceLocation, TextureMapping> function) {
		return modelTemplate.createWithSuffix(block, string, (TextureMapping)function.apply(TextureMapping.getBlockTexture(block, string)), this.modelOutput);
	}

	static BlockModelDefinitionGenerator createPressurePlate(Block block, MultiVariant multiVariant, MultiVariant multiVariant2) {
		return MultiVariantGenerator.dispatch(block).with(createBooleanModelDispatch(BlockStateProperties.POWERED, multiVariant2, multiVariant));
	}

	static BlockModelDefinitionGenerator createSlab(Block block, MultiVariant multiVariant, MultiVariant multiVariant2, MultiVariant multiVariant3) {
		return MultiVariantGenerator.dispatch(block)
			.with(
				PropertyDispatch.initial(BlockStateProperties.SLAB_TYPE)
					.select(SlabType.BOTTOM, multiVariant)
					.select(SlabType.TOP, multiVariant2)
					.select(SlabType.DOUBLE, multiVariant3)
			);
	}

	public void createTrivialCube(Block block) {
		this.createTrivialBlock(block, TexturedModel.CUBE);
	}

	public void createTrivialBlock(Block block, TexturedModel.Provider provider) {
		this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(provider.create(block, this.modelOutput))));
	}

	public void createTintedLeaves(Block block, TexturedModel.Provider provider, int i) {
		ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(resourceLocation)));
		this.registerSimpleTintedItemModel(block, resourceLocation, ItemModelUtils.constantTint(i));
	}

	private void createVine() {
		this.createMultifaceBlockStates(Blocks.VINE);
		ResourceLocation resourceLocation = this.createFlatItemModelWithBlockTexture(Items.VINE, Blocks.VINE);
		this.registerSimpleTintedItemModel(Blocks.VINE, resourceLocation, ItemModelUtils.constantTint(-12012264));
	}

	private void createItemWithGrassTint(Block block) {
		ResourceLocation resourceLocation = this.createFlatItemModelWithBlockTexture(block.asItem(), block);
		this.registerSimpleTintedItemModel(block, resourceLocation, new GrassColorSource());
	}

	private BlockModelGenerators.BlockFamilyProvider family(Block block) {
		TexturedModel texturedModel = (TexturedModel)TEXTURED_MODELS.getOrDefault(block, TexturedModel.CUBE.get(block));
		return new BlockModelGenerators.BlockFamilyProvider(texturedModel.getMapping()).fullBlock(block, texturedModel.getTemplate());
	}

	public void createHangingSign(Block block, Block block2, Block block3) {
		MultiVariant multiVariant = this.createParticleOnlyBlockModel(block2, block);
		this.blockStateOutput.accept(createSimpleBlock(block2, multiVariant));
		this.blockStateOutput.accept(createSimpleBlock(block3, multiVariant));
		this.registerSimpleFlatItemModel(block2.asItem());
	}

	void createDoor(Block block) {
		TextureMapping textureMapping = TextureMapping.door(block);
		MultiVariant multiVariant = plainVariant(ModelTemplates.DOOR_BOTTOM_LEFT.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.DOOR_BOTTOM_RIGHT.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant4 = plainVariant(ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant5 = plainVariant(ModelTemplates.DOOR_TOP_LEFT.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant6 = plainVariant(ModelTemplates.DOOR_TOP_LEFT_OPEN.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant7 = plainVariant(ModelTemplates.DOOR_TOP_RIGHT.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant8 = plainVariant(ModelTemplates.DOOR_TOP_RIGHT_OPEN.create(block, textureMapping, this.modelOutput));
		this.registerSimpleFlatItemModel(block.asItem());
		this.blockStateOutput
			.accept(createDoor(block, multiVariant, multiVariant2, multiVariant3, multiVariant4, multiVariant5, multiVariant6, multiVariant7, multiVariant8));
	}

	private void copyDoorModel(Block block, Block block2) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.DOOR_BOTTOM_LEFT.getDefaultModelLocation(block));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.getDefaultModelLocation(block));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.DOOR_BOTTOM_RIGHT.getDefaultModelLocation(block));
		MultiVariant multiVariant4 = plainVariant(ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.getDefaultModelLocation(block));
		MultiVariant multiVariant5 = plainVariant(ModelTemplates.DOOR_TOP_LEFT.getDefaultModelLocation(block));
		MultiVariant multiVariant6 = plainVariant(ModelTemplates.DOOR_TOP_LEFT_OPEN.getDefaultModelLocation(block));
		MultiVariant multiVariant7 = plainVariant(ModelTemplates.DOOR_TOP_RIGHT.getDefaultModelLocation(block));
		MultiVariant multiVariant8 = plainVariant(ModelTemplates.DOOR_TOP_RIGHT_OPEN.getDefaultModelLocation(block));
		this.itemModelOutput.copy(block.asItem(), block2.asItem());
		this.blockStateOutput
			.accept(createDoor(block2, multiVariant, multiVariant2, multiVariant3, multiVariant4, multiVariant5, multiVariant6, multiVariant7, multiVariant8));
	}

	void createOrientableTrapdoor(Block block) {
		TextureMapping textureMapping = TextureMapping.defaultTexture(block);
		MultiVariant multiVariant = plainVariant(ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(block, textureMapping, this.modelOutput));
		ResourceLocation resourceLocation = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(block, textureMapping, this.modelOutput);
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(block, textureMapping, this.modelOutput));
		this.blockStateOutput.accept(createOrientableTrapdoor(block, multiVariant, plainVariant(resourceLocation), multiVariant2));
		this.registerSimpleItemModel(block, resourceLocation);
	}

	void createTrapdoor(Block block) {
		TextureMapping textureMapping = TextureMapping.defaultTexture(block);
		MultiVariant multiVariant = plainVariant(ModelTemplates.TRAPDOOR_TOP.create(block, textureMapping, this.modelOutput));
		ResourceLocation resourceLocation = ModelTemplates.TRAPDOOR_BOTTOM.create(block, textureMapping, this.modelOutput);
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.TRAPDOOR_OPEN.create(block, textureMapping, this.modelOutput));
		this.blockStateOutput.accept(createTrapdoor(block, multiVariant, plainVariant(resourceLocation), multiVariant2));
		this.registerSimpleItemModel(block, resourceLocation);
	}

	private void copyTrapdoorModel(Block block, Block block2) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.TRAPDOOR_TOP.getDefaultModelLocation(block));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.TRAPDOOR_BOTTOM.getDefaultModelLocation(block));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.TRAPDOOR_OPEN.getDefaultModelLocation(block));
		this.itemModelOutput.copy(block.asItem(), block2.asItem());
		this.blockStateOutput.accept(createTrapdoor(block2, multiVariant, multiVariant2, multiVariant3));
	}

	private void createBigDripLeafBlock() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_partial_tilt"));
		MultiVariant multiVariant3 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_full_tilt"));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.BIG_DRIPLEAF)
					.with(
						PropertyDispatch.initial(BlockStateProperties.TILT)
							.select(Tilt.NONE, multiVariant)
							.select(Tilt.UNSTABLE, multiVariant)
							.select(Tilt.PARTIAL, multiVariant2)
							.select(Tilt.FULL, multiVariant3)
					)
					.with(ROTATION_HORIZONTAL_FACING)
			);
	}

	private BlockModelGenerators.WoodProvider woodProvider(Block block) {
		return new BlockModelGenerators.WoodProvider(TextureMapping.logColumn(block));
	}

	private void createNonTemplateModelBlock(Block block) {
		this.createNonTemplateModelBlock(block, block);
	}

	private void createNonTemplateModelBlock(Block block, Block block2) {
		this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(ModelLocationUtils.getModelLocation(block2))));
	}

	private void createCrossBlockWithDefaultItem(Block block, BlockModelGenerators.PlantType plantType) {
		this.registerSimpleItemModel(block.asItem(), plantType.createItemModel(this, block));
		this.createCrossBlock(block, plantType);
	}

	private void createCrossBlockWithDefaultItem(Block block, BlockModelGenerators.PlantType plantType, TextureMapping textureMapping) {
		this.registerSimpleFlatItemModel(block);
		this.createCrossBlock(block, plantType, textureMapping);
	}

	private void createCrossBlock(Block block, BlockModelGenerators.PlantType plantType) {
		TextureMapping textureMapping = plantType.getTextureMapping(block);
		this.createCrossBlock(block, plantType, textureMapping);
	}

	private void createCrossBlock(Block block, BlockModelGenerators.PlantType plantType, TextureMapping textureMapping) {
		MultiVariant multiVariant = plainVariant(plantType.getCross().create(block, textureMapping, this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
	}

	private void createCrossBlock(Block block, BlockModelGenerators.PlantType plantType, Property<Integer> property, int... is) {
		if (property.getPossibleValues().size() != is.length) {
			throw new IllegalArgumentException("missing values for property: " + property);
		} else {
			this.registerSimpleFlatItemModel(block.asItem());
			this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(property).generate(integer -> {
				String string = "_stage" + is[integer];
				TextureMapping textureMapping = TextureMapping.cross(TextureMapping.getBlockTexture(block, string));
				return plainVariant(plantType.getCross().createWithSuffix(block, string, textureMapping, this.modelOutput));
			})));
		}
	}

	private void createPlantWithDefaultItem(Block block, Block block2, BlockModelGenerators.PlantType plantType) {
		this.registerSimpleItemModel(block.asItem(), plantType.createItemModel(this, block));
		this.createPlant(block, block2, plantType);
	}

	private void createPlant(Block block, Block block2, BlockModelGenerators.PlantType plantType) {
		this.createCrossBlock(block, plantType);
		TextureMapping textureMapping = plantType.getPlantTextureMapping(block);
		MultiVariant multiVariant = plainVariant(plantType.getCrossPot().create(block2, textureMapping, this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block2, multiVariant));
	}

	private void createCoralFans(Block block, Block block2) {
		TexturedModel texturedModel = TexturedModel.CORAL_FAN.get(block);
		MultiVariant multiVariant = plainVariant(texturedModel.create(block, this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.CORAL_WALL_FAN.create(block2, texturedModel.getMapping(), this.modelOutput));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block2, multiVariant2).with(ROTATION_HORIZONTAL_FACING));
		this.registerSimpleFlatItemModel(block);
	}

	private void createStems(Block block, Block block2) {
		this.registerSimpleFlatItemModel(block.asItem());
		TextureMapping textureMapping = TextureMapping.stem(block);
		TextureMapping textureMapping2 = TextureMapping.attachedStem(block, block2);
		MultiVariant multiVariant = plainVariant(ModelTemplates.ATTACHED_STEM.create(block2, textureMapping2, this.modelOutput));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block2, multiVariant)
					.with(
						PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
							.select(Direction.WEST, NOP)
							.select(Direction.SOUTH, Y_ROT_270)
							.select(Direction.NORTH, Y_ROT_90)
							.select(Direction.EAST, Y_ROT_180)
					)
			);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(
						PropertyDispatch.initial(BlockStateProperties.AGE_7)
							.generate(integer -> plainVariant(ModelTemplates.STEMS[integer].create(block, textureMapping, this.modelOutput)))
					)
			);
	}

	private void createPitcherPlant() {
		Block block = Blocks.PITCHER_PLANT;
		this.registerSimpleFlatItemModel(block.asItem());
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(block, "_top"));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(block, "_bottom"));
		this.createDoubleBlock(block, multiVariant, multiVariant2);
	}

	private void createPitcherCrop() {
		Block block = Blocks.PITCHER_CROP;
		this.registerSimpleFlatItemModel(block.asItem());
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(PropertyDispatch.initial(PitcherCropBlock.AGE, BlockStateProperties.DOUBLE_BLOCK_HALF).generate((integer, doubleBlockHalf) -> {
						return switch (doubleBlockHalf) {
							case UPPER -> plainVariant(ModelLocationUtils.getModelLocation(block, "_top_stage_" + integer));
							case LOWER -> plainVariant(ModelLocationUtils.getModelLocation(block, "_bottom_stage_" + integer));
						};
					}))
			);
	}

	private void createCoral(Block block, Block block2, Block block3, Block block4, Block block5, Block block6, Block block7, Block block8) {
		this.createCrossBlockWithDefaultItem(block, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createCrossBlockWithDefaultItem(block2, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTrivialCube(block3);
		this.createTrivialCube(block4);
		this.createCoralFans(block5, block7);
		this.createCoralFans(block6, block8);
	}

	private void createDoublePlant(Block block, BlockModelGenerators.PlantType plantType) {
		MultiVariant multiVariant = plainVariant(this.createSuffixedVariant(block, "_top", plantType.getCross(), TextureMapping::cross));
		MultiVariant multiVariant2 = plainVariant(this.createSuffixedVariant(block, "_bottom", plantType.getCross(), TextureMapping::cross));
		this.createDoubleBlock(block, multiVariant, multiVariant2);
	}

	private void createDoublePlantWithDefaultItem(Block block, BlockModelGenerators.PlantType plantType) {
		this.registerSimpleFlatItemModel(block, "_top");
		this.createDoublePlant(block, plantType);
	}

	private void createTintedDoublePlant(Block block) {
		ResourceLocation resourceLocation = this.createFlatItemModelWithBlockTexture(block.asItem(), block, "_top");
		this.registerSimpleTintedItemModel(block, resourceLocation, new GrassColorSource());
		this.createDoublePlant(block, BlockModelGenerators.PlantType.TINTED);
	}

	private void createSunflower() {
		this.registerSimpleFlatItemModel(Blocks.SUNFLOWER, "_front");
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.SUNFLOWER, "_top"));
		MultiVariant multiVariant2 = plainVariant(
			this.createSuffixedVariant(Blocks.SUNFLOWER, "_bottom", BlockModelGenerators.PlantType.NOT_TINTED.getCross(), TextureMapping::cross)
		);
		this.createDoubleBlock(Blocks.SUNFLOWER, multiVariant, multiVariant2);
	}

	private void createTallSeagrass() {
		MultiVariant multiVariant = plainVariant(this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_top", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture));
		MultiVariant multiVariant2 = plainVariant(
			this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_bottom", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture)
		);
		this.createDoubleBlock(Blocks.TALL_SEAGRASS, multiVariant, multiVariant2);
	}

	private void createSmallDripleaf() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_top"));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_bottom"));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.SMALL_DRIPLEAF)
					.with(
						PropertyDispatch.initial(BlockStateProperties.DOUBLE_BLOCK_HALF).select(DoubleBlockHalf.LOWER, multiVariant2).select(DoubleBlockHalf.UPPER, multiVariant)
					)
					.with(ROTATION_HORIZONTAL_FACING)
			);
	}

	private void createDoubleBlock(Block block, MultiVariant multiVariant, MultiVariant multiVariant2) {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(
						PropertyDispatch.initial(BlockStateProperties.DOUBLE_BLOCK_HALF).select(DoubleBlockHalf.LOWER, multiVariant2).select(DoubleBlockHalf.UPPER, multiVariant)
					)
			);
	}

	private void createPassiveRail(Block block) {
		TextureMapping textureMapping = TextureMapping.rail(block);
		TextureMapping textureMapping2 = TextureMapping.rail(TextureMapping.getBlockTexture(block, "_corner"));
		MultiVariant multiVariant = plainVariant(ModelTemplates.RAIL_FLAT.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.RAIL_CURVED.create(block, textureMapping2, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.RAIL_RAISED_NE.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant4 = plainVariant(ModelTemplates.RAIL_RAISED_SW.create(block, textureMapping, this.modelOutput));
		this.registerSimpleFlatItemModel(block);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(
						PropertyDispatch.initial(BlockStateProperties.RAIL_SHAPE)
							.select(RailShape.NORTH_SOUTH, multiVariant)
							.select(RailShape.EAST_WEST, multiVariant.with(Y_ROT_90))
							.select(RailShape.ASCENDING_EAST, multiVariant3.with(Y_ROT_90))
							.select(RailShape.ASCENDING_WEST, multiVariant4.with(Y_ROT_90))
							.select(RailShape.ASCENDING_NORTH, multiVariant3)
							.select(RailShape.ASCENDING_SOUTH, multiVariant4)
							.select(RailShape.SOUTH_EAST, multiVariant2)
							.select(RailShape.SOUTH_WEST, multiVariant2.with(Y_ROT_90))
							.select(RailShape.NORTH_WEST, multiVariant2.with(Y_ROT_180))
							.select(RailShape.NORTH_EAST, multiVariant2.with(Y_ROT_270))
					)
			);
	}

	private void createActiveRail(Block block) {
		MultiVariant multiVariant = plainVariant(this.createSuffixedVariant(block, "", ModelTemplates.RAIL_FLAT, TextureMapping::rail));
		MultiVariant multiVariant2 = plainVariant(this.createSuffixedVariant(block, "", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail));
		MultiVariant multiVariant3 = plainVariant(this.createSuffixedVariant(block, "", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail));
		MultiVariant multiVariant4 = plainVariant(this.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_FLAT, TextureMapping::rail));
		MultiVariant multiVariant5 = plainVariant(this.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail));
		MultiVariant multiVariant6 = plainVariant(this.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail));
		this.registerSimpleFlatItemModel(block);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(PropertyDispatch.initial(BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT).generate((boolean_, railShape) -> {
						return switch (railShape) {
							case NORTH_SOUTH -> boolean_ ? multiVariant4 : multiVariant;
							case EAST_WEST -> (boolean_ ? multiVariant4 : multiVariant).with(Y_ROT_90);
							case ASCENDING_EAST -> (boolean_ ? multiVariant5 : multiVariant2).with(Y_ROT_90);
							case ASCENDING_WEST -> (boolean_ ? multiVariant6 : multiVariant3).with(Y_ROT_90);
							case ASCENDING_NORTH -> boolean_ ? multiVariant5 : multiVariant2;
							case ASCENDING_SOUTH -> boolean_ ? multiVariant6 : multiVariant3;
							default -> throw new UnsupportedOperationException("Fix you generator!");
						};
					}))
			);
	}

	private void createAirLikeBlock(Block block, Item item) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particleFromItem(item), this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
	}

	private void createAirLikeBlock(Block block, ResourceLocation resourceLocation) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particle(resourceLocation), this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
	}

	private MultiVariant createParticleOnlyBlockModel(Block block, Block block2) {
		return plainVariant(ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particle(block2), this.modelOutput));
	}

	public void createParticleOnlyBlock(Block block, Block block2) {
		this.blockStateOutput.accept(createSimpleBlock(block, this.createParticleOnlyBlockModel(block, block2)));
	}

	private void createParticleOnlyBlock(Block block) {
		this.createParticleOnlyBlock(block, block);
	}

	private void createFullAndCarpetBlocks(Block block, Block block2) {
		this.createTrivialCube(block);
		MultiVariant multiVariant = plainVariant(TexturedModel.CARPET.get(block).create(block2, this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block2, multiVariant));
	}

	private void createLeafLitter(Block block) {
		MultiVariant multiVariant = plainVariant(TexturedModel.LEAF_LITTER_1.create(block, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(TexturedModel.LEAF_LITTER_2.create(block, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(TexturedModel.LEAF_LITTER_3.create(block, this.modelOutput));
		MultiVariant multiVariant4 = plainVariant(TexturedModel.LEAF_LITTER_4.create(block, this.modelOutput));
		this.registerSimpleFlatItemModel(block.asItem());
		this.createSegmentedBlock(
			block,
			multiVariant,
			LEAF_LITTER_MODEL_1_SEGMENT_CONDITION,
			multiVariant2,
			LEAF_LITTER_MODEL_2_SEGMENT_CONDITION,
			multiVariant3,
			LEAF_LITTER_MODEL_3_SEGMENT_CONDITION,
			multiVariant4,
			LEAF_LITTER_MODEL_4_SEGMENT_CONDITION
		);
	}

	private void createFlowerBed(Block block) {
		MultiVariant multiVariant = plainVariant(TexturedModel.FLOWERBED_1.create(block, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(TexturedModel.FLOWERBED_2.create(block, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(TexturedModel.FLOWERBED_3.create(block, this.modelOutput));
		MultiVariant multiVariant4 = plainVariant(TexturedModel.FLOWERBED_4.create(block, this.modelOutput));
		this.registerSimpleFlatItemModel(block.asItem());
		this.createSegmentedBlock(
			block,
			multiVariant,
			FLOWER_BED_MODEL_1_SEGMENT_CONDITION,
			multiVariant2,
			FLOWER_BED_MODEL_2_SEGMENT_CONDITION,
			multiVariant3,
			FLOWER_BED_MODEL_3_SEGMENT_CONDITION,
			multiVariant4,
			FLOWER_BED_MODEL_4_SEGMENT_CONDITION
		);
	}

	private void createSegmentedBlock(
		Block block,
		MultiVariant multiVariant,
		Function<ConditionBuilder, ConditionBuilder> function,
		MultiVariant multiVariant2,
		Function<ConditionBuilder, ConditionBuilder> function2,
		MultiVariant multiVariant3,
		Function<ConditionBuilder, ConditionBuilder> function3,
		MultiVariant multiVariant4,
		Function<ConditionBuilder, ConditionBuilder> function4
	) {
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(block)
					.with((ConditionBuilder)function.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)), multiVariant)
					.with((ConditionBuilder)function.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)), multiVariant.with(Y_ROT_90))
					.with((ConditionBuilder)function.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)), multiVariant.with(Y_ROT_180))
					.with((ConditionBuilder)function.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)), multiVariant.with(Y_ROT_270))
					.with((ConditionBuilder)function2.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)), multiVariant2)
					.with((ConditionBuilder)function2.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)), multiVariant2.with(Y_ROT_90))
					.with((ConditionBuilder)function2.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)), multiVariant2.with(Y_ROT_180))
					.with((ConditionBuilder)function2.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)), multiVariant2.with(Y_ROT_270))
					.with((ConditionBuilder)function3.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)), multiVariant3)
					.with((ConditionBuilder)function3.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)), multiVariant3.with(Y_ROT_90))
					.with((ConditionBuilder)function3.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)), multiVariant3.with(Y_ROT_180))
					.with((ConditionBuilder)function3.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)), multiVariant3.with(Y_ROT_270))
					.with((ConditionBuilder)function4.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)), multiVariant4)
					.with((ConditionBuilder)function4.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)), multiVariant4.with(Y_ROT_90))
					.with((ConditionBuilder)function4.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)), multiVariant4.with(Y_ROT_180))
					.with((ConditionBuilder)function4.apply(condition().term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)), multiVariant4.with(Y_ROT_270))
			);
	}

	private void createColoredBlockWithRandomRotations(TexturedModel.Provider provider, Block... blocks) {
		for (Block block : blocks) {
			Variant variant = plainModel(provider.create(block, this.modelOutput));
			this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, createRotatedVariants(variant)));
		}
	}

	private void createColoredBlockWithStateRotations(TexturedModel.Provider provider, Block... blocks) {
		for (Block block : blocks) {
			MultiVariant multiVariant = plainVariant(provider.create(block, this.modelOutput));
			this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, multiVariant).with(ROTATION_HORIZONTAL_FACING_ALT));
		}
	}

	private void createGlassBlocks(Block block, Block block2) {
		this.createTrivialCube(block);
		TextureMapping textureMapping = TextureMapping.pane(block, block2);
		MultiVariant multiVariant = plainVariant(ModelTemplates.STAINED_GLASS_PANE_POST.create(block2, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.STAINED_GLASS_PANE_SIDE.create(block2, textureMapping, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(block2, textureMapping, this.modelOutput));
		MultiVariant multiVariant4 = plainVariant(ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(block2, textureMapping, this.modelOutput));
		MultiVariant multiVariant5 = plainVariant(ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(block2, textureMapping, this.modelOutput));
		Item item = block2.asItem();
		this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, block));
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(block2)
					.with(multiVariant)
					.with(condition().term(BlockStateProperties.NORTH, true), multiVariant2)
					.with(condition().term(BlockStateProperties.EAST, true), multiVariant2.with(Y_ROT_90))
					.with(condition().term(BlockStateProperties.SOUTH, true), multiVariant3)
					.with(condition().term(BlockStateProperties.WEST, true), multiVariant3.with(Y_ROT_90))
					.with(condition().term(BlockStateProperties.NORTH, false), multiVariant4)
					.with(condition().term(BlockStateProperties.EAST, false), multiVariant5)
					.with(condition().term(BlockStateProperties.SOUTH, false), multiVariant5.with(Y_ROT_90))
					.with(condition().term(BlockStateProperties.WEST, false), multiVariant4.with(Y_ROT_270))
			);
	}

	private void createCommandBlock(Block block) {
		TextureMapping textureMapping = TextureMapping.commandBlock(block);
		MultiVariant multiVariant = plainVariant(ModelTemplates.COMMAND_BLOCK.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(
			this.createSuffixedVariant(
				block, "_conditional", ModelTemplates.COMMAND_BLOCK, resourceLocation -> textureMapping.copyAndUpdate(TextureSlot.SIDE, resourceLocation)
			)
		);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block).with(createBooleanModelDispatch(BlockStateProperties.CONDITIONAL, multiVariant2, multiVariant)).with(ROTATION_FACING)
			);
	}

	private void createAnvil(Block block) {
		MultiVariant multiVariant = plainVariant(TexturedModel.ANVIL.create(block, this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant).with(ROTATION_HORIZONTAL_FACING_ALT));
	}

	private static MultiVariant createBambooModels(int i) {
		String string = "_age" + i;
		return new MultiVariant(
			WeightedList.of(
				(List<Weighted<Variant>>)IntStream.range(1, 5)
					.mapToObj(ix -> new Weighted<>(plainModel(ModelLocationUtils.getModelLocation(Blocks.BAMBOO, ix + string)), 1))
					.collect(Collectors.toList())
			)
		);
	}

	private void createBamboo() {
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.BAMBOO)
					.with(condition().term(BlockStateProperties.AGE_1, 0), createBambooModels(0))
					.with(condition().term(BlockStateProperties.AGE_1, 1), createBambooModels(1))
					.with(
						condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.SMALL),
						plainVariant(ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_small_leaves"))
					)
					.with(
						condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.LARGE),
						plainVariant(ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_large_leaves"))
					)
			);
	}

	private void createBarrel() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.BARREL, "_top_open");
		MultiVariant multiVariant = plainVariant(TexturedModel.CUBE_TOP_BOTTOM.create(Blocks.BARREL, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(
			TexturedModel.CUBE_TOP_BOTTOM
				.get(Blocks.BARREL)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.TOP, resourceLocation))
				.createWithSuffix(Blocks.BARREL, "_open", this.modelOutput)
		);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.BARREL)
					.with(PropertyDispatch.initial(BlockStateProperties.OPEN).select(false, multiVariant).select(true, multiVariant2))
					.with(ROTATIONS_COLUMN_WITH_FACING)
			);
	}

	private static <T extends Comparable<T>> PropertyDispatch<MultiVariant> createEmptyOrFullDispatch(
		Property<T> property, T comparable, MultiVariant multiVariant, MultiVariant multiVariant2
	) {
		return PropertyDispatch.initial(property).generate(comparable2 -> {
			boolean bl = comparable2.compareTo(comparable) >= 0;
			return bl ? multiVariant : multiVariant2;
		});
	}

	private void createBeeNest(Block block, Function<Block, TextureMapping> function) {
		TextureMapping textureMapping = ((TextureMapping)function.apply(block)).copyForced(TextureSlot.SIDE, TextureSlot.PARTICLE);
		TextureMapping textureMapping2 = textureMapping.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front_honey"));
		ResourceLocation resourceLocation = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(block, "_empty", textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(block, "_honey", textureMapping2, this.modelOutput);
		this.itemModelOutput
			.accept(
				block.asItem(),
				ItemModelUtils.selectBlockItemProperty(
					BeehiveBlock.HONEY_LEVEL, ItemModelUtils.plainModel(resourceLocation), Map.of(5, ItemModelUtils.plainModel(resourceLocation2))
				)
			);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(createEmptyOrFullDispatch(BeehiveBlock.HONEY_LEVEL, 5, plainVariant(resourceLocation2), plainVariant(resourceLocation)))
					.with(ROTATION_HORIZONTAL_FACING)
			);
	}

	private void createCropBlock(Block block, Property<Integer> property, int... is) {
		this.registerSimpleFlatItemModel(block.asItem());
		if (property.getPossibleValues().size() != is.length) {
			throw new IllegalArgumentException();
		} else {
			Int2ObjectMap<ResourceLocation> int2ObjectMap = new Int2ObjectOpenHashMap<>();
			this.blockStateOutput
				.accept(
					MultiVariantGenerator.dispatch(block)
						.with(
							PropertyDispatch.initial(property)
								.generate(
									integer -> {
										int i = is[integer];
										return plainVariant(
											int2ObjectMap.computeIfAbsent(
												i,
												(Int2ObjectFunction<? extends ResourceLocation>)(ix -> this.createSuffixedVariant(block, "_stage" + ix, ModelTemplates.CROP, TextureMapping::crop))
											)
										);
									}
								)
						)
				);
		}
	}

	private void createBell() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.BELL, "_floor"));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.BELL, "_ceiling"));
		MultiVariant multiVariant3 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.BELL, "_wall"));
		MultiVariant multiVariant4 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.BELL, "_between_walls"));
		this.registerSimpleFlatItemModel(Items.BELL);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.BELL)
					.with(
						PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.BELL_ATTACHMENT)
							.select(Direction.NORTH, BellAttachType.FLOOR, multiVariant)
							.select(Direction.SOUTH, BellAttachType.FLOOR, multiVariant.with(Y_ROT_180))
							.select(Direction.EAST, BellAttachType.FLOOR, multiVariant.with(Y_ROT_90))
							.select(Direction.WEST, BellAttachType.FLOOR, multiVariant.with(Y_ROT_270))
							.select(Direction.NORTH, BellAttachType.CEILING, multiVariant2)
							.select(Direction.SOUTH, BellAttachType.CEILING, multiVariant2.with(Y_ROT_180))
							.select(Direction.EAST, BellAttachType.CEILING, multiVariant2.with(Y_ROT_90))
							.select(Direction.WEST, BellAttachType.CEILING, multiVariant2.with(Y_ROT_270))
							.select(Direction.NORTH, BellAttachType.SINGLE_WALL, multiVariant3.with(Y_ROT_270))
							.select(Direction.SOUTH, BellAttachType.SINGLE_WALL, multiVariant3.with(Y_ROT_90))
							.select(Direction.EAST, BellAttachType.SINGLE_WALL, multiVariant3)
							.select(Direction.WEST, BellAttachType.SINGLE_WALL, multiVariant3.with(Y_ROT_180))
							.select(Direction.SOUTH, BellAttachType.DOUBLE_WALL, multiVariant4.with(Y_ROT_90))
							.select(Direction.NORTH, BellAttachType.DOUBLE_WALL, multiVariant4.with(Y_ROT_270))
							.select(Direction.EAST, BellAttachType.DOUBLE_WALL, multiVariant4)
							.select(Direction.WEST, BellAttachType.DOUBLE_WALL, multiVariant4.with(Y_ROT_180))
					)
			);
	}

	private void createGrindstone() {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.GRINDSTONE, plainVariant(ModelLocationUtils.getModelLocation(Blocks.GRINDSTONE)))
					.with(
						PropertyDispatch.modify(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
							.select(AttachFace.FLOOR, Direction.NORTH, NOP)
							.select(AttachFace.FLOOR, Direction.EAST, Y_ROT_90)
							.select(AttachFace.FLOOR, Direction.SOUTH, Y_ROT_180)
							.select(AttachFace.FLOOR, Direction.WEST, Y_ROT_270)
							.select(AttachFace.WALL, Direction.NORTH, X_ROT_90)
							.select(AttachFace.WALL, Direction.EAST, X_ROT_90.then(Y_ROT_90))
							.select(AttachFace.WALL, Direction.SOUTH, X_ROT_90.then(Y_ROT_180))
							.select(AttachFace.WALL, Direction.WEST, X_ROT_90.then(Y_ROT_270))
							.select(AttachFace.CEILING, Direction.SOUTH, X_ROT_180)
							.select(AttachFace.CEILING, Direction.WEST, X_ROT_180.then(Y_ROT_90))
							.select(AttachFace.CEILING, Direction.NORTH, X_ROT_180.then(Y_ROT_180))
							.select(AttachFace.CEILING, Direction.EAST, X_ROT_180.then(Y_ROT_270))
					)
			);
	}

	private void createFurnace(Block block, TexturedModel.Provider provider) {
		MultiVariant multiVariant = plainVariant(provider.create(block, this.modelOutput));
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(block, "_front_on");
		MultiVariant multiVariant2 = plainVariant(
			provider.get(block)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.FRONT, resourceLocation))
				.createWithSuffix(block, "_on", this.modelOutput)
		);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(createBooleanModelDispatch(BlockStateProperties.LIT, multiVariant2, multiVariant))
					.with(ROTATION_HORIZONTAL_FACING)
			);
	}

	private void createCampfires(Block... blocks) {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.decorateBlockModelLocation("campfire_off"));

		for (Block block : blocks) {
			MultiVariant multiVariant2 = plainVariant(ModelTemplates.CAMPFIRE.create(block, TextureMapping.campfire(block), this.modelOutput));
			this.registerSimpleFlatItemModel(block.asItem());
			this.blockStateOutput
				.accept(
					MultiVariantGenerator.dispatch(block)
						.with(createBooleanModelDispatch(BlockStateProperties.LIT, multiVariant2, multiVariant))
						.with(ROTATION_HORIZONTAL_FACING_ALT)
				);
		}
	}

	private void createAzalea(Block block) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.AZALEA.create(block, TextureMapping.cubeTop(block), this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
	}

	private void createPottedAzalea(Block block) {
		MultiVariant multiVariant;
		if (block == Blocks.POTTED_FLOWERING_AZALEA) {
			multiVariant = plainVariant(ModelTemplates.POTTED_FLOWERING_AZALEA.create(block, TextureMapping.pottedAzalea(block), this.modelOutput));
		} else {
			multiVariant = plainVariant(ModelTemplates.POTTED_AZALEA.create(block, TextureMapping.pottedAzalea(block), this.modelOutput));
		}

		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
	}

	private void createBookshelf() {
		TextureMapping textureMapping = TextureMapping.column(TextureMapping.getBlockTexture(Blocks.BOOKSHELF), TextureMapping.getBlockTexture(Blocks.OAK_PLANKS));
		MultiVariant multiVariant = plainVariant(ModelTemplates.CUBE_COLUMN.create(Blocks.BOOKSHELF, textureMapping, this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(Blocks.BOOKSHELF, multiVariant));
	}

	private void createRedstoneWire() {
		this.registerSimpleFlatItemModel(Items.REDSTONE);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.REDSTONE_WIRE)
					.with(
						or(
							condition()
								.term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
								.term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
								.term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE)
								.term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE),
							condition()
								.term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
								.term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
							condition()
								.term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
								.term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
							condition()
								.term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
								.term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
							condition()
								.term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
								.term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
						),
						plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_dot"))
					)
					.with(
						condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
						plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side0"))
					)
					.with(
						condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
						plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt0"))
					)
					.with(
						condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
						plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt1")).with(Y_ROT_270)
					)
					.with(
						condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
						plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side1")).with(Y_ROT_270)
					)
					.with(
						condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.UP), plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
					)
					.with(
						condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.UP),
						plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up")).with(Y_ROT_90)
					)
					.with(
						condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.UP),
						plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up")).with(Y_ROT_180)
					)
					.with(
						condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.UP),
						plainVariant(ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up")).with(Y_ROT_270)
					)
			);
	}

	private void createComparator() {
		this.registerSimpleFlatItemModel(Items.COMPARATOR);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.COMPARATOR)
					.with(
						PropertyDispatch.initial(BlockStateProperties.MODE_COMPARATOR, BlockStateProperties.POWERED)
							.select(ComparatorMode.COMPARE, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.COMPARATOR)))
							.select(ComparatorMode.COMPARE, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on")))
							.select(ComparatorMode.SUBTRACT, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_subtract")))
							.select(ComparatorMode.SUBTRACT, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on_subtract")))
					)
					.with(ROTATION_HORIZONTAL_FACING_ALT)
			);
	}

	private void createSmoothStoneSlab() {
		TextureMapping textureMapping = TextureMapping.cube(Blocks.SMOOTH_STONE);
		TextureMapping textureMapping2 = TextureMapping.column(TextureMapping.getBlockTexture(Blocks.SMOOTH_STONE_SLAB, "_side"), textureMapping.get(TextureSlot.TOP));
		MultiVariant multiVariant = plainVariant(ModelTemplates.SLAB_BOTTOM.create(Blocks.SMOOTH_STONE_SLAB, textureMapping2, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.SLAB_TOP.create(Blocks.SMOOTH_STONE_SLAB, textureMapping2, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(
			ModelTemplates.CUBE_COLUMN.createWithOverride(Blocks.SMOOTH_STONE_SLAB, "_double", textureMapping2, this.modelOutput)
		);
		this.blockStateOutput.accept(createSlab(Blocks.SMOOTH_STONE_SLAB, multiVariant, multiVariant2, multiVariant3));
		this.blockStateOutput
			.accept(createSimpleBlock(Blocks.SMOOTH_STONE, plainVariant(ModelTemplates.CUBE_ALL.create(Blocks.SMOOTH_STONE, textureMapping, this.modelOutput))));
	}

	private void createBrewingStand() {
		this.registerSimpleFlatItemModel(Items.BREWING_STAND);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.BREWING_STAND)
					.with(plainVariant(TextureMapping.getBlockTexture(Blocks.BREWING_STAND)))
					.with(condition().term(BlockStateProperties.HAS_BOTTLE_0, true), plainVariant(TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle0")))
					.with(condition().term(BlockStateProperties.HAS_BOTTLE_1, true), plainVariant(TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle1")))
					.with(condition().term(BlockStateProperties.HAS_BOTTLE_2, true), plainVariant(TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle2")))
					.with(condition().term(BlockStateProperties.HAS_BOTTLE_0, false), plainVariant(TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty0")))
					.with(condition().term(BlockStateProperties.HAS_BOTTLE_1, false), plainVariant(TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty1")))
					.with(condition().term(BlockStateProperties.HAS_BOTTLE_2, false), plainVariant(TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty2")))
			);
	}

	private void createMushroomBlock(Block block) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.SINGLE_FACE.create(block, TextureMapping.defaultTexture(block), this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.decorateBlockModelLocation("mushroom_block_inside"));
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(block)
					.with(condition().term(BlockStateProperties.NORTH, true), multiVariant)
					.with(condition().term(BlockStateProperties.EAST, true), multiVariant.with(Y_ROT_90).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.SOUTH, true), multiVariant.with(Y_ROT_180).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.WEST, true), multiVariant.with(Y_ROT_270).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.UP, true), multiVariant.with(X_ROT_270).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.DOWN, true), multiVariant.with(X_ROT_90).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.NORTH, false), multiVariant2)
					.with(condition().term(BlockStateProperties.EAST, false), multiVariant2.with(Y_ROT_90))
					.with(condition().term(BlockStateProperties.SOUTH, false), multiVariant2.with(Y_ROT_180))
					.with(condition().term(BlockStateProperties.WEST, false), multiVariant2.with(Y_ROT_270))
					.with(condition().term(BlockStateProperties.UP, false), multiVariant2.with(X_ROT_270))
					.with(condition().term(BlockStateProperties.DOWN, false), multiVariant2.with(X_ROT_90))
			);
		this.registerSimpleItemModel(block, TexturedModel.CUBE.createWithSuffix(block, "_inventory", this.modelOutput));
	}

	private void createCakeBlock() {
		this.registerSimpleFlatItemModel(Items.CAKE);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.CAKE)
					.with(
						PropertyDispatch.initial(BlockStateProperties.BITES)
							.select(0, plainVariant(ModelLocationUtils.getModelLocation(Blocks.CAKE)))
							.select(1, plainVariant(ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice1")))
							.select(2, plainVariant(ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice2")))
							.select(3, plainVariant(ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice3")))
							.select(4, plainVariant(ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice4")))
							.select(5, plainVariant(ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice5")))
							.select(6, plainVariant(ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice6")))
					)
			);
	}

	private void createCartographyTable() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
			.put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.DARK_OAK_PLANKS))
			.put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_top"))
			.put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
			.put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
			.put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side1"))
			.put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side2"));
		this.blockStateOutput
			.accept(createSimpleBlock(Blocks.CARTOGRAPHY_TABLE, plainVariant(ModelTemplates.CUBE.create(Blocks.CARTOGRAPHY_TABLE, textureMapping, this.modelOutput))));
	}

	private void createSmithingTable() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
			.put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_bottom"))
			.put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_top"))
			.put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
			.put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
			.put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"))
			.put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"));
		this.blockStateOutput
			.accept(createSimpleBlock(Blocks.SMITHING_TABLE, plainVariant(ModelTemplates.CUBE.create(Blocks.SMITHING_TABLE, textureMapping, this.modelOutput))));
	}

	private void createCraftingTableLike(Block block, Block block2, BiFunction<Block, Block, TextureMapping> biFunction) {
		TextureMapping textureMapping = (TextureMapping)biFunction.apply(block, block2);
		this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(ModelTemplates.CUBE.create(block, textureMapping, this.modelOutput))));
	}

	public void createGenericCube(Block block) {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_particle"))
			.put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_down"))
			.put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_up"))
			.put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_north"))
			.put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_south"))
			.put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_east"))
			.put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_west"));
		this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(ModelTemplates.CUBE.create(block, textureMapping, this.modelOutput))));
	}

	private void createPumpkins() {
		TextureMapping textureMapping = TextureMapping.column(Blocks.PUMPKIN);
		this.blockStateOutput.accept(createSimpleBlock(Blocks.PUMPKIN, plainVariant(ModelLocationUtils.getModelLocation(Blocks.PUMPKIN))));
		this.createPumpkinVariant(Blocks.CARVED_PUMPKIN, textureMapping);
		this.createPumpkinVariant(Blocks.JACK_O_LANTERN, textureMapping);
	}

	private void createPumpkinVariant(Block block, TextureMapping textureMapping) {
		MultiVariant multiVariant = plainVariant(
			ModelTemplates.CUBE_ORIENTABLE.create(block, textureMapping.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(block)), this.modelOutput)
		);
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, multiVariant).with(ROTATION_HORIZONTAL_FACING));
	}

	private void createCauldrons() {
		this.registerSimpleFlatItemModel(Items.CAULDRON);
		this.createNonTemplateModelBlock(Blocks.CAULDRON);
		this.blockStateOutput
			.accept(
				createSimpleBlock(
					Blocks.LAVA_CAULDRON,
					plainVariant(
						ModelTemplates.CAULDRON_FULL
							.create(Blocks.LAVA_CAULDRON, TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.LAVA, "_still")), this.modelOutput)
					)
				)
			);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.WATER_CAULDRON)
					.with(
						PropertyDispatch.initial(LayeredCauldronBlock.LEVEL)
							.select(
								1,
								plainVariant(
									ModelTemplates.CAULDRON_LEVEL1
										.createWithSuffix(Blocks.WATER_CAULDRON, "_level1", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")), this.modelOutput)
								)
							)
							.select(
								2,
								plainVariant(
									ModelTemplates.CAULDRON_LEVEL2
										.createWithSuffix(Blocks.WATER_CAULDRON, "_level2", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")), this.modelOutput)
								)
							)
							.select(
								3,
								plainVariant(
									ModelTemplates.CAULDRON_FULL
										.createWithSuffix(Blocks.WATER_CAULDRON, "_full", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")), this.modelOutput)
								)
							)
					)
			);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.POWDER_SNOW_CAULDRON)
					.with(
						PropertyDispatch.initial(LayeredCauldronBlock.LEVEL)
							.select(
								1,
								plainVariant(
									ModelTemplates.CAULDRON_LEVEL1
										.createWithSuffix(
											Blocks.POWDER_SNOW_CAULDRON, "_level1", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)), this.modelOutput
										)
								)
							)
							.select(
								2,
								plainVariant(
									ModelTemplates.CAULDRON_LEVEL2
										.createWithSuffix(
											Blocks.POWDER_SNOW_CAULDRON, "_level2", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)), this.modelOutput
										)
								)
							)
							.select(
								3,
								plainVariant(
									ModelTemplates.CAULDRON_FULL
										.createWithSuffix(Blocks.POWDER_SNOW_CAULDRON, "_full", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)), this.modelOutput)
								)
							)
					)
			);
	}

	private void createChorusFlower() {
		TextureMapping textureMapping = TextureMapping.defaultTexture(Blocks.CHORUS_FLOWER);
		MultiVariant multiVariant = plainVariant(ModelTemplates.CHORUS_FLOWER.create(Blocks.CHORUS_FLOWER, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(
			this.createSuffixedVariant(
				Blocks.CHORUS_FLOWER, "_dead", ModelTemplates.CHORUS_FLOWER, resourceLocation -> textureMapping.copyAndUpdate(TextureSlot.TEXTURE, resourceLocation)
			)
		);
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(Blocks.CHORUS_FLOWER).with(createEmptyOrFullDispatch(BlockStateProperties.AGE_5, 5, multiVariant2, multiVariant)));
	}

	private void createCrafterBlock() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.CRAFTER));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_triggered"));
		MultiVariant multiVariant3 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting"));
		MultiVariant multiVariant4 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting_triggered"));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.CRAFTER)
					.with(
						PropertyDispatch.initial(BlockStateProperties.TRIGGERED, CrafterBlock.CRAFTING)
							.select(false, false, multiVariant)
							.select(true, true, multiVariant4)
							.select(true, false, multiVariant2)
							.select(false, true, multiVariant3)
					)
					.with(PropertyDispatch.modify(BlockStateProperties.ORIENTATION).generate(BlockModelGenerators::applyRotation))
			);
	}

	private void createDispenserBlock(Block block) {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_side"))
			.put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"));
		TextureMapping textureMapping2 = new TextureMapping()
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
			.put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front_vertical"));
		MultiVariant multiVariant = plainVariant(ModelTemplates.CUBE_ORIENTABLE.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.CUBE_ORIENTABLE_VERTICAL.create(block, textureMapping2, this.modelOutput));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(
						PropertyDispatch.initial(BlockStateProperties.FACING)
							.select(Direction.DOWN, multiVariant2.with(X_ROT_180))
							.select(Direction.UP, multiVariant2)
							.select(Direction.NORTH, multiVariant)
							.select(Direction.EAST, multiVariant.with(Y_ROT_90))
							.select(Direction.SOUTH, multiVariant.with(Y_ROT_180))
							.select(Direction.WEST, multiVariant.with(Y_ROT_270))
					)
			);
	}

	private void createEndPortalFrame() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME, "_filled"));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.END_PORTAL_FRAME)
					.with(PropertyDispatch.initial(BlockStateProperties.EYE).select(false, multiVariant).select(true, multiVariant2))
					.with(ROTATION_HORIZONTAL_FACING_ALT)
			);
	}

	private void createChorusPlant() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_side"));
		Variant variant = plainModel(ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside"));
		Variant variant2 = plainModel(ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside1"));
		Variant variant3 = plainModel(ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside2"));
		Variant variant4 = plainModel(ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside3"));
		Variant variant5 = variant.with(UV_LOCK);
		Variant variant6 = variant2.with(UV_LOCK);
		Variant variant7 = variant3.with(UV_LOCK);
		Variant variant8 = variant4.with(UV_LOCK);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.CHORUS_PLANT)
					.with(condition().term(BlockStateProperties.NORTH, true), multiVariant)
					.with(condition().term(BlockStateProperties.EAST, true), multiVariant.with(Y_ROT_90).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.SOUTH, true), multiVariant.with(Y_ROT_180).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.WEST, true), multiVariant.with(Y_ROT_270).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.UP, true), multiVariant.with(X_ROT_270).with(UV_LOCK))
					.with(condition().term(BlockStateProperties.DOWN, true), multiVariant.with(X_ROT_90).with(UV_LOCK))
					.with(
						condition().term(BlockStateProperties.NORTH, false),
						new MultiVariant(WeightedList.of(new Weighted<>(variant, 2), new Weighted<>(variant2, 1), new Weighted<>(variant3, 1), new Weighted<>(variant4, 1)))
					)
					.with(
						condition().term(BlockStateProperties.EAST, false),
						new MultiVariant(
							WeightedList.of(
								new Weighted<>(variant6.with(Y_ROT_90), 1),
								new Weighted<>(variant7.with(Y_ROT_90), 1),
								new Weighted<>(variant8.with(Y_ROT_90), 1),
								new Weighted<>(variant5.with(Y_ROT_90), 2)
							)
						)
					)
					.with(
						condition().term(BlockStateProperties.SOUTH, false),
						new MultiVariant(
							WeightedList.of(
								new Weighted<>(variant7.with(Y_ROT_180), 1),
								new Weighted<>(variant8.with(Y_ROT_180), 1),
								new Weighted<>(variant5.with(Y_ROT_180), 2),
								new Weighted<>(variant6.with(Y_ROT_180), 1)
							)
						)
					)
					.with(
						condition().term(BlockStateProperties.WEST, false),
						new MultiVariant(
							WeightedList.of(
								new Weighted<>(variant8.with(Y_ROT_270), 1),
								new Weighted<>(variant5.with(Y_ROT_270), 2),
								new Weighted<>(variant6.with(Y_ROT_270), 1),
								new Weighted<>(variant7.with(Y_ROT_270), 1)
							)
						)
					)
					.with(
						condition().term(BlockStateProperties.UP, false),
						new MultiVariant(
							WeightedList.of(
								new Weighted<>(variant5.with(X_ROT_270), 2),
								new Weighted<>(variant8.with(X_ROT_270), 1),
								new Weighted<>(variant6.with(X_ROT_270), 1),
								new Weighted<>(variant7.with(X_ROT_270), 1)
							)
						)
					)
					.with(
						condition().term(BlockStateProperties.DOWN, false),
						new MultiVariant(
							WeightedList.of(
								new Weighted<>(variant8.with(X_ROT_90), 1),
								new Weighted<>(variant7.with(X_ROT_90), 1),
								new Weighted<>(variant6.with(X_ROT_90), 1),
								new Weighted<>(variant5.with(X_ROT_90), 2)
							)
						)
					)
			);
	}

	private void createComposter() {
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.COMPOSTER)
					.with(plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER)))
					.with(condition().term(BlockStateProperties.LEVEL_COMPOSTER, 1), plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents1")))
					.with(condition().term(BlockStateProperties.LEVEL_COMPOSTER, 2), plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents2")))
					.with(condition().term(BlockStateProperties.LEVEL_COMPOSTER, 3), plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents3")))
					.with(condition().term(BlockStateProperties.LEVEL_COMPOSTER, 4), plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents4")))
					.with(condition().term(BlockStateProperties.LEVEL_COMPOSTER, 5), plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents5")))
					.with(condition().term(BlockStateProperties.LEVEL_COMPOSTER, 6), plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents6")))
					.with(condition().term(BlockStateProperties.LEVEL_COMPOSTER, 7), plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents7")))
					.with(condition().term(BlockStateProperties.LEVEL_COMPOSTER, 8), plainVariant(TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents_ready")))
			);
	}

	private void createCopperBulb(Block block) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(block), this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(this.createSuffixedVariant(block, "_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube));
		MultiVariant multiVariant3 = plainVariant(this.createSuffixedVariant(block, "_lit", ModelTemplates.CUBE_ALL, TextureMapping::cube));
		MultiVariant multiVariant4 = plainVariant(this.createSuffixedVariant(block, "_lit_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube));
		this.blockStateOutput.accept(createCopperBulb(block, multiVariant, multiVariant3, multiVariant2, multiVariant4));
	}

	private static BlockModelDefinitionGenerator createCopperBulb(
		Block block, MultiVariant multiVariant, MultiVariant multiVariant2, MultiVariant multiVariant3, MultiVariant multiVariant4
	) {
		return MultiVariantGenerator.dispatch(block)
			.with(PropertyDispatch.initial(BlockStateProperties.LIT, BlockStateProperties.POWERED).generate((boolean_, boolean2) -> {
				if (boolean_) {
					return boolean2 ? multiVariant4 : multiVariant2;
				} else {
					return boolean2 ? multiVariant3 : multiVariant;
				}
			}));
	}

	private void copyCopperBulbModel(Block block, Block block2) {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(block));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(block, "_powered"));
		MultiVariant multiVariant3 = plainVariant(ModelLocationUtils.getModelLocation(block, "_lit"));
		MultiVariant multiVariant4 = plainVariant(ModelLocationUtils.getModelLocation(block, "_lit_powered"));
		this.itemModelOutput.copy(block.asItem(), block2.asItem());
		this.blockStateOutput.accept(createCopperBulb(block2, multiVariant, multiVariant3, multiVariant2, multiVariant4));
	}

	private void createAmethystCluster(Block block) {
		MultiVariant multiVariant = plainVariant(ModelTemplates.CROSS.create(block, TextureMapping.cross(block), this.modelOutput));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, multiVariant).with(ROTATIONS_COLUMN_WITH_FACING));
	}

	private void createAmethystClusters() {
		this.createAmethystCluster(Blocks.SMALL_AMETHYST_BUD);
		this.createAmethystCluster(Blocks.MEDIUM_AMETHYST_BUD);
		this.createAmethystCluster(Blocks.LARGE_AMETHYST_BUD);
		this.createAmethystCluster(Blocks.AMETHYST_CLUSTER);
	}

	private void createPointedDripstone() {
		PropertyDispatch.C2<MultiVariant, Direction, DripstoneThickness> c2 = PropertyDispatch.initial(
			BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.DRIPSTONE_THICKNESS
		);

		for (DripstoneThickness dripstoneThickness : DripstoneThickness.values()) {
			c2.select(Direction.UP, dripstoneThickness, this.createPointedDripstoneVariant(Direction.UP, dripstoneThickness));
		}

		for (DripstoneThickness dripstoneThickness : DripstoneThickness.values()) {
			c2.select(Direction.DOWN, dripstoneThickness, this.createPointedDripstoneVariant(Direction.DOWN, dripstoneThickness));
		}

		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.POINTED_DRIPSTONE).with(c2));
	}

	private MultiVariant createPointedDripstoneVariant(Direction direction, DripstoneThickness dripstoneThickness) {
		String string = "_" + direction.getSerializedName() + "_" + dripstoneThickness.getSerializedName();
		TextureMapping textureMapping = TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.POINTED_DRIPSTONE, string));
		return plainVariant(ModelTemplates.POINTED_DRIPSTONE.createWithSuffix(Blocks.POINTED_DRIPSTONE, string, textureMapping, this.modelOutput));
	}

	private void createNyliumBlock(Block block) {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.NETHERRACK))
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(block))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"));
		this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(ModelTemplates.CUBE_BOTTOM_TOP.create(block, textureMapping, this.modelOutput))));
	}

	private void createDaylightDetector() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_side");
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_top"))
			.put(TextureSlot.SIDE, resourceLocation);
		TextureMapping textureMapping2 = new TextureMapping()
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_inverted_top"))
			.put(TextureSlot.SIDE, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.DAYLIGHT_DETECTOR)
					.with(
						PropertyDispatch.initial(BlockStateProperties.INVERTED)
							.select(false, plainVariant(ModelTemplates.DAYLIGHT_DETECTOR.create(Blocks.DAYLIGHT_DETECTOR, textureMapping, this.modelOutput)))
							.select(
								true,
								plainVariant(
									ModelTemplates.DAYLIGHT_DETECTOR.create(ModelLocationUtils.getModelLocation(Blocks.DAYLIGHT_DETECTOR, "_inverted"), textureMapping2, this.modelOutput)
								)
							)
					)
			);
	}

	private void createRotatableColumn(Block block) {
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(block, plainVariant(ModelLocationUtils.getModelLocation(block))).with(ROTATIONS_COLUMN_WITH_FACING));
	}

	private void createLightningRod() {
		Block block = Blocks.LIGHTNING_ROD;
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(block, "_on"));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(block));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(createBooleanModelDispatch(BlockStateProperties.POWERED, multiVariant, multiVariant2))
					.with(ROTATIONS_COLUMN_WITH_FACING)
			);
	}

	private void createFarmland() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND));
		TextureMapping textureMapping2 = new TextureMapping()
			.put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"));
		MultiVariant multiVariant = plainVariant(ModelTemplates.FARMLAND.create(Blocks.FARMLAND, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(
			ModelTemplates.FARMLAND.create(TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"), textureMapping2, this.modelOutput)
		);
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(Blocks.FARMLAND).with(createEmptyOrFullDispatch(BlockStateProperties.MOISTURE, 7, multiVariant2, multiVariant)));
	}

	private MultiVariant createFloorFireModels(Block block) {
		return variants(
			plainModel(ModelTemplates.FIRE_FLOOR.create(ModelLocationUtils.getModelLocation(block, "_floor0"), TextureMapping.fire0(block), this.modelOutput)),
			plainModel(ModelTemplates.FIRE_FLOOR.create(ModelLocationUtils.getModelLocation(block, "_floor1"), TextureMapping.fire1(block), this.modelOutput))
		);
	}

	private MultiVariant createSideFireModels(Block block) {
		return variants(
			plainModel(ModelTemplates.FIRE_SIDE.create(ModelLocationUtils.getModelLocation(block, "_side0"), TextureMapping.fire0(block), this.modelOutput)),
			plainModel(ModelTemplates.FIRE_SIDE.create(ModelLocationUtils.getModelLocation(block, "_side1"), TextureMapping.fire1(block), this.modelOutput)),
			plainModel(ModelTemplates.FIRE_SIDE_ALT.create(ModelLocationUtils.getModelLocation(block, "_side_alt0"), TextureMapping.fire0(block), this.modelOutput)),
			plainModel(ModelTemplates.FIRE_SIDE_ALT.create(ModelLocationUtils.getModelLocation(block, "_side_alt1"), TextureMapping.fire1(block), this.modelOutput))
		);
	}

	private MultiVariant createTopFireModels(Block block) {
		return variants(
			plainModel(ModelTemplates.FIRE_UP.create(ModelLocationUtils.getModelLocation(block, "_up0"), TextureMapping.fire0(block), this.modelOutput)),
			plainModel(ModelTemplates.FIRE_UP.create(ModelLocationUtils.getModelLocation(block, "_up1"), TextureMapping.fire1(block), this.modelOutput)),
			plainModel(ModelTemplates.FIRE_UP_ALT.create(ModelLocationUtils.getModelLocation(block, "_up_alt0"), TextureMapping.fire0(block), this.modelOutput)),
			plainModel(ModelTemplates.FIRE_UP_ALT.create(ModelLocationUtils.getModelLocation(block, "_up_alt1"), TextureMapping.fire1(block), this.modelOutput))
		);
	}

	private void createFire() {
		ConditionBuilder conditionBuilder = condition()
			.term(BlockStateProperties.NORTH, false)
			.term(BlockStateProperties.EAST, false)
			.term(BlockStateProperties.SOUTH, false)
			.term(BlockStateProperties.WEST, false)
			.term(BlockStateProperties.UP, false);
		MultiVariant multiVariant = this.createFloorFireModels(Blocks.FIRE);
		MultiVariant multiVariant2 = this.createSideFireModels(Blocks.FIRE);
		MultiVariant multiVariant3 = this.createTopFireModels(Blocks.FIRE);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.FIRE)
					.with(conditionBuilder, multiVariant)
					.with(or(condition().term(BlockStateProperties.NORTH, true), conditionBuilder), multiVariant2)
					.with(or(condition().term(BlockStateProperties.EAST, true), conditionBuilder), multiVariant2.with(Y_ROT_90))
					.with(or(condition().term(BlockStateProperties.SOUTH, true), conditionBuilder), multiVariant2.with(Y_ROT_180))
					.with(or(condition().term(BlockStateProperties.WEST, true), conditionBuilder), multiVariant2.with(Y_ROT_270))
					.with(condition().term(BlockStateProperties.UP, true), multiVariant3)
			);
	}

	private void createSoulFire() {
		MultiVariant multiVariant = this.createFloorFireModels(Blocks.SOUL_FIRE);
		MultiVariant multiVariant2 = this.createSideFireModels(Blocks.SOUL_FIRE);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.SOUL_FIRE)
					.with(multiVariant)
					.with(multiVariant2)
					.with(multiVariant2.with(Y_ROT_90))
					.with(multiVariant2.with(Y_ROT_180))
					.with(multiVariant2.with(Y_ROT_270))
			);
	}

	private void createLantern(Block block) {
		MultiVariant multiVariant = plainVariant(TexturedModel.LANTERN.create(block, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(TexturedModel.HANGING_LANTERN.create(block, this.modelOutput));
		this.registerSimpleFlatItemModel(block.asItem());
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(block).with(createBooleanModelDispatch(BlockStateProperties.HANGING, multiVariant2, multiVariant)));
	}

	private void createMuddyMangroveRoots() {
		TextureMapping textureMapping = TextureMapping.column(
			TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_side"), TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_top")
		);
		MultiVariant multiVariant = plainVariant(ModelTemplates.CUBE_COLUMN.create(Blocks.MUDDY_MANGROVE_ROOTS, textureMapping, this.modelOutput));
		this.blockStateOutput.accept(createAxisAlignedPillarBlock(Blocks.MUDDY_MANGROVE_ROOTS, multiVariant));
	}

	private void createMangrovePropagule() {
		this.registerSimpleFlatItemModel(Items.MANGROVE_PROPAGULE);
		Block block = Blocks.MANGROVE_PROPAGULE;
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(block));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.MANGROVE_PROPAGULE)
					.with(
						PropertyDispatch.initial(MangrovePropaguleBlock.HANGING, MangrovePropaguleBlock.AGE)
							.generate((boolean_, integer) -> boolean_ ? plainVariant(ModelLocationUtils.getModelLocation(block, "_hanging_" + integer)) : multiVariant)
					)
			);
	}

	private void createFrostedIce() {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.FROSTED_ICE)
					.with(
						PropertyDispatch.initial(BlockStateProperties.AGE_3)
							.select(0, plainVariant(this.createSuffixedVariant(Blocks.FROSTED_ICE, "_0", ModelTemplates.CUBE_ALL, TextureMapping::cube)))
							.select(1, plainVariant(this.createSuffixedVariant(Blocks.FROSTED_ICE, "_1", ModelTemplates.CUBE_ALL, TextureMapping::cube)))
							.select(2, plainVariant(this.createSuffixedVariant(Blocks.FROSTED_ICE, "_2", ModelTemplates.CUBE_ALL, TextureMapping::cube)))
							.select(3, plainVariant(this.createSuffixedVariant(Blocks.FROSTED_ICE, "_3", ModelTemplates.CUBE_ALL, TextureMapping::cube)))
					)
			);
	}

	private void createGrassBlocks() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.DIRT);
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.BOTTOM, resourceLocation)
			.copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_snow"));
		MultiVariant multiVariant = plainVariant(ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.GRASS_BLOCK, "_snow", textureMapping, this.modelOutput));
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.GRASS_BLOCK);
		this.createGrassLikeBlock(Blocks.GRASS_BLOCK, createRotatedVariants(plainModel(resourceLocation2)), multiVariant);
		this.registerSimpleTintedItemModel(Blocks.GRASS_BLOCK, resourceLocation2, new GrassColorSource());
		MultiVariant multiVariant2 = createRotatedVariants(
			plainModel(
				TexturedModel.CUBE_TOP_BOTTOM
					.get(Blocks.MYCELIUM)
					.updateTextures(textureMappingx -> textureMappingx.put(TextureSlot.BOTTOM, resourceLocation))
					.create(Blocks.MYCELIUM, this.modelOutput)
			)
		);
		this.createGrassLikeBlock(Blocks.MYCELIUM, multiVariant2, multiVariant);
		MultiVariant multiVariant3 = createRotatedVariants(
			plainModel(
				TexturedModel.CUBE_TOP_BOTTOM
					.get(Blocks.PODZOL)
					.updateTextures(textureMappingx -> textureMappingx.put(TextureSlot.BOTTOM, resourceLocation))
					.create(Blocks.PODZOL, this.modelOutput)
			)
		);
		this.createGrassLikeBlock(Blocks.PODZOL, multiVariant3, multiVariant);
	}

	private void createGrassLikeBlock(Block block, MultiVariant multiVariant, MultiVariant multiVariant2) {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(BlockStateProperties.SNOWY).select(true, multiVariant2).select(false, multiVariant))
			);
	}

	private void createCocoa() {
		this.registerSimpleFlatItemModel(Items.COCOA_BEANS);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.COCOA)
					.with(
						PropertyDispatch.initial(BlockStateProperties.AGE_2)
							.select(0, plainVariant(ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage0")))
							.select(1, plainVariant(ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage1")))
							.select(2, plainVariant(ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage2")))
					)
					.with(ROTATION_HORIZONTAL_FACING_ALT)
			);
	}

	private void createDirtPath() {
		Variant variant = plainModel(ModelLocationUtils.getModelLocation(Blocks.DIRT_PATH));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.DIRT_PATH, createRotatedVariants(variant)));
	}

	private void createWeightedPressurePlate(Block block, Block block2) {
		TextureMapping textureMapping = TextureMapping.defaultTexture(block2);
		MultiVariant multiVariant = plainVariant(ModelTemplates.PRESSURE_PLATE_UP.create(block, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.PRESSURE_PLATE_DOWN.create(block, textureMapping, this.modelOutput));
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(block).with(createEmptyOrFullDispatch(BlockStateProperties.POWER, 1, multiVariant2, multiVariant)));
	}

	private void createHopper() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.HOPPER));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.HOPPER, "_side"));
		this.registerSimpleFlatItemModel(Items.HOPPER);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.HOPPER)
					.with(
						PropertyDispatch.initial(BlockStateProperties.FACING_HOPPER)
							.select(Direction.DOWN, multiVariant)
							.select(Direction.NORTH, multiVariant2)
							.select(Direction.EAST, multiVariant2.with(Y_ROT_90))
							.select(Direction.SOUTH, multiVariant2.with(Y_ROT_180))
							.select(Direction.WEST, multiVariant2.with(Y_ROT_270))
					)
			);
	}

	private void copyModel(Block block, Block block2) {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(block));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block2, multiVariant));
		this.itemModelOutput.copy(block.asItem(), block2.asItem());
	}

	private void createIronBars() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post_ends"));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post"));
		MultiVariant multiVariant3 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap"));
		MultiVariant multiVariant4 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap_alt"));
		MultiVariant multiVariant5 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side"));
		MultiVariant multiVariant6 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side_alt"));
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.IRON_BARS)
					.with(multiVariant)
					.with(
						condition()
							.term(BlockStateProperties.NORTH, false)
							.term(BlockStateProperties.EAST, false)
							.term(BlockStateProperties.SOUTH, false)
							.term(BlockStateProperties.WEST, false),
						multiVariant2
					)
					.with(
						condition()
							.term(BlockStateProperties.NORTH, true)
							.term(BlockStateProperties.EAST, false)
							.term(BlockStateProperties.SOUTH, false)
							.term(BlockStateProperties.WEST, false),
						multiVariant3
					)
					.with(
						condition()
							.term(BlockStateProperties.NORTH, false)
							.term(BlockStateProperties.EAST, true)
							.term(BlockStateProperties.SOUTH, false)
							.term(BlockStateProperties.WEST, false),
						multiVariant3.with(Y_ROT_90)
					)
					.with(
						condition()
							.term(BlockStateProperties.NORTH, false)
							.term(BlockStateProperties.EAST, false)
							.term(BlockStateProperties.SOUTH, true)
							.term(BlockStateProperties.WEST, false),
						multiVariant4
					)
					.with(
						condition()
							.term(BlockStateProperties.NORTH, false)
							.term(BlockStateProperties.EAST, false)
							.term(BlockStateProperties.SOUTH, false)
							.term(BlockStateProperties.WEST, true),
						multiVariant4.with(Y_ROT_90)
					)
					.with(condition().term(BlockStateProperties.NORTH, true), multiVariant5)
					.with(condition().term(BlockStateProperties.EAST, true), multiVariant5.with(Y_ROT_90))
					.with(condition().term(BlockStateProperties.SOUTH, true), multiVariant6)
					.with(condition().term(BlockStateProperties.WEST, true), multiVariant6.with(Y_ROT_90))
			);
		this.registerSimpleFlatItemModel(Blocks.IRON_BARS);
	}

	private void createNonTemplateHorizontalBlock(Block block) {
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(block, plainVariant(ModelLocationUtils.getModelLocation(block))).with(ROTATION_HORIZONTAL_FACING));
	}

	private void createLever() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.LEVER));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.LEVER, "_on"));
		this.registerSimpleFlatItemModel(Blocks.LEVER);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.LEVER)
					.with(createBooleanModelDispatch(BlockStateProperties.POWERED, multiVariant, multiVariant2))
					.with(
						PropertyDispatch.modify(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
							.select(AttachFace.CEILING, Direction.NORTH, X_ROT_180.then(Y_ROT_180))
							.select(AttachFace.CEILING, Direction.EAST, X_ROT_180.then(Y_ROT_270))
							.select(AttachFace.CEILING, Direction.SOUTH, X_ROT_180)
							.select(AttachFace.CEILING, Direction.WEST, X_ROT_180.then(Y_ROT_90))
							.select(AttachFace.FLOOR, Direction.NORTH, NOP)
							.select(AttachFace.FLOOR, Direction.EAST, Y_ROT_90)
							.select(AttachFace.FLOOR, Direction.SOUTH, Y_ROT_180)
							.select(AttachFace.FLOOR, Direction.WEST, Y_ROT_270)
							.select(AttachFace.WALL, Direction.NORTH, X_ROT_90)
							.select(AttachFace.WALL, Direction.EAST, X_ROT_90.then(Y_ROT_90))
							.select(AttachFace.WALL, Direction.SOUTH, X_ROT_90.then(Y_ROT_180))
							.select(AttachFace.WALL, Direction.WEST, X_ROT_90.then(Y_ROT_270))
					)
			);
	}

	private void createLilyPad() {
		ResourceLocation resourceLocation = this.createFlatItemModelWithBlockTexture(Items.LILY_PAD, Blocks.LILY_PAD);
		this.registerSimpleTintedItemModel(Blocks.LILY_PAD, resourceLocation, ItemModelUtils.constantTint(-9321636));
		Variant variant = plainModel(ModelLocationUtils.getModelLocation(Blocks.LILY_PAD));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.LILY_PAD, createRotatedVariants(variant)));
	}

	private void createFrogspawnBlock() {
		this.registerSimpleFlatItemModel(Blocks.FROGSPAWN);
		this.blockStateOutput.accept(createSimpleBlock(Blocks.FROGSPAWN, plainVariant(ModelLocationUtils.getModelLocation(Blocks.FROGSPAWN))));
	}

	private void createNetherPortalBlock() {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.NETHER_PORTAL)
					.with(
						PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_AXIS)
							.select(Direction.Axis.X, plainVariant(ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ns")))
							.select(Direction.Axis.Z, plainVariant(ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ew")))
					)
			);
	}

	private void createNetherrack() {
		Variant variant = plainModel(TexturedModel.CUBE.create(Blocks.NETHERRACK, this.modelOutput));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(
					Blocks.NETHERRACK,
					variants(
						variant,
						variant.with(X_ROT_90),
						variant.with(X_ROT_180),
						variant.with(X_ROT_270),
						variant.with(Y_ROT_90),
						variant.with(Y_ROT_90.then(X_ROT_90)),
						variant.with(Y_ROT_90.then(X_ROT_180)),
						variant.with(Y_ROT_90.then(X_ROT_270)),
						variant.with(Y_ROT_180),
						variant.with(Y_ROT_180.then(X_ROT_90)),
						variant.with(Y_ROT_180.then(X_ROT_180)),
						variant.with(Y_ROT_180.then(X_ROT_270)),
						variant.with(Y_ROT_270),
						variant.with(Y_ROT_270.then(X_ROT_90)),
						variant.with(Y_ROT_270.then(X_ROT_180)),
						variant.with(Y_ROT_270.then(X_ROT_270))
					)
				)
			);
	}

	private void createObserver() {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.OBSERVER));
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.OBSERVER, "_on"));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.OBSERVER)
					.with(createBooleanModelDispatch(BlockStateProperties.POWERED, multiVariant2, multiVariant))
					.with(ROTATION_FACING)
			);
	}

	private void createPistons() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.PISTON, "_bottom"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky");
		ResourceLocation resourceLocation2 = TextureMapping.getBlockTexture(Blocks.PISTON, "_top");
		TextureMapping textureMapping2 = textureMapping.copyAndUpdate(TextureSlot.PLATFORM, resourceLocation);
		TextureMapping textureMapping3 = textureMapping.copyAndUpdate(TextureSlot.PLATFORM, resourceLocation2);
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(Blocks.PISTON, "_base"));
		this.createPistonVariant(Blocks.PISTON, multiVariant, textureMapping3);
		this.createPistonVariant(Blocks.STICKY_PISTON, multiVariant, textureMapping2);
		ResourceLocation resourceLocation3 = ModelTemplates.CUBE_BOTTOM_TOP
			.createWithSuffix(Blocks.PISTON, "_inventory", textureMapping.copyAndUpdate(TextureSlot.TOP, resourceLocation2), this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.CUBE_BOTTOM_TOP
			.createWithSuffix(Blocks.STICKY_PISTON, "_inventory", textureMapping.copyAndUpdate(TextureSlot.TOP, resourceLocation), this.modelOutput);
		this.registerSimpleItemModel(Blocks.PISTON, resourceLocation3);
		this.registerSimpleItemModel(Blocks.STICKY_PISTON, resourceLocation4);
	}

	private void createPistonVariant(Block block, MultiVariant multiVariant, TextureMapping textureMapping) {
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.PISTON.create(block, textureMapping, this.modelOutput));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block).with(createBooleanModelDispatch(BlockStateProperties.EXTENDED, multiVariant, multiVariant2)).with(ROTATION_FACING)
			);
	}

	private void createPistonHeads() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.UNSTICKY, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
		TextureMapping textureMapping2 = textureMapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky"));
		TextureMapping textureMapping3 = textureMapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.PISTON_HEAD)
					.with(
						PropertyDispatch.initial(BlockStateProperties.SHORT, BlockStateProperties.PISTON_TYPE)
							.select(false, PistonType.DEFAULT, plainVariant(ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head", textureMapping3, this.modelOutput)))
							.select(
								false, PistonType.STICKY, plainVariant(ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head_sticky", textureMapping2, this.modelOutput))
							)
							.select(
								true,
								PistonType.DEFAULT,
								plainVariant(ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short", textureMapping3, this.modelOutput))
							)
							.select(
								true,
								PistonType.STICKY,
								plainVariant(ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short_sticky", textureMapping2, this.modelOutput))
							)
					)
					.with(ROTATION_FACING)
			);
	}

	private void createTrialSpawner() {
		Block block = Blocks.TRIAL_SPAWNER;
		TextureMapping textureMapping = TextureMapping.trialSpawner(block, "_side_inactive", "_top_inactive");
		TextureMapping textureMapping2 = TextureMapping.trialSpawner(block, "_side_active", "_top_active");
		TextureMapping textureMapping3 = TextureMapping.trialSpawner(block, "_side_active", "_top_ejecting_reward");
		TextureMapping textureMapping4 = TextureMapping.trialSpawner(block, "_side_inactive_ominous", "_top_inactive_ominous");
		TextureMapping textureMapping5 = TextureMapping.trialSpawner(block, "_side_active_ominous", "_top_active_ominous");
		TextureMapping textureMapping6 = TextureMapping.trialSpawner(block, "_side_active_ominous", "_top_ejecting_reward_ominous");
		ResourceLocation resourceLocation = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.create(block, textureMapping, this.modelOutput);
		MultiVariant multiVariant = plainVariant(resourceLocation);
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_active", textureMapping2, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(
			ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_ejecting_reward", textureMapping3, this.modelOutput)
		);
		MultiVariant multiVariant4 = plainVariant(
			ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_inactive_ominous", textureMapping4, this.modelOutput)
		);
		MultiVariant multiVariant5 = plainVariant(
			ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_active_ominous", textureMapping5, this.modelOutput)
		);
		MultiVariant multiVariant6 = plainVariant(
			ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_ejecting_reward_ominous", textureMapping6, this.modelOutput)
		);
		this.registerSimpleItemModel(block, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(PropertyDispatch.initial(BlockStateProperties.TRIAL_SPAWNER_STATE, BlockStateProperties.OMINOUS).generate((trialSpawnerState, boolean_) -> {
						return switch (trialSpawnerState) {
							case INACTIVE, COOLDOWN -> boolean_ ? multiVariant4 : multiVariant;
							case WAITING_FOR_PLAYERS, ACTIVE, WAITING_FOR_REWARD_EJECTION -> boolean_ ? multiVariant5 : multiVariant2;
							case EJECTING_REWARD -> boolean_ ? multiVariant6 : multiVariant3;
						};
					}))
			);
	}

	private void createVault() {
		Block block = Blocks.VAULT;
		TextureMapping textureMapping = TextureMapping.vault(block, "_front_off", "_side_off", "_top", "_bottom");
		TextureMapping textureMapping2 = TextureMapping.vault(block, "_front_on", "_side_on", "_top", "_bottom");
		TextureMapping textureMapping3 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top", "_bottom");
		TextureMapping textureMapping4 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top_ejecting", "_bottom");
		ResourceLocation resourceLocation = ModelTemplates.VAULT.create(block, textureMapping, this.modelOutput);
		MultiVariant multiVariant = plainVariant(resourceLocation);
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.VAULT.createWithSuffix(block, "_active", textureMapping2, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.VAULT.createWithSuffix(block, "_unlocking", textureMapping3, this.modelOutput));
		MultiVariant multiVariant4 = plainVariant(ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward", textureMapping4, this.modelOutput));
		TextureMapping textureMapping5 = TextureMapping.vault(block, "_front_off_ominous", "_side_off_ominous", "_top_ominous", "_bottom_ominous");
		TextureMapping textureMapping6 = TextureMapping.vault(block, "_front_on_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
		TextureMapping textureMapping7 = TextureMapping.vault(block, "_front_ejecting_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
		TextureMapping textureMapping8 = TextureMapping.vault(block, "_front_ejecting_ominous", "_side_on_ominous", "_top_ejecting_ominous", "_bottom_ominous");
		MultiVariant multiVariant5 = plainVariant(ModelTemplates.VAULT.createWithSuffix(block, "_ominous", textureMapping5, this.modelOutput));
		MultiVariant multiVariant6 = plainVariant(ModelTemplates.VAULT.createWithSuffix(block, "_active_ominous", textureMapping6, this.modelOutput));
		MultiVariant multiVariant7 = plainVariant(ModelTemplates.VAULT.createWithSuffix(block, "_unlocking_ominous", textureMapping7, this.modelOutput));
		MultiVariant multiVariant8 = plainVariant(ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward_ominous", textureMapping8, this.modelOutput));
		this.registerSimpleItemModel(block, resourceLocation);
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(VaultBlock.STATE, VaultBlock.OMINOUS).generate((vaultState, boolean_) -> {
				return switch (vaultState) {
					case INACTIVE -> boolean_ ? multiVariant5 : multiVariant;
					case ACTIVE -> boolean_ ? multiVariant6 : multiVariant2;
					case UNLOCKING -> boolean_ ? multiVariant7 : multiVariant3;
					case EJECTING -> boolean_ ? multiVariant8 : multiVariant4;
				};
			})).with(ROTATION_HORIZONTAL_FACING));
	}

	private void createSculkSensor() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_inactive");
		MultiVariant multiVariant = plainVariant(resourceLocation);
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_active"));
		this.registerSimpleItemModel(Blocks.SCULK_SENSOR, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.SCULK_SENSOR)
					.with(
						PropertyDispatch.initial(BlockStateProperties.SCULK_SENSOR_PHASE)
							.generate(
								sculkSensorPhase -> sculkSensorPhase != SculkSensorPhase.ACTIVE && sculkSensorPhase != SculkSensorPhase.COOLDOWN ? multiVariant : multiVariant2
							)
					)
			);
	}

	private void createCalibratedSculkSensor() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_inactive");
		MultiVariant multiVariant = plainVariant(resourceLocation);
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_active"));
		this.registerSimpleItemModel(Blocks.CALIBRATED_SCULK_SENSOR, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.CALIBRATED_SCULK_SENSOR)
					.with(
						PropertyDispatch.initial(BlockStateProperties.SCULK_SENSOR_PHASE)
							.generate(
								sculkSensorPhase -> sculkSensorPhase != SculkSensorPhase.ACTIVE && sculkSensorPhase != SculkSensorPhase.COOLDOWN ? multiVariant : multiVariant2
							)
					)
					.with(ROTATION_HORIZONTAL_FACING)
			);
	}

	private void createSculkShrieker() {
		ResourceLocation resourceLocation = ModelTemplates.SCULK_SHRIEKER.create(Blocks.SCULK_SHRIEKER, TextureMapping.sculkShrieker(false), this.modelOutput);
		MultiVariant multiVariant = plainVariant(resourceLocation);
		MultiVariant multiVariant2 = plainVariant(
			ModelTemplates.SCULK_SHRIEKER.createWithSuffix(Blocks.SCULK_SHRIEKER, "_can_summon", TextureMapping.sculkShrieker(true), this.modelOutput)
		);
		this.registerSimpleItemModel(Blocks.SCULK_SHRIEKER, resourceLocation);
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(Blocks.SCULK_SHRIEKER).with(createBooleanModelDispatch(BlockStateProperties.CAN_SUMMON, multiVariant2, multiVariant)));
	}

	private void createScaffolding() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_stable");
		MultiVariant multiVariant = plainVariant(resourceLocation);
		MultiVariant multiVariant2 = plainVariant(ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_unstable"));
		this.registerSimpleItemModel(Blocks.SCAFFOLDING, resourceLocation);
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(Blocks.SCAFFOLDING).with(createBooleanModelDispatch(BlockStateProperties.BOTTOM, multiVariant2, multiVariant)));
	}

	private void createCaveVines() {
		MultiVariant multiVariant = plainVariant(this.createSuffixedVariant(Blocks.CAVE_VINES, "", ModelTemplates.CROSS, TextureMapping::cross));
		MultiVariant multiVariant2 = plainVariant(this.createSuffixedVariant(Blocks.CAVE_VINES, "_lit", ModelTemplates.CROSS, TextureMapping::cross));
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(Blocks.CAVE_VINES).with(createBooleanModelDispatch(BlockStateProperties.BERRIES, multiVariant2, multiVariant)));
		MultiVariant multiVariant3 = plainVariant(this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "", ModelTemplates.CROSS, TextureMapping::cross));
		MultiVariant multiVariant4 = plainVariant(this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "_lit", ModelTemplates.CROSS, TextureMapping::cross));
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(Blocks.CAVE_VINES_PLANT).with(createBooleanModelDispatch(BlockStateProperties.BERRIES, multiVariant4, multiVariant3)));
	}

	private void createRedstoneLamp() {
		MultiVariant multiVariant = plainVariant(TexturedModel.CUBE.create(Blocks.REDSTONE_LAMP, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(this.createSuffixedVariant(Blocks.REDSTONE_LAMP, "_on", ModelTemplates.CUBE_ALL, TextureMapping::cube));
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(Blocks.REDSTONE_LAMP).with(createBooleanModelDispatch(BlockStateProperties.LIT, multiVariant2, multiVariant)));
	}

	private void createNormalTorch(Block block, Block block2) {
		TextureMapping textureMapping = TextureMapping.torch(block);
		this.blockStateOutput.accept(createSimpleBlock(block, plainVariant(ModelTemplates.TORCH.create(block, textureMapping, this.modelOutput))));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block2, plainVariant(ModelTemplates.WALL_TORCH.create(block2, textureMapping, this.modelOutput))).with(ROTATION_TORCH)
			);
		this.registerSimpleFlatItemModel(block);
	}

	private void createRedstoneTorch() {
		TextureMapping textureMapping = TextureMapping.torch(Blocks.REDSTONE_TORCH);
		TextureMapping textureMapping2 = TextureMapping.torch(TextureMapping.getBlockTexture(Blocks.REDSTONE_TORCH, "_off"));
		MultiVariant multiVariant = plainVariant(ModelTemplates.REDSTONE_TORCH.create(Blocks.REDSTONE_TORCH, textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.TORCH_UNLIT.createWithSuffix(Blocks.REDSTONE_TORCH, "_off", textureMapping2, this.modelOutput));
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(Blocks.REDSTONE_TORCH).with(createBooleanModelDispatch(BlockStateProperties.LIT, multiVariant, multiVariant2)));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.REDSTONE_WALL_TORCH.create(Blocks.REDSTONE_WALL_TORCH, textureMapping, this.modelOutput));
		MultiVariant multiVariant4 = plainVariant(
			ModelTemplates.WALL_TORCH_UNLIT.createWithSuffix(Blocks.REDSTONE_WALL_TORCH, "_off", textureMapping2, this.modelOutput)
		);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.REDSTONE_WALL_TORCH)
					.with(createBooleanModelDispatch(BlockStateProperties.LIT, multiVariant3, multiVariant4))
					.with(ROTATION_TORCH)
			);
		this.registerSimpleFlatItemModel(Blocks.REDSTONE_TORCH);
	}

	private void createRepeater() {
		this.registerSimpleFlatItemModel(Items.REPEATER);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.REPEATER)
					.with(
						PropertyDispatch.initial(BlockStateProperties.DELAY, BlockStateProperties.LOCKED, BlockStateProperties.POWERED)
							.generate((integer, boolean_, boolean2) -> {
								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append('_').append(integer).append("tick");
								if (boolean2) {
									stringBuilder.append("_on");
								}

								if (boolean_) {
									stringBuilder.append("_locked");
								}

								return plainVariant(TextureMapping.getBlockTexture(Blocks.REPEATER, stringBuilder.toString()));
							})
					)
					.with(ROTATION_HORIZONTAL_FACING_ALT)
			);
	}

	private void createSeaPickle() {
		this.registerSimpleFlatItemModel(Items.SEA_PICKLE);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.SEA_PICKLE)
					.with(
						PropertyDispatch.initial(BlockStateProperties.PICKLES, BlockStateProperties.WATERLOGGED)
							.select(1, false, createRotatedVariants(plainModel(ModelLocationUtils.decorateBlockModelLocation("dead_sea_pickle"))))
							.select(2, false, createRotatedVariants(plainModel(ModelLocationUtils.decorateBlockModelLocation("two_dead_sea_pickles"))))
							.select(3, false, createRotatedVariants(plainModel(ModelLocationUtils.decorateBlockModelLocation("three_dead_sea_pickles"))))
							.select(4, false, createRotatedVariants(plainModel(ModelLocationUtils.decorateBlockModelLocation("four_dead_sea_pickles"))))
							.select(1, true, createRotatedVariants(plainModel(ModelLocationUtils.decorateBlockModelLocation("sea_pickle"))))
							.select(2, true, createRotatedVariants(plainModel(ModelLocationUtils.decorateBlockModelLocation("two_sea_pickles"))))
							.select(3, true, createRotatedVariants(plainModel(ModelLocationUtils.decorateBlockModelLocation("three_sea_pickles"))))
							.select(4, true, createRotatedVariants(plainModel(ModelLocationUtils.decorateBlockModelLocation("four_sea_pickles"))))
					)
			);
	}

	private void createSnowBlocks() {
		TextureMapping textureMapping = TextureMapping.cube(Blocks.SNOW);
		MultiVariant multiVariant = plainVariant(ModelTemplates.CUBE_ALL.create(Blocks.SNOW_BLOCK, textureMapping, this.modelOutput));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.SNOW)
					.with(
						PropertyDispatch.initial(BlockStateProperties.LAYERS)
							.generate(integer -> integer < 8 ? plainVariant(ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height" + integer * 2)) : multiVariant)
					)
			);
		this.registerSimpleItemModel(Blocks.SNOW, ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height2"));
		this.blockStateOutput.accept(createSimpleBlock(Blocks.SNOW_BLOCK, multiVariant));
	}

	private void createStonecutter() {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.STONECUTTER, plainVariant(ModelLocationUtils.getModelLocation(Blocks.STONECUTTER))).with(ROTATION_HORIZONTAL_FACING)
			);
	}

	private void createStructureBlock() {
		ResourceLocation resourceLocation = TexturedModel.CUBE.create(Blocks.STRUCTURE_BLOCK, this.modelOutput);
		this.registerSimpleItemModel(Blocks.STRUCTURE_BLOCK, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.STRUCTURE_BLOCK)
					.with(
						PropertyDispatch.initial(BlockStateProperties.STRUCTUREBLOCK_MODE)
							.generate(
								structureMode -> plainVariant(
									this.createSuffixedVariant(Blocks.STRUCTURE_BLOCK, "_" + structureMode.getSerializedName(), ModelTemplates.CUBE_ALL, TextureMapping::cube)
								)
							)
					)
			);
	}

	private void createTestBlock() {
		Map<TestBlockMode, ResourceLocation> map = new HashMap();

		for (TestBlockMode testBlockMode : TestBlockMode.values()) {
			map.put(testBlockMode, this.createSuffixedVariant(Blocks.TEST_BLOCK, "_" + testBlockMode.getSerializedName(), ModelTemplates.CUBE_ALL, TextureMapping::cube));
		}

		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.TEST_BLOCK)
					.with(PropertyDispatch.initial(BlockStateProperties.TEST_BLOCK_MODE).generate(testBlockModex -> plainVariant((ResourceLocation)map.get(testBlockModex))))
			);
		this.itemModelOutput
			.accept(
				Items.TEST_BLOCK,
				ItemModelUtils.selectBlockItemProperty(
					TestBlock.MODE,
					ItemModelUtils.plainModel((ResourceLocation)map.get(TestBlockMode.START)),
					Map.of(
						TestBlockMode.FAIL,
						ItemModelUtils.plainModel((ResourceLocation)map.get(TestBlockMode.FAIL)),
						TestBlockMode.LOG,
						ItemModelUtils.plainModel((ResourceLocation)map.get(TestBlockMode.LOG)),
						TestBlockMode.ACCEPT,
						ItemModelUtils.plainModel((ResourceLocation)map.get(TestBlockMode.ACCEPT))
					)
				)
			);
	}

	private void createSweetBerryBush() {
		this.registerSimpleFlatItemModel(Items.SWEET_BERRIES);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.SWEET_BERRY_BUSH)
					.with(
						PropertyDispatch.initial(BlockStateProperties.AGE_3)
							.generate(integer -> plainVariant(this.createSuffixedVariant(Blocks.SWEET_BERRY_BUSH, "_stage" + integer, ModelTemplates.CROSS, TextureMapping::cross)))
					)
			);
	}

	private void createTripwire() {
		this.registerSimpleFlatItemModel(Items.STRING);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.TRIPWIRE)
					.with(
						PropertyDispatch.initial(
								BlockStateProperties.ATTACHED, BlockStateProperties.EAST, BlockStateProperties.NORTH, BlockStateProperties.SOUTH, BlockStateProperties.WEST
							)
							.select(false, false, false, false, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns")))
							.select(false, true, false, false, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n")).with(Y_ROT_90))
							.select(false, false, true, false, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n")))
							.select(false, false, false, true, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n")).with(Y_ROT_180))
							.select(false, false, false, false, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n")).with(Y_ROT_270))
							.select(false, true, true, false, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne")))
							.select(false, true, false, true, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne")).with(Y_ROT_90))
							.select(false, false, false, true, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne")).with(Y_ROT_180))
							.select(false, false, true, false, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne")).with(Y_ROT_270))
							.select(false, false, true, true, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns")))
							.select(false, true, false, false, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns")).with(Y_ROT_90))
							.select(false, true, true, true, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse")))
							.select(false, true, false, true, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse")).with(Y_ROT_90))
							.select(false, false, true, true, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse")).with(Y_ROT_180))
							.select(false, true, true, false, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse")).with(Y_ROT_270))
							.select(false, true, true, true, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nsew")))
							.select(true, false, false, false, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns")))
							.select(true, false, true, false, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n")))
							.select(true, false, false, true, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n")).with(Y_ROT_180))
							.select(true, true, false, false, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n")).with(Y_ROT_90))
							.select(true, false, false, false, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n")).with(Y_ROT_270))
							.select(true, true, true, false, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne")))
							.select(true, true, false, true, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne")).with(Y_ROT_90))
							.select(true, false, false, true, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne")).with(Y_ROT_180))
							.select(true, false, true, false, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne")).with(Y_ROT_270))
							.select(true, false, true, true, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns")))
							.select(true, true, false, false, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns")).with(Y_ROT_90))
							.select(true, true, true, true, false, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse")))
							.select(true, true, false, true, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse")).with(Y_ROT_90))
							.select(true, false, true, true, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse")).with(Y_ROT_180))
							.select(true, true, true, false, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse")).with(Y_ROT_270))
							.select(true, true, true, true, true, plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nsew")))
					)
			);
	}

	private void createTripwireHook() {
		this.registerSimpleFlatItemModel(Blocks.TRIPWIRE_HOOK);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.TRIPWIRE_HOOK)
					.with(
						PropertyDispatch.initial(BlockStateProperties.ATTACHED, BlockStateProperties.POWERED)
							.generate(
								(boolean_, boolean2) -> plainVariant(ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE_HOOK, (boolean_ ? "_attached" : "") + (boolean2 ? "_on" : "")))
							)
					)
					.with(ROTATION_HORIZONTAL_FACING)
			);
	}

	private Variant createTurtleEggModel(int i, String string, TextureMapping textureMapping) {
		return switch (i) {
			case 1 -> plainModel(
				ModelTemplates.TURTLE_EGG.create(ModelLocationUtils.decorateBlockModelLocation(string + "turtle_egg"), textureMapping, this.modelOutput)
			);
			case 2 -> plainModel(
				ModelTemplates.TWO_TURTLE_EGGS.create(ModelLocationUtils.decorateBlockModelLocation("two_" + string + "turtle_eggs"), textureMapping, this.modelOutput)
			);
			case 3 -> plainModel(
				ModelTemplates.THREE_TURTLE_EGGS.create(ModelLocationUtils.decorateBlockModelLocation("three_" + string + "turtle_eggs"), textureMapping, this.modelOutput)
			);
			case 4 -> plainModel(
				ModelTemplates.FOUR_TURTLE_EGGS.create(ModelLocationUtils.decorateBlockModelLocation("four_" + string + "turtle_eggs"), textureMapping, this.modelOutput)
			);
			default -> throw new UnsupportedOperationException();
		};
	}

	private Variant createTurtleEggModel(int i, int j) {
		return switch (j) {
			case 0 -> this.createTurtleEggModel(i, "", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG)));
			case 1 -> this.createTurtleEggModel(i, "slightly_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_slightly_cracked")));
			case 2 -> this.createTurtleEggModel(i, "very_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_very_cracked")));
			default -> throw new UnsupportedOperationException();
		};
	}

	private void createTurtleEgg() {
		this.registerSimpleFlatItemModel(Items.TURTLE_EGG);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.TURTLE_EGG)
					.with(
						PropertyDispatch.initial(BlockStateProperties.EGGS, BlockStateProperties.HATCH)
							.generate((integer, integer2) -> createRotatedVariants(this.createTurtleEggModel(integer, integer2)))
					)
			);
	}

	private void createDriedGhastBlock() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.DRIED_GHAST, "_hydration_0");
		this.registerSimpleItemModel(Blocks.DRIED_GHAST, resourceLocation);
		Function<Integer, ResourceLocation> function = integer -> {
			String string = switch (integer) {
				case 1 -> "_hydration_1";
				case 2 -> "_hydration_2";
				case 3 -> "_hydration_3";
				default -> "_hydration_0";
			};
			TextureMapping textureMapping = TextureMapping.driedGhast(string);
			return ModelTemplates.DRIED_GHAST.createWithSuffix(Blocks.DRIED_GHAST, string, textureMapping, this.modelOutput);
		};
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.DRIED_GHAST)
					.with(PropertyDispatch.initial(DriedGhastBlock.HYDRATION_LEVEL).generate(integer -> plainVariant((ResourceLocation)function.apply(integer))))
					.with(ROTATION_HORIZONTAL_FACING)
			);
	}

	private void createSnifferEgg() {
		this.registerSimpleFlatItemModel(Items.SNIFFER_EGG);
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.SNIFFER_EGG).with(PropertyDispatch.initial(SnifferEggBlock.HATCH).generate(integer -> {
			String string = switch (integer) {
				case 1 -> "_slightly_cracked";
				case 2 -> "_very_cracked";
				default -> "_not_cracked";
			};
			TextureMapping textureMapping = TextureMapping.snifferEgg(string);
			return plainVariant(ModelTemplates.SNIFFER_EGG.createWithSuffix(Blocks.SNIFFER_EGG, string, textureMapping, this.modelOutput));
		})));
	}

	private void createMultiface(Block block) {
		this.registerSimpleFlatItemModel(block);
		this.createMultifaceBlockStates(block);
	}

	private void createMultiface(Block block, Item item) {
		this.registerSimpleFlatItemModel(item);
		this.createMultifaceBlockStates(block);
	}

	private static <T extends Property<?>> Map<T, VariantMutator> selectMultifaceProperties(StateHolder<?, ?> stateHolder, Function<Direction, T> function) {
		Builder<T, VariantMutator> builder = ImmutableMap.builderWithExpectedSize(MULTIFACE_GENERATOR.size());
		MULTIFACE_GENERATOR.forEach((direction, variantMutator) -> {
			T property = (T)function.apply(direction);
			if (stateHolder.hasProperty(property)) {
				builder.put(property, variantMutator);
			}
		});
		return builder.build();
	}

	private void createMultifaceBlockStates(Block block) {
		Map<Property<Boolean>, VariantMutator> map = selectMultifaceProperties(block.defaultBlockState(), MultifaceBlock::getFaceProperty);
		ConditionBuilder conditionBuilder = condition();
		map.forEach((property, variantMutator) -> conditionBuilder.term(property, false));
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(block));
		MultiPartGenerator multiPartGenerator = MultiPartGenerator.multiPart(block);
		map.forEach((property, variantMutator) -> {
			multiPartGenerator.with(condition().term(property, true), multiVariant.with(variantMutator));
			multiPartGenerator.with(conditionBuilder, multiVariant.with(variantMutator));
		});
		this.blockStateOutput.accept(multiPartGenerator);
	}

	private void createMossyCarpet(Block block) {
		Map<Property<WallSide>, VariantMutator> map = selectMultifaceProperties(block.defaultBlockState(), MossyCarpetBlock::getPropertyForFace);
		ConditionBuilder conditionBuilder = condition().term(MossyCarpetBlock.BASE, false);
		map.forEach((property, variantMutator) -> conditionBuilder.term(property, WallSide.NONE));
		MultiVariant multiVariant = plainVariant(TexturedModel.CARPET.create(block, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(
			TexturedModel.MOSSY_CARPET_SIDE
				.get(block)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side_tall")))
				.createWithSuffix(block, "_side_tall", this.modelOutput)
		);
		MultiVariant multiVariant3 = plainVariant(
			TexturedModel.MOSSY_CARPET_SIDE
				.get(block)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side_small")))
				.createWithSuffix(block, "_side_small", this.modelOutput)
		);
		MultiPartGenerator multiPartGenerator = MultiPartGenerator.multiPart(block);
		multiPartGenerator.with(condition().term(MossyCarpetBlock.BASE, true), multiVariant);
		multiPartGenerator.with(conditionBuilder, multiVariant);
		map.forEach((property, variantMutator) -> {
			multiPartGenerator.with(condition().term(property, WallSide.TALL), multiVariant2.with(variantMutator));
			multiPartGenerator.with(condition().term(property, WallSide.LOW), multiVariant3.with(variantMutator));
			multiPartGenerator.with(conditionBuilder, multiVariant2.with(variantMutator));
		});
		this.blockStateOutput.accept(multiPartGenerator);
	}

	private void createHangingMoss(Block block) {
		this.registerSimpleFlatItemModel(block);
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(HangingMossBlock.TIP).generate(boolean_ -> {
			String string = boolean_ ? "_tip" : "";
			TextureMapping textureMapping = TextureMapping.cross(TextureMapping.getBlockTexture(block, string));
			return plainVariant(BlockModelGenerators.PlantType.NOT_TINTED.getCross().createWithSuffix(block, string, textureMapping, this.modelOutput));
		})));
	}

	private void createSculkCatalyst() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_bottom");
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.BOTTOM, resourceLocation)
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side"));
		TextureMapping textureMapping2 = new TextureMapping()
			.put(TextureSlot.BOTTOM, resourceLocation)
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top_bloom"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side_bloom"));
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_BOTTOM_TOP.create(Blocks.SCULK_CATALYST, textureMapping, this.modelOutput);
		MultiVariant multiVariant = plainVariant(resourceLocation2);
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.SCULK_CATALYST, "_bloom", textureMapping2, this.modelOutput));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.SCULK_CATALYST)
					.with(PropertyDispatch.initial(BlockStateProperties.BLOOM).generate(boolean_ -> boolean_ ? multiVariant2 : multiVariant))
			);
		this.registerSimpleItemModel(Blocks.SCULK_CATALYST, resourceLocation2);
	}

	private void createChiseledBookshelf() {
		Block block = Blocks.CHISELED_BOOKSHELF;
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(block));
		MultiPartGenerator multiPartGenerator = MultiPartGenerator.multiPart(block);
		List.of(Pair.of(Direction.NORTH, NOP), Pair.of(Direction.EAST, Y_ROT_90), Pair.of(Direction.SOUTH, Y_ROT_180), Pair.of(Direction.WEST, Y_ROT_270))
			.forEach(pair -> {
				Direction direction = (Direction)pair.getFirst();
				VariantMutator variantMutator = (VariantMutator)pair.getSecond();
				Condition condition = condition().term(BlockStateProperties.HORIZONTAL_FACING, direction).build();
				multiPartGenerator.with(condition, multiVariant.with(variantMutator).with(UV_LOCK));
				this.addSlotStateAndRotationVariants(multiPartGenerator, condition, variantMutator);
			});
		this.blockStateOutput.accept(multiPartGenerator);
		this.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block, "_inventory"));
		CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.clear();
	}

	private void addSlotStateAndRotationVariants(MultiPartGenerator multiPartGenerator, Condition condition, VariantMutator variantMutator) {
		List.of(
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_LEFT),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_MID),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_RIGHT),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_LEFT),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_MID),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_RIGHT)
			)
			.forEach(pair -> {
				BooleanProperty booleanProperty = (BooleanProperty)pair.getFirst();
				ModelTemplate modelTemplate = (ModelTemplate)pair.getSecond();
				this.addBookSlotModel(multiPartGenerator, condition, variantMutator, booleanProperty, modelTemplate, true);
				this.addBookSlotModel(multiPartGenerator, condition, variantMutator, booleanProperty, modelTemplate, false);
			});
	}

	private void addBookSlotModel(
		MultiPartGenerator multiPartGenerator,
		Condition condition,
		VariantMutator variantMutator,
		BooleanProperty booleanProperty,
		ModelTemplate modelTemplate,
		boolean bl
	) {
		String string = bl ? "_occupied" : "_empty";
		TextureMapping textureMapping = new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(Blocks.CHISELED_BOOKSHELF, string));
		BlockModelGenerators.BookSlotModelCacheKey bookSlotModelCacheKey = new BlockModelGenerators.BookSlotModelCacheKey(modelTemplate, string);
		MultiVariant multiVariant = plainVariant(
			(ResourceLocation)CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.computeIfAbsent(
				bookSlotModelCacheKey, bookSlotModelCacheKeyx -> modelTemplate.createWithSuffix(Blocks.CHISELED_BOOKSHELF, string, textureMapping, this.modelOutput)
			)
		);
		multiPartGenerator.with(
			new CombinedCondition(CombinedCondition.Operation.AND, List.of(condition, condition().term(booleanProperty, bl).build())), multiVariant.with(variantMutator)
		);
	}

	private void createMagmaBlock() {
		MultiVariant multiVariant = plainVariant(
			ModelTemplates.CUBE_ALL.create(Blocks.MAGMA_BLOCK, TextureMapping.cube(ModelLocationUtils.decorateBlockModelLocation("magma")), this.modelOutput)
		);
		this.blockStateOutput.accept(createSimpleBlock(Blocks.MAGMA_BLOCK, multiVariant));
	}

	private void createShulkerBox(Block block, @Nullable DyeColor dyeColor) {
		this.createParticleOnlyBlock(block);
		Item item = block.asItem();
		ResourceLocation resourceLocation = ModelTemplates.SHULKER_BOX_INVENTORY.create(item, TextureMapping.particle(block), this.modelOutput);
		ItemModel.Unbaked unbaked = dyeColor != null
			? ItemModelUtils.specialModel(resourceLocation, new ShulkerBoxSpecialRenderer.Unbaked(dyeColor))
			: ItemModelUtils.specialModel(resourceLocation, new ShulkerBoxSpecialRenderer.Unbaked());
		this.itemModelOutput.accept(item, unbaked);
	}

	private void createGrowingPlant(Block block, Block block2, BlockModelGenerators.PlantType plantType) {
		this.createCrossBlock(block, plantType);
		this.createCrossBlock(block2, plantType);
	}

	private void createInfestedStone() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.STONE);
		Variant variant = plainModel(resourceLocation);
		Variant variant2 = plainModel(ModelLocationUtils.getModelLocation(Blocks.STONE, "_mirrored"));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INFESTED_STONE, createRotatedVariants(variant, variant2)));
		this.registerSimpleItemModel(Blocks.INFESTED_STONE, resourceLocation);
	}

	private void createInfestedDeepslate() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE);
		Variant variant = plainModel(resourceLocation);
		Variant variant2 = plainModel(ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE, "_mirrored"));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.INFESTED_DEEPSLATE, createRotatedVariants(variant, variant2)).with(createRotatedPillar()));
		this.registerSimpleItemModel(Blocks.INFESTED_DEEPSLATE, resourceLocation);
	}

	private void createNetherRoots(Block block, Block block2) {
		this.createCrossBlockWithDefaultItem(block, BlockModelGenerators.PlantType.NOT_TINTED);
		TextureMapping textureMapping = TextureMapping.plant(TextureMapping.getBlockTexture(block, "_pot"));
		MultiVariant multiVariant = plainVariant(BlockModelGenerators.PlantType.NOT_TINTED.getCrossPot().create(block2, textureMapping, this.modelOutput));
		this.blockStateOutput.accept(createSimpleBlock(block2, multiVariant));
	}

	private void createRespawnAnchor() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_bottom");
		ResourceLocation resourceLocation2 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top_off");
		ResourceLocation resourceLocation3 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top");
		ResourceLocation[] resourceLocations = new ResourceLocation[5];

		for (int i = 0; i < 5; i++) {
			TextureMapping textureMapping = new TextureMapping()
				.put(TextureSlot.BOTTOM, resourceLocation)
				.put(TextureSlot.TOP, i == 0 ? resourceLocation2 : resourceLocation3)
				.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_side" + i));
			resourceLocations[i] = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.RESPAWN_ANCHOR, "_" + i, textureMapping, this.modelOutput);
		}

		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.RESPAWN_ANCHOR)
					.with(PropertyDispatch.initial(BlockStateProperties.RESPAWN_ANCHOR_CHARGES).generate(integer -> plainVariant(resourceLocations[integer])))
			);
		this.registerSimpleItemModel(Blocks.RESPAWN_ANCHOR, resourceLocations[0]);
	}

	private static VariantMutator applyRotation(FrontAndTop frontAndTop) {
		return switch (frontAndTop) {
			case DOWN_NORTH -> X_ROT_90;
			case DOWN_SOUTH -> X_ROT_90.then(Y_ROT_180);
			case DOWN_WEST -> X_ROT_90.then(Y_ROT_270);
			case DOWN_EAST -> X_ROT_90.then(Y_ROT_90);
			case UP_NORTH -> X_ROT_270.then(Y_ROT_180);
			case UP_SOUTH -> X_ROT_270;
			case UP_WEST -> X_ROT_270.then(Y_ROT_90);
			case UP_EAST -> X_ROT_270.then(Y_ROT_270);
			case NORTH_UP -> NOP;
			case SOUTH_UP -> Y_ROT_180;
			case WEST_UP -> Y_ROT_270;
			case EAST_UP -> Y_ROT_90;
		};
	}

	private void createJigsaw() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_top");
		ResourceLocation resourceLocation2 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_bottom");
		ResourceLocation resourceLocation3 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_side");
		ResourceLocation resourceLocation4 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_lock");
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.DOWN, resourceLocation3)
			.put(TextureSlot.WEST, resourceLocation3)
			.put(TextureSlot.EAST, resourceLocation3)
			.put(TextureSlot.PARTICLE, resourceLocation)
			.put(TextureSlot.NORTH, resourceLocation)
			.put(TextureSlot.SOUTH, resourceLocation2)
			.put(TextureSlot.UP, resourceLocation4);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(Blocks.JIGSAW, plainVariant(ModelTemplates.CUBE_DIRECTIONAL.create(Blocks.JIGSAW, textureMapping, this.modelOutput)))
					.with(PropertyDispatch.modify(BlockStateProperties.ORIENTATION).generate(BlockModelGenerators::applyRotation))
			);
	}

	private void createPetrifiedOakSlab() {
		Block block = Blocks.OAK_PLANKS;
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.getModelLocation(block));
		TextureMapping textureMapping = TextureMapping.cube(block);
		Block block2 = Blocks.PETRIFIED_OAK_SLAB;
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.SLAB_BOTTOM.create(block2, textureMapping, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.SLAB_TOP.create(block2, textureMapping, this.modelOutput));
		this.blockStateOutput.accept(createSlab(block2, multiVariant2, multiVariant3, multiVariant));
	}

	private void createHead(Block block, Block block2, SkullBlock.Type type, ResourceLocation resourceLocation) {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.decorateBlockModelLocation("skull"));
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
		this.blockStateOutput.accept(createSimpleBlock(block2, multiVariant));
		if (type == SkullBlock.Types.PLAYER) {
			this.itemModelOutput.accept(block.asItem(), ItemModelUtils.specialModel(resourceLocation, new PlayerHeadSpecialRenderer.Unbaked()));
		} else {
			this.itemModelOutput.accept(block.asItem(), ItemModelUtils.specialModel(resourceLocation, new SkullSpecialRenderer.Unbaked(type)));
		}
	}

	private void createHeads() {
		ResourceLocation resourceLocation = ModelLocationUtils.decorateItemModelLocation("template_skull");
		this.createHead(Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, SkullBlock.Types.CREEPER, resourceLocation);
		this.createHead(Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, SkullBlock.Types.PLAYER, resourceLocation);
		this.createHead(Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, SkullBlock.Types.ZOMBIE, resourceLocation);
		this.createHead(Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, SkullBlock.Types.SKELETON, resourceLocation);
		this.createHead(Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, SkullBlock.Types.WITHER_SKELETON, resourceLocation);
		this.createHead(Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, SkullBlock.Types.PIGLIN, resourceLocation);
		this.createHead(Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, SkullBlock.Types.DRAGON, ModelLocationUtils.getModelLocation(Items.DRAGON_HEAD));
	}

	private void createBanner(Block block, Block block2, DyeColor dyeColor) {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.decorateBlockModelLocation("banner"));
		ResourceLocation resourceLocation = ModelLocationUtils.decorateItemModelLocation("template_banner");
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
		this.blockStateOutput.accept(createSimpleBlock(block2, multiVariant));
		Item item = block.asItem();
		this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourceLocation, new BannerSpecialRenderer.Unbaked(dyeColor)));
	}

	private void createBanners() {
		this.createBanner(Blocks.WHITE_BANNER, Blocks.WHITE_WALL_BANNER, DyeColor.WHITE);
		this.createBanner(Blocks.ORANGE_BANNER, Blocks.ORANGE_WALL_BANNER, DyeColor.ORANGE);
		this.createBanner(Blocks.MAGENTA_BANNER, Blocks.MAGENTA_WALL_BANNER, DyeColor.MAGENTA);
		this.createBanner(Blocks.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, DyeColor.LIGHT_BLUE);
		this.createBanner(Blocks.YELLOW_BANNER, Blocks.YELLOW_WALL_BANNER, DyeColor.YELLOW);
		this.createBanner(Blocks.LIME_BANNER, Blocks.LIME_WALL_BANNER, DyeColor.LIME);
		this.createBanner(Blocks.PINK_BANNER, Blocks.PINK_WALL_BANNER, DyeColor.PINK);
		this.createBanner(Blocks.GRAY_BANNER, Blocks.GRAY_WALL_BANNER, DyeColor.GRAY);
		this.createBanner(Blocks.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, DyeColor.LIGHT_GRAY);
		this.createBanner(Blocks.CYAN_BANNER, Blocks.CYAN_WALL_BANNER, DyeColor.CYAN);
		this.createBanner(Blocks.PURPLE_BANNER, Blocks.PURPLE_WALL_BANNER, DyeColor.PURPLE);
		this.createBanner(Blocks.BLUE_BANNER, Blocks.BLUE_WALL_BANNER, DyeColor.BLUE);
		this.createBanner(Blocks.BROWN_BANNER, Blocks.BROWN_WALL_BANNER, DyeColor.BROWN);
		this.createBanner(Blocks.GREEN_BANNER, Blocks.GREEN_WALL_BANNER, DyeColor.GREEN);
		this.createBanner(Blocks.RED_BANNER, Blocks.RED_WALL_BANNER, DyeColor.RED);
		this.createBanner(Blocks.BLACK_BANNER, Blocks.BLACK_WALL_BANNER, DyeColor.BLACK);
	}

	private void createChest(Block block, Block block2, ResourceLocation resourceLocation, boolean bl) {
		this.createParticleOnlyBlock(block, block2);
		Item item = block.asItem();
		ResourceLocation resourceLocation2 = ModelTemplates.CHEST_INVENTORY.create(item, TextureMapping.particle(block2), this.modelOutput);
		ItemModel.Unbaked unbaked = ItemModelUtils.specialModel(resourceLocation2, new ChestSpecialRenderer.Unbaked(resourceLocation));
		if (bl) {
			ItemModel.Unbaked unbaked2 = ItemModelUtils.specialModel(resourceLocation2, new ChestSpecialRenderer.Unbaked(ChestSpecialRenderer.GIFT_CHEST_TEXTURE));
			this.itemModelOutput.accept(item, ItemModelUtils.isXmas(unbaked2, unbaked));
		} else {
			this.itemModelOutput.accept(item, unbaked);
		}
	}

	private void createChests() {
		this.createChest(Blocks.CHEST, Blocks.OAK_PLANKS, ChestSpecialRenderer.NORMAL_CHEST_TEXTURE, true);
		this.createChest(Blocks.TRAPPED_CHEST, Blocks.OAK_PLANKS, ChestSpecialRenderer.TRAPPED_CHEST_TEXTURE, true);
		this.createChest(Blocks.ENDER_CHEST, Blocks.OBSIDIAN, ChestSpecialRenderer.ENDER_CHEST_TEXTURE, false);
	}

	private void createBed(Block block, Block block2, DyeColor dyeColor) {
		MultiVariant multiVariant = plainVariant(ModelLocationUtils.decorateBlockModelLocation("bed"));
		this.blockStateOutput.accept(createSimpleBlock(block, multiVariant));
		Item item = block.asItem();
		ResourceLocation resourceLocation = ModelTemplates.BED_INVENTORY
			.create(ModelLocationUtils.getModelLocation(item), TextureMapping.particle(block2), this.modelOutput);
		this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourceLocation, new BedSpecialRenderer.Unbaked(dyeColor)));
	}

	private void createBeds() {
		this.createBed(Blocks.WHITE_BED, Blocks.WHITE_WOOL, DyeColor.WHITE);
		this.createBed(Blocks.ORANGE_BED, Blocks.ORANGE_WOOL, DyeColor.ORANGE);
		this.createBed(Blocks.MAGENTA_BED, Blocks.MAGENTA_WOOL, DyeColor.MAGENTA);
		this.createBed(Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL, DyeColor.LIGHT_BLUE);
		this.createBed(Blocks.YELLOW_BED, Blocks.YELLOW_WOOL, DyeColor.YELLOW);
		this.createBed(Blocks.LIME_BED, Blocks.LIME_WOOL, DyeColor.LIME);
		this.createBed(Blocks.PINK_BED, Blocks.PINK_WOOL, DyeColor.PINK);
		this.createBed(Blocks.GRAY_BED, Blocks.GRAY_WOOL, DyeColor.GRAY);
		this.createBed(Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL, DyeColor.LIGHT_GRAY);
		this.createBed(Blocks.CYAN_BED, Blocks.CYAN_WOOL, DyeColor.CYAN);
		this.createBed(Blocks.PURPLE_BED, Blocks.PURPLE_WOOL, DyeColor.PURPLE);
		this.createBed(Blocks.BLUE_BED, Blocks.BLUE_WOOL, DyeColor.BLUE);
		this.createBed(Blocks.BROWN_BED, Blocks.BROWN_WOOL, DyeColor.BROWN);
		this.createBed(Blocks.GREEN_BED, Blocks.GREEN_WOOL, DyeColor.GREEN);
		this.createBed(Blocks.RED_BED, Blocks.RED_WOOL, DyeColor.RED);
		this.createBed(Blocks.BLACK_BED, Blocks.BLACK_WOOL, DyeColor.BLACK);
	}

	private void generateSimpleSpecialItemModel(Block block, SpecialModelRenderer.Unbaked unbaked) {
		Item item = block.asItem();
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(item);
		this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourceLocation, unbaked));
	}

	public void run() {
		BlockFamilies.getAllFamilies()
			.filter(BlockFamily::shouldGenerateModel)
			.forEach(blockFamily -> this.family(blockFamily.getBaseBlock()).generateFor(blockFamily));
		this.family(Blocks.CUT_COPPER)
			.generateFor(BlockFamilies.CUT_COPPER)
			.donateModelTo(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)
			.donateModelTo(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER)
			.generateFor(BlockFamilies.WAXED_CUT_COPPER);
		this.family(Blocks.EXPOSED_CUT_COPPER)
			.generateFor(BlockFamilies.EXPOSED_CUT_COPPER)
			.donateModelTo(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)
			.donateModelTo(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER)
			.generateFor(BlockFamilies.WAXED_EXPOSED_CUT_COPPER);
		this.family(Blocks.WEATHERED_CUT_COPPER)
			.generateFor(BlockFamilies.WEATHERED_CUT_COPPER)
			.donateModelTo(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)
			.donateModelTo(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER)
			.generateFor(BlockFamilies.WAXED_WEATHERED_CUT_COPPER);
		this.family(Blocks.OXIDIZED_CUT_COPPER)
			.generateFor(BlockFamilies.OXIDIZED_CUT_COPPER)
			.donateModelTo(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)
			.donateModelTo(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER)
			.generateFor(BlockFamilies.WAXED_OXIDIZED_CUT_COPPER);
		this.createCopperBulb(Blocks.COPPER_BULB);
		this.createCopperBulb(Blocks.EXPOSED_COPPER_BULB);
		this.createCopperBulb(Blocks.WEATHERED_COPPER_BULB);
		this.createCopperBulb(Blocks.OXIDIZED_COPPER_BULB);
		this.copyCopperBulbModel(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB);
		this.copyCopperBulbModel(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB);
		this.copyCopperBulbModel(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB);
		this.copyCopperBulbModel(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB);
		this.createNonTemplateModelBlock(Blocks.AIR);
		this.createNonTemplateModelBlock(Blocks.CAVE_AIR, Blocks.AIR);
		this.createNonTemplateModelBlock(Blocks.VOID_AIR, Blocks.AIR);
		this.createNonTemplateModelBlock(Blocks.BEACON);
		this.createNonTemplateModelBlock(Blocks.CACTUS);
		this.createNonTemplateModelBlock(Blocks.BUBBLE_COLUMN, Blocks.WATER);
		this.createNonTemplateModelBlock(Blocks.DRAGON_EGG);
		this.createNonTemplateModelBlock(Blocks.DRIED_KELP_BLOCK);
		this.createNonTemplateModelBlock(Blocks.ENCHANTING_TABLE);
		this.createNonTemplateModelBlock(Blocks.FLOWER_POT);
		this.registerSimpleFlatItemModel(Items.FLOWER_POT);
		this.createNonTemplateModelBlock(Blocks.HONEY_BLOCK);
		this.createNonTemplateModelBlock(Blocks.WATER);
		this.createNonTemplateModelBlock(Blocks.LAVA);
		this.createNonTemplateModelBlock(Blocks.SLIME_BLOCK);
		this.registerSimpleFlatItemModel(Items.CHAIN);
		this.createCandleAndCandleCake(Blocks.WHITE_CANDLE, Blocks.WHITE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.ORANGE_CANDLE, Blocks.ORANGE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.MAGENTA_CANDLE, Blocks.MAGENTA_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_BLUE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.YELLOW_CANDLE, Blocks.YELLOW_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.LIME_CANDLE, Blocks.LIME_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.PINK_CANDLE, Blocks.PINK_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.GRAY_CANDLE, Blocks.GRAY_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.LIGHT_GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.CYAN_CANDLE, Blocks.CYAN_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.PURPLE_CANDLE, Blocks.PURPLE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.BLUE_CANDLE, Blocks.BLUE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.BROWN_CANDLE, Blocks.BROWN_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.GREEN_CANDLE, Blocks.GREEN_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.RED_CANDLE, Blocks.RED_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.BLACK_CANDLE, Blocks.BLACK_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.CANDLE, Blocks.CANDLE_CAKE);
		this.createNonTemplateModelBlock(Blocks.POTTED_BAMBOO);
		this.createNonTemplateModelBlock(Blocks.POTTED_CACTUS);
		this.createNonTemplateModelBlock(Blocks.POWDER_SNOW);
		this.createNonTemplateModelBlock(Blocks.SPORE_BLOSSOM);
		this.createAzalea(Blocks.AZALEA);
		this.createAzalea(Blocks.FLOWERING_AZALEA);
		this.createPottedAzalea(Blocks.POTTED_AZALEA);
		this.createPottedAzalea(Blocks.POTTED_FLOWERING_AZALEA);
		this.createCaveVines();
		this.createFullAndCarpetBlocks(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET);
		this.createMossyCarpet(Blocks.PALE_MOSS_CARPET);
		this.createHangingMoss(Blocks.PALE_HANGING_MOSS);
		this.createTrivialCube(Blocks.PALE_MOSS_BLOCK);
		this.createFlowerBed(Blocks.PINK_PETALS);
		this.createFlowerBed(Blocks.WILDFLOWERS);
		this.createLeafLitter(Blocks.LEAF_LITTER);
		this.createCrossBlock(Blocks.FIREFLY_BUSH, BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
		this.registerSimpleFlatItemModel(Items.FIREFLY_BUSH);
		this.createAirLikeBlock(Blocks.BARRIER, Items.BARRIER);
		this.registerSimpleFlatItemModel(Items.BARRIER);
		this.createLightBlock();
		this.createAirLikeBlock(Blocks.STRUCTURE_VOID, Items.STRUCTURE_VOID);
		this.registerSimpleFlatItemModel(Items.STRUCTURE_VOID);
		this.createAirLikeBlock(Blocks.MOVING_PISTON, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
		this.createTrivialCube(Blocks.COAL_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_COAL_ORE);
		this.createTrivialCube(Blocks.COAL_BLOCK);
		this.createTrivialCube(Blocks.DIAMOND_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_DIAMOND_ORE);
		this.createTrivialCube(Blocks.DIAMOND_BLOCK);
		this.createTrivialCube(Blocks.EMERALD_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_EMERALD_ORE);
		this.createTrivialCube(Blocks.EMERALD_BLOCK);
		this.createTrivialCube(Blocks.GOLD_ORE);
		this.createTrivialCube(Blocks.NETHER_GOLD_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_GOLD_ORE);
		this.createTrivialCube(Blocks.GOLD_BLOCK);
		this.createTrivialCube(Blocks.IRON_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_IRON_ORE);
		this.createTrivialCube(Blocks.IRON_BLOCK);
		this.createTrivialBlock(Blocks.ANCIENT_DEBRIS, TexturedModel.COLUMN);
		this.createTrivialCube(Blocks.NETHERITE_BLOCK);
		this.createTrivialCube(Blocks.LAPIS_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_LAPIS_ORE);
		this.createTrivialCube(Blocks.LAPIS_BLOCK);
		this.createTrivialCube(Blocks.RESIN_BLOCK);
		this.createTrivialCube(Blocks.NETHER_QUARTZ_ORE);
		this.createTrivialCube(Blocks.REDSTONE_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_REDSTONE_ORE);
		this.createTrivialCube(Blocks.REDSTONE_BLOCK);
		this.createTrivialCube(Blocks.GILDED_BLACKSTONE);
		this.createTrivialCube(Blocks.BLUE_ICE);
		this.createTrivialCube(Blocks.CLAY);
		this.createTrivialCube(Blocks.COARSE_DIRT);
		this.createTrivialCube(Blocks.CRYING_OBSIDIAN);
		this.createTrivialCube(Blocks.END_STONE);
		this.createTrivialCube(Blocks.GLOWSTONE);
		this.createTrivialCube(Blocks.GRAVEL);
		this.createTrivialCube(Blocks.HONEYCOMB_BLOCK);
		this.createTrivialCube(Blocks.ICE);
		this.createTrivialBlock(Blocks.JUKEBOX, TexturedModel.CUBE_TOP);
		this.createTrivialBlock(Blocks.LODESTONE, TexturedModel.COLUMN);
		this.createTrivialBlock(Blocks.MELON, TexturedModel.COLUMN);
		this.createNonTemplateModelBlock(Blocks.MANGROVE_ROOTS);
		this.createNonTemplateModelBlock(Blocks.POTTED_MANGROVE_PROPAGULE);
		this.createTrivialCube(Blocks.NETHER_WART_BLOCK);
		this.createTrivialCube(Blocks.NOTE_BLOCK);
		this.createTrivialCube(Blocks.PACKED_ICE);
		this.createTrivialCube(Blocks.OBSIDIAN);
		this.createTrivialCube(Blocks.QUARTZ_BRICKS);
		this.createTrivialCube(Blocks.SEA_LANTERN);
		this.createTrivialCube(Blocks.SHROOMLIGHT);
		this.createTrivialCube(Blocks.SOUL_SAND);
		this.createTrivialCube(Blocks.SOUL_SOIL);
		this.createTrivialBlock(Blocks.SPAWNER, TexturedModel.CUBE_INNER_FACES);
		this.createCreakingHeart(Blocks.CREAKING_HEART);
		this.createTrivialCube(Blocks.SPONGE);
		this.createTrivialBlock(Blocks.SEAGRASS, TexturedModel.SEAGRASS);
		this.registerSimpleFlatItemModel(Items.SEAGRASS);
		this.createTrivialBlock(Blocks.TNT, TexturedModel.CUBE_TOP_BOTTOM);
		this.createTrivialBlock(Blocks.TARGET, TexturedModel.COLUMN);
		this.createTrivialCube(Blocks.WARPED_WART_BLOCK);
		this.createTrivialCube(Blocks.WET_SPONGE);
		this.createTrivialCube(Blocks.AMETHYST_BLOCK);
		this.createTrivialCube(Blocks.BUDDING_AMETHYST);
		this.createTrivialCube(Blocks.CALCITE);
		this.createTrivialCube(Blocks.DRIPSTONE_BLOCK);
		this.createTrivialCube(Blocks.RAW_IRON_BLOCK);
		this.createTrivialCube(Blocks.RAW_COPPER_BLOCK);
		this.createTrivialCube(Blocks.RAW_GOLD_BLOCK);
		this.createRotatedMirroredVariantBlock(Blocks.SCULK);
		this.createNonTemplateModelBlock(Blocks.HEAVY_CORE);
		this.createPetrifiedOakSlab();
		this.createTrivialCube(Blocks.COPPER_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_COPPER_ORE);
		this.createTrivialCube(Blocks.COPPER_BLOCK);
		this.createTrivialCube(Blocks.EXPOSED_COPPER);
		this.createTrivialCube(Blocks.WEATHERED_COPPER);
		this.createTrivialCube(Blocks.OXIDIZED_COPPER);
		this.copyModel(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK);
		this.copyModel(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER);
		this.copyModel(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER);
		this.copyModel(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
		this.createDoor(Blocks.COPPER_DOOR);
		this.createDoor(Blocks.EXPOSED_COPPER_DOOR);
		this.createDoor(Blocks.WEATHERED_COPPER_DOOR);
		this.createDoor(Blocks.OXIDIZED_COPPER_DOOR);
		this.copyDoorModel(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR);
		this.copyDoorModel(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR);
		this.copyDoorModel(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR);
		this.copyDoorModel(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR);
		this.createTrapdoor(Blocks.COPPER_TRAPDOOR);
		this.createTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR);
		this.createTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR);
		this.createTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR);
		this.copyTrapdoorModel(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR);
		this.copyTrapdoorModel(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
		this.copyTrapdoorModel(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
		this.copyTrapdoorModel(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);
		this.createTrivialCube(Blocks.COPPER_GRATE);
		this.createTrivialCube(Blocks.EXPOSED_COPPER_GRATE);
		this.createTrivialCube(Blocks.WEATHERED_COPPER_GRATE);
		this.createTrivialCube(Blocks.OXIDIZED_COPPER_GRATE);
		this.copyModel(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE);
		this.copyModel(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE);
		this.copyModel(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE);
		this.copyModel(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE);
		this.createWeightedPressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.GOLD_BLOCK);
		this.createWeightedPressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.IRON_BLOCK);
		this.createAmethystClusters();
		this.createBookshelf();
		this.createChiseledBookshelf();
		this.createBrewingStand();
		this.createCakeBlock();
		this.createCampfires(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
		this.createCartographyTable();
		this.createCauldrons();
		this.createChorusFlower();
		this.createChorusPlant();
		this.createComposter();
		this.createDaylightDetector();
		this.createEndPortalFrame();
		this.createRotatableColumn(Blocks.END_ROD);
		this.createLightningRod();
		this.createFarmland();
		this.createFire();
		this.createSoulFire();
		this.createFrostedIce();
		this.createGrassBlocks();
		this.createCocoa();
		this.createDirtPath();
		this.createGrindstone();
		this.createHopper();
		this.createIronBars();
		this.createLever();
		this.createLilyPad();
		this.createNetherPortalBlock();
		this.createNetherrack();
		this.createObserver();
		this.createPistons();
		this.createPistonHeads();
		this.createScaffolding();
		this.createRedstoneTorch();
		this.createRedstoneLamp();
		this.createRepeater();
		this.createSeaPickle();
		this.createSmithingTable();
		this.createSnowBlocks();
		this.createStonecutter();
		this.createStructureBlock();
		this.createSweetBerryBush();
		this.createTestBlock();
		this.createTrivialCube(Blocks.TEST_INSTANCE_BLOCK);
		this.createTripwire();
		this.createTripwireHook();
		this.createTurtleEgg();
		this.createSnifferEgg();
		this.createDriedGhastBlock();
		this.createVine();
		this.createMultiface(Blocks.GLOW_LICHEN);
		this.createMultiface(Blocks.SCULK_VEIN);
		this.createMultiface(Blocks.RESIN_CLUMP, Items.RESIN_CLUMP);
		this.createMagmaBlock();
		this.createJigsaw();
		this.createSculkSensor();
		this.createCalibratedSculkSensor();
		this.createSculkShrieker();
		this.createFrogspawnBlock();
		this.createMangrovePropagule();
		this.createMuddyMangroveRoots();
		this.createTrialSpawner();
		this.createVault();
		this.createNonTemplateHorizontalBlock(Blocks.LADDER);
		this.registerSimpleFlatItemModel(Blocks.LADDER);
		this.createNonTemplateHorizontalBlock(Blocks.LECTERN);
		this.createBigDripLeafBlock();
		this.createNonTemplateHorizontalBlock(Blocks.BIG_DRIPLEAF_STEM);
		this.createNormalTorch(Blocks.TORCH, Blocks.WALL_TORCH);
		this.createNormalTorch(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);
		this.createCraftingTableLike(Blocks.CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
		this.createCraftingTableLike(Blocks.FLETCHING_TABLE, Blocks.BIRCH_PLANKS, TextureMapping::fletchingTable);
		this.createNyliumBlock(Blocks.CRIMSON_NYLIUM);
		this.createNyliumBlock(Blocks.WARPED_NYLIUM);
		this.createDispenserBlock(Blocks.DISPENSER);
		this.createDispenserBlock(Blocks.DROPPER);
		this.createCrafterBlock();
		this.createLantern(Blocks.LANTERN);
		this.createLantern(Blocks.SOUL_LANTERN);
		this.createAxisAlignedPillarBlockCustomModel(Blocks.CHAIN, plainVariant(ModelLocationUtils.getModelLocation(Blocks.CHAIN)));
		this.createAxisAlignedPillarBlock(Blocks.BASALT, TexturedModel.COLUMN);
		this.createAxisAlignedPillarBlock(Blocks.POLISHED_BASALT, TexturedModel.COLUMN);
		this.createTrivialCube(Blocks.SMOOTH_BASALT);
		this.createAxisAlignedPillarBlock(Blocks.BONE_BLOCK, TexturedModel.COLUMN);
		this.createRotatedVariantBlock(Blocks.DIRT);
		this.createRotatedVariantBlock(Blocks.ROOTED_DIRT);
		this.createRotatedVariantBlock(Blocks.SAND);
		this.createBrushableBlock(Blocks.SUSPICIOUS_SAND);
		this.createBrushableBlock(Blocks.SUSPICIOUS_GRAVEL);
		this.createRotatedVariantBlock(Blocks.RED_SAND);
		this.createRotatedMirroredVariantBlock(Blocks.BEDROCK);
		this.createTrivialBlock(Blocks.REINFORCED_DEEPSLATE, TexturedModel.CUBE_TOP_BOTTOM);
		this.createRotatedPillarWithHorizontalVariant(Blocks.HAY_BLOCK, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createRotatedPillarWithHorizontalVariant(Blocks.PURPUR_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
		this.createRotatedPillarWithHorizontalVariant(Blocks.QUARTZ_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
		this.createRotatedPillarWithHorizontalVariant(Blocks.OCHRE_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createRotatedPillarWithHorizontalVariant(Blocks.VERDANT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createRotatedPillarWithHorizontalVariant(Blocks.PEARLESCENT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createHorizontallyRotatedBlock(Blocks.LOOM, TexturedModel.ORIENTABLE);
		this.createPumpkins();
		this.createBeeNest(Blocks.BEE_NEST, TextureMapping::orientableCube);
		this.createBeeNest(Blocks.BEEHIVE, TextureMapping::orientableCubeSameEnds);
		this.createCropBlock(Blocks.BEETROOTS, BlockStateProperties.AGE_3, 0, 1, 2, 3);
		this.createCropBlock(Blocks.CARROTS, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
		this.createCropBlock(Blocks.NETHER_WART, BlockStateProperties.AGE_3, 0, 1, 1, 2);
		this.createCropBlock(Blocks.POTATOES, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
		this.createCropBlock(Blocks.WHEAT, BlockStateProperties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
		this.createCrossBlock(Blocks.TORCHFLOWER_CROP, BlockModelGenerators.PlantType.NOT_TINTED, BlockStateProperties.AGE_1, 0, 1);
		this.createPitcherCrop();
		this.createPitcherPlant();
		this.createBanners();
		this.createBeds();
		this.createHeads();
		this.createChests();
		this.createShulkerBox(Blocks.SHULKER_BOX, null);
		this.createShulkerBox(Blocks.WHITE_SHULKER_BOX, DyeColor.WHITE);
		this.createShulkerBox(Blocks.ORANGE_SHULKER_BOX, DyeColor.ORANGE);
		this.createShulkerBox(Blocks.MAGENTA_SHULKER_BOX, DyeColor.MAGENTA);
		this.createShulkerBox(Blocks.LIGHT_BLUE_SHULKER_BOX, DyeColor.LIGHT_BLUE);
		this.createShulkerBox(Blocks.YELLOW_SHULKER_BOX, DyeColor.YELLOW);
		this.createShulkerBox(Blocks.LIME_SHULKER_BOX, DyeColor.LIME);
		this.createShulkerBox(Blocks.PINK_SHULKER_BOX, DyeColor.PINK);
		this.createShulkerBox(Blocks.GRAY_SHULKER_BOX, DyeColor.GRAY);
		this.createShulkerBox(Blocks.LIGHT_GRAY_SHULKER_BOX, DyeColor.LIGHT_GRAY);
		this.createShulkerBox(Blocks.CYAN_SHULKER_BOX, DyeColor.CYAN);
		this.createShulkerBox(Blocks.PURPLE_SHULKER_BOX, DyeColor.PURPLE);
		this.createShulkerBox(Blocks.BLUE_SHULKER_BOX, DyeColor.BLUE);
		this.createShulkerBox(Blocks.BROWN_SHULKER_BOX, DyeColor.BROWN);
		this.createShulkerBox(Blocks.GREEN_SHULKER_BOX, DyeColor.GREEN);
		this.createShulkerBox(Blocks.RED_SHULKER_BOX, DyeColor.RED);
		this.createShulkerBox(Blocks.BLACK_SHULKER_BOX, DyeColor.BLACK);
		this.createParticleOnlyBlock(Blocks.CONDUIT);
		this.generateSimpleSpecialItemModel(Blocks.CONDUIT, new ConduitSpecialRenderer.Unbaked());
		this.createParticleOnlyBlock(Blocks.DECORATED_POT, Blocks.TERRACOTTA);
		this.generateSimpleSpecialItemModel(Blocks.DECORATED_POT, new DecoratedPotSpecialRenderer.Unbaked());
		this.createParticleOnlyBlock(Blocks.END_PORTAL, Blocks.OBSIDIAN);
		this.createParticleOnlyBlock(Blocks.END_GATEWAY, Blocks.OBSIDIAN);
		this.createTrivialCube(Blocks.AZALEA_LEAVES);
		this.createTrivialCube(Blocks.FLOWERING_AZALEA_LEAVES);
		this.createTrivialCube(Blocks.WHITE_CONCRETE);
		this.createTrivialCube(Blocks.ORANGE_CONCRETE);
		this.createTrivialCube(Blocks.MAGENTA_CONCRETE);
		this.createTrivialCube(Blocks.LIGHT_BLUE_CONCRETE);
		this.createTrivialCube(Blocks.YELLOW_CONCRETE);
		this.createTrivialCube(Blocks.LIME_CONCRETE);
		this.createTrivialCube(Blocks.PINK_CONCRETE);
		this.createTrivialCube(Blocks.GRAY_CONCRETE);
		this.createTrivialCube(Blocks.LIGHT_GRAY_CONCRETE);
		this.createTrivialCube(Blocks.CYAN_CONCRETE);
		this.createTrivialCube(Blocks.PURPLE_CONCRETE);
		this.createTrivialCube(Blocks.BLUE_CONCRETE);
		this.createTrivialCube(Blocks.BROWN_CONCRETE);
		this.createTrivialCube(Blocks.GREEN_CONCRETE);
		this.createTrivialCube(Blocks.RED_CONCRETE);
		this.createTrivialCube(Blocks.BLACK_CONCRETE);
		this.createColoredBlockWithRandomRotations(
			TexturedModel.CUBE,
			Blocks.WHITE_CONCRETE_POWDER,
			Blocks.ORANGE_CONCRETE_POWDER,
			Blocks.MAGENTA_CONCRETE_POWDER,
			Blocks.LIGHT_BLUE_CONCRETE_POWDER,
			Blocks.YELLOW_CONCRETE_POWDER,
			Blocks.LIME_CONCRETE_POWDER,
			Blocks.PINK_CONCRETE_POWDER,
			Blocks.GRAY_CONCRETE_POWDER,
			Blocks.LIGHT_GRAY_CONCRETE_POWDER,
			Blocks.CYAN_CONCRETE_POWDER,
			Blocks.PURPLE_CONCRETE_POWDER,
			Blocks.BLUE_CONCRETE_POWDER,
			Blocks.BROWN_CONCRETE_POWDER,
			Blocks.GREEN_CONCRETE_POWDER,
			Blocks.RED_CONCRETE_POWDER,
			Blocks.BLACK_CONCRETE_POWDER
		);
		this.createTrivialCube(Blocks.TERRACOTTA);
		this.createTrivialCube(Blocks.WHITE_TERRACOTTA);
		this.createTrivialCube(Blocks.ORANGE_TERRACOTTA);
		this.createTrivialCube(Blocks.MAGENTA_TERRACOTTA);
		this.createTrivialCube(Blocks.LIGHT_BLUE_TERRACOTTA);
		this.createTrivialCube(Blocks.YELLOW_TERRACOTTA);
		this.createTrivialCube(Blocks.LIME_TERRACOTTA);
		this.createTrivialCube(Blocks.PINK_TERRACOTTA);
		this.createTrivialCube(Blocks.GRAY_TERRACOTTA);
		this.createTrivialCube(Blocks.LIGHT_GRAY_TERRACOTTA);
		this.createTrivialCube(Blocks.CYAN_TERRACOTTA);
		this.createTrivialCube(Blocks.PURPLE_TERRACOTTA);
		this.createTrivialCube(Blocks.BLUE_TERRACOTTA);
		this.createTrivialCube(Blocks.BROWN_TERRACOTTA);
		this.createTrivialCube(Blocks.GREEN_TERRACOTTA);
		this.createTrivialCube(Blocks.RED_TERRACOTTA);
		this.createTrivialCube(Blocks.BLACK_TERRACOTTA);
		this.createTrivialCube(Blocks.TINTED_GLASS);
		this.createGlassBlocks(Blocks.GLASS, Blocks.GLASS_PANE);
		this.createGlassBlocks(Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE);
		this.createColoredBlockWithStateRotations(
			TexturedModel.GLAZED_TERRACOTTA,
			Blocks.WHITE_GLAZED_TERRACOTTA,
			Blocks.ORANGE_GLAZED_TERRACOTTA,
			Blocks.MAGENTA_GLAZED_TERRACOTTA,
			Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
			Blocks.YELLOW_GLAZED_TERRACOTTA,
			Blocks.LIME_GLAZED_TERRACOTTA,
			Blocks.PINK_GLAZED_TERRACOTTA,
			Blocks.GRAY_GLAZED_TERRACOTTA,
			Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA,
			Blocks.CYAN_GLAZED_TERRACOTTA,
			Blocks.PURPLE_GLAZED_TERRACOTTA,
			Blocks.BLUE_GLAZED_TERRACOTTA,
			Blocks.BROWN_GLAZED_TERRACOTTA,
			Blocks.GREEN_GLAZED_TERRACOTTA,
			Blocks.RED_GLAZED_TERRACOTTA,
			Blocks.BLACK_GLAZED_TERRACOTTA
		);
		this.createFullAndCarpetBlocks(Blocks.WHITE_WOOL, Blocks.WHITE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.ORANGE_WOOL, Blocks.ORANGE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.MAGENTA_WOOL, Blocks.MAGENTA_CARPET);
		this.createFullAndCarpetBlocks(Blocks.LIGHT_BLUE_WOOL, Blocks.LIGHT_BLUE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
		this.createFullAndCarpetBlocks(Blocks.LIME_WOOL, Blocks.LIME_CARPET);
		this.createFullAndCarpetBlocks(Blocks.PINK_WOOL, Blocks.PINK_CARPET);
		this.createFullAndCarpetBlocks(Blocks.GRAY_WOOL, Blocks.GRAY_CARPET);
		this.createFullAndCarpetBlocks(Blocks.LIGHT_GRAY_WOOL, Blocks.LIGHT_GRAY_CARPET);
		this.createFullAndCarpetBlocks(Blocks.CYAN_WOOL, Blocks.CYAN_CARPET);
		this.createFullAndCarpetBlocks(Blocks.PURPLE_WOOL, Blocks.PURPLE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.BROWN_WOOL, Blocks.BROWN_CARPET);
		this.createFullAndCarpetBlocks(Blocks.GREEN_WOOL, Blocks.GREEN_CARPET);
		this.createFullAndCarpetBlocks(Blocks.RED_WOOL, Blocks.RED_CARPET);
		this.createFullAndCarpetBlocks(Blocks.BLACK_WOOL, Blocks.BLACK_CARPET);
		this.createTrivialCube(Blocks.MUD);
		this.createTrivialCube(Blocks.PACKED_MUD);
		this.createPlant(Blocks.FERN, Blocks.POTTED_FERN, BlockModelGenerators.PlantType.TINTED);
		this.createItemWithGrassTint(Blocks.FERN);
		this.createPlantWithDefaultItem(Blocks.DANDELION, Blocks.POTTED_DANDELION, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.POPPY, Blocks.POTTED_POPPY, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.OPEN_EYEBLOSSOM, Blocks.POTTED_OPEN_EYEBLOSSOM, BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.CLOSED_EYEBLOSSOM, Blocks.POTTED_CLOSED_EYEBLOSSOM, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.BLUE_ORCHID, Blocks.POTTED_BLUE_ORCHID, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.ALLIUM, Blocks.POTTED_ALLIUM, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.AZURE_BLUET, Blocks.POTTED_AZURE_BLUET, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.RED_TULIP, Blocks.POTTED_RED_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.ORANGE_TULIP, Blocks.POTTED_ORANGE_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.WHITE_TULIP, Blocks.POTTED_WHITE_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.PINK_TULIP, Blocks.POTTED_PINK_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.OXEYE_DAISY, Blocks.POTTED_OXEYE_DAISY, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.CORNFLOWER, Blocks.POTTED_CORNFLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.LILY_OF_THE_VALLEY, Blocks.POTTED_LILY_OF_THE_VALLEY, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.RED_MUSHROOM, Blocks.POTTED_RED_MUSHROOM, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.BROWN_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.DEAD_BUSH, Blocks.POTTED_DEAD_BUSH, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPlantWithDefaultItem(Blocks.TORCHFLOWER, Blocks.POTTED_TORCHFLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createPointedDripstone();
		this.createMushroomBlock(Blocks.BROWN_MUSHROOM_BLOCK);
		this.createMushroomBlock(Blocks.RED_MUSHROOM_BLOCK);
		this.createMushroomBlock(Blocks.MUSHROOM_STEM);
		this.createCrossBlock(Blocks.SHORT_GRASS, BlockModelGenerators.PlantType.TINTED);
		this.createItemWithGrassTint(Blocks.SHORT_GRASS);
		this.createCrossBlockWithDefaultItem(Blocks.SHORT_DRY_GRASS, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createCrossBlockWithDefaultItem(Blocks.TALL_DRY_GRASS, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createCrossBlock(Blocks.BUSH, BlockModelGenerators.PlantType.TINTED);
		this.createItemWithGrassTint(Blocks.BUSH);
		this.createCrossBlock(Blocks.SUGAR_CANE, BlockModelGenerators.PlantType.TINTED);
		this.registerSimpleFlatItemModel(Items.SUGAR_CANE);
		this.createGrowingPlant(Blocks.KELP, Blocks.KELP_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
		this.registerSimpleFlatItemModel(Items.KELP);
		this.createCrossBlock(Blocks.HANGING_ROOTS, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createGrowingPlant(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createGrowingPlant(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
		this.registerSimpleFlatItemModel(Blocks.WEEPING_VINES, "_plant");
		this.registerSimpleFlatItemModel(Blocks.TWISTING_VINES, "_plant");
		this.createCrossBlockWithDefaultItem(
			Blocks.BAMBOO_SAPLING, BlockModelGenerators.PlantType.TINTED, TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.BAMBOO, "_stage0"))
		);
		this.createBamboo();
		this.createCrossBlockWithDefaultItem(Blocks.CACTUS_FLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createCrossBlockWithDefaultItem(Blocks.COBWEB, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createDoublePlantWithDefaultItem(Blocks.LILAC, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createDoublePlantWithDefaultItem(Blocks.ROSE_BUSH, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createDoublePlantWithDefaultItem(Blocks.PEONY, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTintedDoublePlant(Blocks.TALL_GRASS);
		this.createTintedDoublePlant(Blocks.LARGE_FERN);
		this.createSunflower();
		this.createTallSeagrass();
		this.createSmallDripleaf();
		this.createCoral(
			Blocks.TUBE_CORAL,
			Blocks.DEAD_TUBE_CORAL,
			Blocks.TUBE_CORAL_BLOCK,
			Blocks.DEAD_TUBE_CORAL_BLOCK,
			Blocks.TUBE_CORAL_FAN,
			Blocks.DEAD_TUBE_CORAL_FAN,
			Blocks.TUBE_CORAL_WALL_FAN,
			Blocks.DEAD_TUBE_CORAL_WALL_FAN
		);
		this.createCoral(
			Blocks.BRAIN_CORAL,
			Blocks.DEAD_BRAIN_CORAL,
			Blocks.BRAIN_CORAL_BLOCK,
			Blocks.DEAD_BRAIN_CORAL_BLOCK,
			Blocks.BRAIN_CORAL_FAN,
			Blocks.DEAD_BRAIN_CORAL_FAN,
			Blocks.BRAIN_CORAL_WALL_FAN,
			Blocks.DEAD_BRAIN_CORAL_WALL_FAN
		);
		this.createCoral(
			Blocks.BUBBLE_CORAL,
			Blocks.DEAD_BUBBLE_CORAL,
			Blocks.BUBBLE_CORAL_BLOCK,
			Blocks.DEAD_BUBBLE_CORAL_BLOCK,
			Blocks.BUBBLE_CORAL_FAN,
			Blocks.DEAD_BUBBLE_CORAL_FAN,
			Blocks.BUBBLE_CORAL_WALL_FAN,
			Blocks.DEAD_BUBBLE_CORAL_WALL_FAN
		);
		this.createCoral(
			Blocks.FIRE_CORAL,
			Blocks.DEAD_FIRE_CORAL,
			Blocks.FIRE_CORAL_BLOCK,
			Blocks.DEAD_FIRE_CORAL_BLOCK,
			Blocks.FIRE_CORAL_FAN,
			Blocks.DEAD_FIRE_CORAL_FAN,
			Blocks.FIRE_CORAL_WALL_FAN,
			Blocks.DEAD_FIRE_CORAL_WALL_FAN
		);
		this.createCoral(
			Blocks.HORN_CORAL,
			Blocks.DEAD_HORN_CORAL,
			Blocks.HORN_CORAL_BLOCK,
			Blocks.DEAD_HORN_CORAL_BLOCK,
			Blocks.HORN_CORAL_FAN,
			Blocks.DEAD_HORN_CORAL_FAN,
			Blocks.HORN_CORAL_WALL_FAN,
			Blocks.DEAD_HORN_CORAL_WALL_FAN
		);
		this.createStems(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM);
		this.createStems(Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
		this.woodProvider(Blocks.MANGROVE_LOG).logWithHorizontal(Blocks.MANGROVE_LOG).wood(Blocks.MANGROVE_WOOD);
		this.woodProvider(Blocks.STRIPPED_MANGROVE_LOG).logWithHorizontal(Blocks.STRIPPED_MANGROVE_LOG).wood(Blocks.STRIPPED_MANGROVE_WOOD);
		this.createHangingSign(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN);
		this.createTintedLeaves(Blocks.MANGROVE_LEAVES, TexturedModel.LEAVES, -7158200);
		this.woodProvider(Blocks.ACACIA_LOG).logWithHorizontal(Blocks.ACACIA_LOG).wood(Blocks.ACACIA_WOOD);
		this.woodProvider(Blocks.STRIPPED_ACACIA_LOG).logWithHorizontal(Blocks.STRIPPED_ACACIA_LOG).wood(Blocks.STRIPPED_ACACIA_WOOD);
		this.createHangingSign(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTintedLeaves(Blocks.ACACIA_LEAVES, TexturedModel.LEAVES, -12012264);
		this.woodProvider(Blocks.CHERRY_LOG).logUVLocked(Blocks.CHERRY_LOG).wood(Blocks.CHERRY_WOOD);
		this.woodProvider(Blocks.STRIPPED_CHERRY_LOG).logUVLocked(Blocks.STRIPPED_CHERRY_LOG).wood(Blocks.STRIPPED_CHERRY_WOOD);
		this.createHangingSign(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.CHERRY_SAPLING, Blocks.POTTED_CHERRY_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTrivialBlock(Blocks.CHERRY_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.BIRCH_LOG).logWithHorizontal(Blocks.BIRCH_LOG).wood(Blocks.BIRCH_WOOD);
		this.woodProvider(Blocks.STRIPPED_BIRCH_LOG).logWithHorizontal(Blocks.STRIPPED_BIRCH_LOG).wood(Blocks.STRIPPED_BIRCH_WOOD);
		this.createHangingSign(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTintedLeaves(Blocks.BIRCH_LEAVES, TexturedModel.LEAVES, -8345771);
		this.woodProvider(Blocks.OAK_LOG).logWithHorizontal(Blocks.OAK_LOG).wood(Blocks.OAK_WOOD);
		this.woodProvider(Blocks.STRIPPED_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_OAK_LOG).wood(Blocks.STRIPPED_OAK_WOOD);
		this.createHangingSign(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTintedLeaves(Blocks.OAK_LEAVES, TexturedModel.LEAVES, -12012264);
		this.woodProvider(Blocks.SPRUCE_LOG).logWithHorizontal(Blocks.SPRUCE_LOG).wood(Blocks.SPRUCE_WOOD);
		this.woodProvider(Blocks.STRIPPED_SPRUCE_LOG).logWithHorizontal(Blocks.STRIPPED_SPRUCE_LOG).wood(Blocks.STRIPPED_SPRUCE_WOOD);
		this.createHangingSign(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTintedLeaves(Blocks.SPRUCE_LEAVES, TexturedModel.LEAVES, -10380959);
		this.woodProvider(Blocks.DARK_OAK_LOG).logWithHorizontal(Blocks.DARK_OAK_LOG).wood(Blocks.DARK_OAK_WOOD);
		this.woodProvider(Blocks.STRIPPED_DARK_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_DARK_OAK_LOG).wood(Blocks.STRIPPED_DARK_OAK_WOOD);
		this.createHangingSign(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTintedLeaves(Blocks.DARK_OAK_LEAVES, TexturedModel.LEAVES, -12012264);
		this.woodProvider(Blocks.PALE_OAK_LOG).logWithHorizontal(Blocks.PALE_OAK_LOG).wood(Blocks.PALE_OAK_WOOD);
		this.woodProvider(Blocks.STRIPPED_PALE_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_PALE_OAK_LOG).wood(Blocks.STRIPPED_PALE_OAK_WOOD);
		this.createHangingSign(Blocks.STRIPPED_PALE_OAK_LOG, Blocks.PALE_OAK_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.PALE_OAK_SAPLING, Blocks.POTTED_PALE_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTrivialBlock(Blocks.PALE_OAK_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.JUNGLE_LOG).logWithHorizontal(Blocks.JUNGLE_LOG).wood(Blocks.JUNGLE_WOOD);
		this.woodProvider(Blocks.STRIPPED_JUNGLE_LOG).logWithHorizontal(Blocks.STRIPPED_JUNGLE_LOG).wood(Blocks.STRIPPED_JUNGLE_WOOD);
		this.createHangingSign(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createTintedLeaves(Blocks.JUNGLE_LEAVES, TexturedModel.LEAVES, -12012264);
		this.woodProvider(Blocks.CRIMSON_STEM).log(Blocks.CRIMSON_STEM).wood(Blocks.CRIMSON_HYPHAE);
		this.woodProvider(Blocks.STRIPPED_CRIMSON_STEM).log(Blocks.STRIPPED_CRIMSON_STEM).wood(Blocks.STRIPPED_CRIMSON_HYPHAE);
		this.createHangingSign(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createNetherRoots(Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS);
		this.woodProvider(Blocks.WARPED_STEM).log(Blocks.WARPED_STEM).wood(Blocks.WARPED_HYPHAE);
		this.woodProvider(Blocks.STRIPPED_WARPED_STEM).log(Blocks.STRIPPED_WARPED_STEM).wood(Blocks.STRIPPED_WARPED_HYPHAE);
		this.createHangingSign(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN);
		this.createPlantWithDefaultItem(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, BlockModelGenerators.PlantType.NOT_TINTED);
		this.createNetherRoots(Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
		this.woodProvider(Blocks.BAMBOO_BLOCK).logUVLocked(Blocks.BAMBOO_BLOCK);
		this.woodProvider(Blocks.STRIPPED_BAMBOO_BLOCK).logUVLocked(Blocks.STRIPPED_BAMBOO_BLOCK);
		this.createHangingSign(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
		this.createCrossBlock(Blocks.NETHER_SPROUTS, BlockModelGenerators.PlantType.NOT_TINTED);
		this.registerSimpleFlatItemModel(Items.NETHER_SPROUTS);
		this.createDoor(Blocks.IRON_DOOR);
		this.createTrapdoor(Blocks.IRON_TRAPDOOR);
		this.createSmoothStoneSlab();
		this.createPassiveRail(Blocks.RAIL);
		this.createActiveRail(Blocks.POWERED_RAIL);
		this.createActiveRail(Blocks.DETECTOR_RAIL);
		this.createActiveRail(Blocks.ACTIVATOR_RAIL);
		this.createComparator();
		this.createCommandBlock(Blocks.COMMAND_BLOCK);
		this.createCommandBlock(Blocks.REPEATING_COMMAND_BLOCK);
		this.createCommandBlock(Blocks.CHAIN_COMMAND_BLOCK);
		this.createAnvil(Blocks.ANVIL);
		this.createAnvil(Blocks.CHIPPED_ANVIL);
		this.createAnvil(Blocks.DAMAGED_ANVIL);
		this.createBarrel();
		this.createBell();
		this.createFurnace(Blocks.FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
		this.createFurnace(Blocks.BLAST_FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
		this.createFurnace(Blocks.SMOKER, TexturedModel.ORIENTABLE);
		this.createRedstoneWire();
		this.createRespawnAnchor();
		this.createSculkCatalyst();
		this.copyModel(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
		this.copyModel(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
		this.copyModel(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
		this.copyModel(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
		this.createInfestedStone();
		this.copyModel(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
		this.createInfestedDeepslate();
	}

	private void createLightBlock() {
		ItemModel.Unbaked unbaked = ItemModelUtils.plainModel(this.createFlatItemModel(Items.LIGHT));
		Map<Integer, ItemModel.Unbaked> map = new HashMap(16);
		PropertyDispatch.C1<MultiVariant, Integer> c1 = PropertyDispatch.initial(BlockStateProperties.LEVEL);

		for (int i = 0; i <= 15; i++) {
			String string = String.format(Locale.ROOT, "_%02d", i);
			ResourceLocation resourceLocation = TextureMapping.getItemTexture(Items.LIGHT, string);
			c1.select(i, plainVariant(ModelTemplates.PARTICLE_ONLY.createWithSuffix(Blocks.LIGHT, string, TextureMapping.particle(resourceLocation), this.modelOutput)));
			ItemModel.Unbaked unbaked2 = ItemModelUtils.plainModel(
				ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(Items.LIGHT, string), TextureMapping.layer0(resourceLocation), this.modelOutput)
			);
			map.put(i, unbaked2);
		}

		this.itemModelOutput.accept(Items.LIGHT, ItemModelUtils.selectBlockItemProperty(LightBlock.LEVEL, unbaked, map));
		this.blockStateOutput.accept(MultiVariantGenerator.dispatch(Blocks.LIGHT).with(c1));
	}

	private void createCandleAndCandleCake(Block block, Block block2) {
		this.registerSimpleFlatItemModel(block.asItem());
		TextureMapping textureMapping = TextureMapping.cube(TextureMapping.getBlockTexture(block));
		TextureMapping textureMapping2 = TextureMapping.cube(TextureMapping.getBlockTexture(block, "_lit"));
		MultiVariant multiVariant = plainVariant(ModelTemplates.CANDLE.createWithSuffix(block, "_one_candle", textureMapping, this.modelOutput));
		MultiVariant multiVariant2 = plainVariant(ModelTemplates.TWO_CANDLES.createWithSuffix(block, "_two_candles", textureMapping, this.modelOutput));
		MultiVariant multiVariant3 = plainVariant(ModelTemplates.THREE_CANDLES.createWithSuffix(block, "_three_candles", textureMapping, this.modelOutput));
		MultiVariant multiVariant4 = plainVariant(ModelTemplates.FOUR_CANDLES.createWithSuffix(block, "_four_candles", textureMapping, this.modelOutput));
		MultiVariant multiVariant5 = plainVariant(ModelTemplates.CANDLE.createWithSuffix(block, "_one_candle_lit", textureMapping2, this.modelOutput));
		MultiVariant multiVariant6 = plainVariant(ModelTemplates.TWO_CANDLES.createWithSuffix(block, "_two_candles_lit", textureMapping2, this.modelOutput));
		MultiVariant multiVariant7 = plainVariant(ModelTemplates.THREE_CANDLES.createWithSuffix(block, "_three_candles_lit", textureMapping2, this.modelOutput));
		MultiVariant multiVariant8 = plainVariant(ModelTemplates.FOUR_CANDLES.createWithSuffix(block, "_four_candles_lit", textureMapping2, this.modelOutput));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.dispatch(block)
					.with(
						PropertyDispatch.initial(BlockStateProperties.CANDLES, BlockStateProperties.LIT)
							.select(1, false, multiVariant)
							.select(2, false, multiVariant2)
							.select(3, false, multiVariant3)
							.select(4, false, multiVariant4)
							.select(1, true, multiVariant5)
							.select(2, true, multiVariant6)
							.select(3, true, multiVariant7)
							.select(4, true, multiVariant8)
					)
			);
		MultiVariant multiVariant9 = plainVariant(ModelTemplates.CANDLE_CAKE.create(block2, TextureMapping.candleCake(block, false), this.modelOutput));
		MultiVariant multiVariant10 = plainVariant(
			ModelTemplates.CANDLE_CAKE.createWithSuffix(block2, "_lit", TextureMapping.candleCake(block, true), this.modelOutput)
		);
		this.blockStateOutput
			.accept(MultiVariantGenerator.dispatch(block2).with(createBooleanModelDispatch(BlockStateProperties.LIT, multiVariant10, multiVariant9)));
	}

	@Environment(EnvType.CLIENT)
	class BlockFamilyProvider {
		private final TextureMapping mapping;
		private final Map<ModelTemplate, ResourceLocation> models = new HashMap();
		@Nullable
		private BlockFamily family;
		@Nullable
		private Variant fullBlock;
		private final Set<Block> skipGeneratingModelsFor = new HashSet();

		public BlockFamilyProvider(final TextureMapping textureMapping) {
			this.mapping = textureMapping;
		}

		public BlockModelGenerators.BlockFamilyProvider fullBlock(Block block, ModelTemplate modelTemplate) {
			this.fullBlock = BlockModelGenerators.plainModel(modelTemplate.create(block, this.mapping, BlockModelGenerators.this.modelOutput));
			if (BlockModelGenerators.FULL_BLOCK_MODEL_CUSTOM_GENERATORS.containsKey(block)) {
				BlockModelGenerators.this.blockStateOutput
					.accept(
						((BlockModelGenerators.BlockStateGeneratorSupplier)BlockModelGenerators.FULL_BLOCK_MODEL_CUSTOM_GENERATORS.get(block))
							.create(block, this.fullBlock, this.mapping, BlockModelGenerators.this.modelOutput)
					);
			} else {
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.variant(this.fullBlock)));
			}

			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider donateModelTo(Block block, Block block2) {
			ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block2, BlockModelGenerators.plainVariant(resourceLocation)));
			BlockModelGenerators.this.itemModelOutput.copy(block.asItem(), block2.asItem());
			this.skipGeneratingModelsFor.add(block2);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider button(Block block) {
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(ModelTemplates.BUTTON.create(block, this.mapping, BlockModelGenerators.this.modelOutput));
			MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(
				ModelTemplates.BUTTON_PRESSED.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createButton(block, multiVariant, multiVariant2));
			ResourceLocation resourceLocation = ModelTemplates.BUTTON_INVENTORY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider wall(Block block) {
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(ModelTemplates.WALL_POST.create(block, this.mapping, BlockModelGenerators.this.modelOutput));
			MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(
				ModelTemplates.WALL_LOW_SIDE.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant3 = BlockModelGenerators.plainVariant(
				ModelTemplates.WALL_TALL_SIDE.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createWall(block, multiVariant, multiVariant2, multiVariant3));
			ResourceLocation resourceLocation = ModelTemplates.WALL_INVENTORY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider customFence(Block block) {
			TextureMapping textureMapping = TextureMapping.customParticle(block);
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_POST.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_SIDE_NORTH.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant3 = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_SIDE_EAST.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant4 = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_SIDE_SOUTH.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant5 = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_SIDE_WEST.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createCustomFence(block, multiVariant, multiVariant2, multiVariant3, multiVariant4, multiVariant5));
			ResourceLocation resourceLocation = ModelTemplates.CUSTOM_FENCE_INVENTORY.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider fence(Block block) {
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(ModelTemplates.FENCE_POST.create(block, this.mapping, BlockModelGenerators.this.modelOutput));
			MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(ModelTemplates.FENCE_SIDE.create(block, this.mapping, BlockModelGenerators.this.modelOutput));
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createFence(block, multiVariant, multiVariant2));
			ResourceLocation resourceLocation = ModelTemplates.FENCE_INVENTORY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider customFenceGate(Block block) {
			TextureMapping textureMapping = TextureMapping.customParticle(block);
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_GATE_OPEN.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_GATE_CLOSED.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant3 = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_GATE_WALL_OPEN.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant4 = BlockModelGenerators.plainVariant(
				ModelTemplates.CUSTOM_FENCE_GATE_WALL_CLOSED.create(block, textureMapping, BlockModelGenerators.this.modelOutput)
			);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createFenceGate(block, multiVariant, multiVariant2, multiVariant3, multiVariant4, false));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider fenceGate(Block block) {
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(
				ModelTemplates.FENCE_GATE_OPEN.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(
				ModelTemplates.FENCE_GATE_CLOSED.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant3 = BlockModelGenerators.plainVariant(
				ModelTemplates.FENCE_GATE_WALL_OPEN.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant4 = BlockModelGenerators.plainVariant(
				ModelTemplates.FENCE_GATE_WALL_CLOSED.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createFenceGate(block, multiVariant, multiVariant2, multiVariant3, multiVariant4, true));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider pressurePlate(Block block) {
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(
				ModelTemplates.PRESSURE_PLATE_UP.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(
				ModelTemplates.PRESSURE_PLATE_DOWN.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
			);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createPressurePlate(block, multiVariant, multiVariant2));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider sign(Block block) {
			if (this.family == null) {
				throw new IllegalStateException("Family not defined");
			} else {
				Block block2 = (Block)this.family.getVariants().get(BlockFamily.Variant.WALL_SIGN);
				MultiVariant multiVariant = BlockModelGenerators.plainVariant(
					ModelTemplates.PARTICLE_ONLY.create(block, this.mapping, BlockModelGenerators.this.modelOutput)
				);
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, multiVariant));
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block2, multiVariant));
				BlockModelGenerators.this.registerSimpleFlatItemModel(block.asItem());
				return this;
			}
		}

		public BlockModelGenerators.BlockFamilyProvider slab(Block block) {
			if (this.fullBlock == null) {
				throw new IllegalStateException("Full block not generated yet");
			} else {
				ResourceLocation resourceLocation = this.getOrCreateModel(ModelTemplates.SLAB_BOTTOM, block);
				MultiVariant multiVariant = BlockModelGenerators.plainVariant(this.getOrCreateModel(ModelTemplates.SLAB_TOP, block));
				BlockModelGenerators.this.blockStateOutput
					.accept(
						BlockModelGenerators.createSlab(block, BlockModelGenerators.plainVariant(resourceLocation), multiVariant, BlockModelGenerators.variant(this.fullBlock))
					);
				BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
				return this;
			}
		}

		public BlockModelGenerators.BlockFamilyProvider stairs(Block block) {
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(this.getOrCreateModel(ModelTemplates.STAIRS_INNER, block));
			ResourceLocation resourceLocation = this.getOrCreateModel(ModelTemplates.STAIRS_STRAIGHT, block);
			MultiVariant multiVariant2 = BlockModelGenerators.plainVariant(this.getOrCreateModel(ModelTemplates.STAIRS_OUTER, block));
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createStairs(block, multiVariant, BlockModelGenerators.plainVariant(resourceLocation), multiVariant2));
			BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
			return this;
		}

		private BlockModelGenerators.BlockFamilyProvider fullBlockVariant(Block block) {
			TexturedModel texturedModel = (TexturedModel)BlockModelGenerators.TEXTURED_MODELS.getOrDefault(block, TexturedModel.CUBE.get(block));
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(texturedModel.create(block, BlockModelGenerators.this.modelOutput));
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, multiVariant));
			return this;
		}

		private BlockModelGenerators.BlockFamilyProvider door(Block block) {
			BlockModelGenerators.this.createDoor(block);
			return this;
		}

		private void trapdoor(Block block) {
			if (BlockModelGenerators.NON_ORIENTABLE_TRAPDOOR.contains(block)) {
				BlockModelGenerators.this.createTrapdoor(block);
			} else {
				BlockModelGenerators.this.createOrientableTrapdoor(block);
			}
		}

		private ResourceLocation getOrCreateModel(ModelTemplate modelTemplate, Block block) {
			return (ResourceLocation)this.models
				.computeIfAbsent(modelTemplate, modelTemplatex -> modelTemplatex.create(block, this.mapping, BlockModelGenerators.this.modelOutput));
		}

		public BlockModelGenerators.BlockFamilyProvider generateFor(BlockFamily blockFamily) {
			this.family = blockFamily;
			blockFamily.getVariants()
				.forEach(
					(variant, block) -> {
						if (!this.skipGeneratingModelsFor.contains(block)) {
							BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block> biConsumer = (BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>)BlockModelGenerators.SHAPE_CONSUMERS
								.get(variant);
							if (biConsumer != null) {
								biConsumer.accept(this, block);
							}
						}
					}
				);
			return this;
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	interface BlockStateGeneratorSupplier {
		BlockModelDefinitionGenerator create(Block block, Variant variant, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer);
	}

	@Environment(EnvType.CLIENT)
	record BookSlotModelCacheKey(ModelTemplate template, String modelSuffix) {
	}

	@Environment(EnvType.CLIENT)
	static enum PlantType {
		TINTED(ModelTemplates.TINTED_CROSS, ModelTemplates.TINTED_FLOWER_POT_CROSS, false),
		NOT_TINTED(ModelTemplates.CROSS, ModelTemplates.FLOWER_POT_CROSS, false),
		EMISSIVE_NOT_TINTED(ModelTemplates.CROSS_EMISSIVE, ModelTemplates.FLOWER_POT_CROSS_EMISSIVE, true);

		private final ModelTemplate blockTemplate;
		private final ModelTemplate flowerPotTemplate;
		private final boolean isEmissive;

		private PlantType(final ModelTemplate modelTemplate, final ModelTemplate modelTemplate2, final boolean bl) {
			this.blockTemplate = modelTemplate;
			this.flowerPotTemplate = modelTemplate2;
			this.isEmissive = bl;
		}

		public ModelTemplate getCross() {
			return this.blockTemplate;
		}

		public ModelTemplate getCrossPot() {
			return this.flowerPotTemplate;
		}

		public ResourceLocation createItemModel(BlockModelGenerators blockModelGenerators, Block block) {
			Item item = block.asItem();
			return this.isEmissive
				? blockModelGenerators.createFlatItemModelWithBlockTextureAndOverlay(item, block, "_emissive")
				: blockModelGenerators.createFlatItemModelWithBlockTexture(item, block);
		}

		public TextureMapping getTextureMapping(Block block) {
			return this.isEmissive ? TextureMapping.crossEmissive(block) : TextureMapping.cross(block);
		}

		public TextureMapping getPlantTextureMapping(Block block) {
			return this.isEmissive ? TextureMapping.plantEmissive(block) : TextureMapping.plant(block);
		}
	}

	@Environment(EnvType.CLIENT)
	class WoodProvider {
		private final TextureMapping logMapping;

		public WoodProvider(final TextureMapping textureMapping) {
			this.logMapping = textureMapping;
		}

		public BlockModelGenerators.WoodProvider wood(Block block) {
			TextureMapping textureMapping = this.logMapping.copyAndUpdate(TextureSlot.END, this.logMapping.get(TextureSlot.SIDE));
			ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, BlockModelGenerators.plainVariant(resourceLocation)));
			BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
			return this;
		}

		public BlockModelGenerators.WoodProvider log(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN.create(block, this.logMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, BlockModelGenerators.plainVariant(resourceLocation)));
			BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
			return this;
		}

		public BlockModelGenerators.WoodProvider logWithHorizontal(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN.create(block, this.logMapping, BlockModelGenerators.this.modelOutput);
			MultiVariant multiVariant = BlockModelGenerators.plainVariant(
				ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(block, this.logMapping, BlockModelGenerators.this.modelOutput)
			);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block, BlockModelGenerators.plainVariant(resourceLocation), multiVariant));
			BlockModelGenerators.this.registerSimpleItemModel(block, resourceLocation);
			return this;
		}

		public BlockModelGenerators.WoodProvider logUVLocked(Block block) {
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createPillarBlockUVLocked(block, this.logMapping, BlockModelGenerators.this.modelOutput));
			BlockModelGenerators.this.registerSimpleItemModel(block, ModelTemplates.CUBE_COLUMN.create(block, this.logMapping, BlockModelGenerators.this.modelOutput));
			return this;
		}
	}
}
