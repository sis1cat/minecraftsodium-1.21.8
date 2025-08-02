package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BlockEntity implements RenderDataBlockEntity {
	private static final Codec<BlockEntityType<?>> TYPE_CODEC = BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec();
	private static final Logger LOGGER = LogUtils.getLogger();
	private final BlockEntityType<?> type;
	@Nullable
	protected Level level;
	protected final BlockPos worldPosition;
	protected boolean remove;
	private BlockState blockState;
	private DataComponentMap components = DataComponentMap.EMPTY;

	public BlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		this.type = blockEntityType;
		this.worldPosition = blockPos.immutable();
		this.validateBlockState(blockState);
		this.blockState = blockState;
	}

	private void validateBlockState(BlockState blockState) {
		if (!this.isValidBlockState(blockState)) {
			throw new IllegalStateException("Invalid block entity " + this.getNameForReporting() + " state at " + this.worldPosition + ", got " + blockState);
		}
	}

	public boolean isValidBlockState(BlockState blockState) {
		return this.type.isValid(blockState);
	}

	public static BlockPos getPosFromTag(ChunkPos chunkPos, CompoundTag compoundTag) {
		int i = compoundTag.getIntOr("x", 0);
		int j = compoundTag.getIntOr("y", 0);
		int k = compoundTag.getIntOr("z", 0);
		int l = SectionPos.blockToSectionCoord(i);
		int m = SectionPos.blockToSectionCoord(k);
		if (l != chunkPos.x || m != chunkPos.z) {
			LOGGER.warn("Block entity {} found in a wrong chunk, expected position from chunk {}", compoundTag, chunkPos);
			i = chunkPos.getBlockX(SectionPos.sectionRelative(i));
			k = chunkPos.getBlockZ(SectionPos.sectionRelative(k));
		}

		return new BlockPos(i, j, k);
	}

	@Nullable
	public Level getLevel() {
		return this.level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public boolean hasLevel() {
		return this.level != null;
	}

	protected void loadAdditional(ValueInput valueInput) {
	}

	public final void loadWithComponents(ValueInput valueInput) {
		this.loadAdditional(valueInput);
		this.components = (DataComponentMap)valueInput.read("components", DataComponentMap.CODEC).orElse(DataComponentMap.EMPTY);
	}

	public final void loadCustomOnly(ValueInput valueInput) {
		this.loadAdditional(valueInput);
	}

	protected void saveAdditional(ValueOutput valueOutput) {
	}

	public final CompoundTag saveWithFullMetadata(HolderLookup.Provider provider) {
		CompoundTag var4;
		try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
			TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, provider);
			this.saveWithFullMetadata(tagValueOutput);
			var4 = tagValueOutput.buildResult();
		}

		return var4;
	}

	public void saveWithFullMetadata(ValueOutput valueOutput) {
		this.saveWithoutMetadata(valueOutput);
		this.saveMetadata(valueOutput);
	}

	public void saveWithId(ValueOutput valueOutput) {
		this.saveWithoutMetadata(valueOutput);
		this.saveId(valueOutput);
	}

	public final CompoundTag saveWithoutMetadata(HolderLookup.Provider provider) {
		CompoundTag var4;
		try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
			TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, provider);
			this.saveWithoutMetadata(tagValueOutput);
			var4 = tagValueOutput.buildResult();
		}

		return var4;
	}

	public void saveWithoutMetadata(ValueOutput valueOutput) {
		this.saveAdditional(valueOutput);
		valueOutput.store("components", DataComponentMap.CODEC, this.components);
	}

	public final CompoundTag saveCustomOnly(HolderLookup.Provider provider) {
		CompoundTag var4;
		try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
			TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, provider);
			this.saveCustomOnly(tagValueOutput);
			var4 = tagValueOutput.buildResult();
		}

		return var4;
	}

	public void saveCustomOnly(ValueOutput valueOutput) {
		this.saveAdditional(valueOutput);
	}

	private void saveId(ValueOutput valueOutput) {
		addEntityType(valueOutput, this.getType());
	}

	public static void addEntityType(ValueOutput valueOutput, BlockEntityType<?> blockEntityType) {
		valueOutput.store("id", TYPE_CODEC, blockEntityType);
	}

	private void saveMetadata(ValueOutput valueOutput) {
		this.saveId(valueOutput);
		valueOutput.putInt("x", this.worldPosition.getX());
		valueOutput.putInt("y", this.worldPosition.getY());
		valueOutput.putInt("z", this.worldPosition.getZ());
	}

	@Nullable
	public static BlockEntity loadStatic(BlockPos blockPos, BlockState blockState, CompoundTag compoundTag, HolderLookup.Provider provider) {
		BlockEntityType<?> blockEntityType = (BlockEntityType<?>)compoundTag.read("id", TYPE_CODEC).orElse(null);
		if (blockEntityType == null) {
			LOGGER.error("Skipping block entity with invalid type: {}", compoundTag.get("id"));
			return null;
		} else {
			BlockEntity blockEntity;
			try {
				blockEntity = blockEntityType.create(blockPos, blockState);
			} catch (Throwable var12) {
				LOGGER.error("Failed to create block entity {} for block {} at position {} ", blockEntityType, blockPos, blockState, var12);
				return null;
			}

			try {
				BlockEntity var7;
				try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
					blockEntity.loadWithComponents(TagValueInput.create(scopedCollector, provider, compoundTag));
					var7 = blockEntity;
				}

				return var7;
			} catch (Throwable var11) {
				LOGGER.error("Failed to load data for block entity {} for block {} at position {}", blockEntityType, blockPos, blockState, var11);
				return null;
			}
		}
	}

	public void setChanged() {
		if (this.level != null) {
			setChanged(this.level, this.worldPosition, this.blockState);
		}
	}

	protected static void setChanged(Level level, BlockPos blockPos, BlockState blockState) {
		level.blockEntityChanged(blockPos);
		if (!blockState.isAir()) {
			level.updateNeighbourForOutputSignal(blockPos, blockState.getBlock());
		}
	}

	public BlockPos getBlockPos() {
		return this.worldPosition;
	}

	public BlockState getBlockState() {
		return this.blockState;
	}

	@Nullable
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return null;
	}

	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return new CompoundTag();
	}

	public boolean isRemoved() {
		return this.remove;
	}

	public void setRemoved() {
		this.remove = true;
	}

	public void clearRemoved() {
		this.remove = false;
	}

	public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
		if (this instanceof Container container && this.level != null) {
			Containers.dropContents(this.level, blockPos, container);
		}
	}

	public boolean triggerEvent(int i, int j) {
		return false;
	}

	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail("Name", this::getNameForReporting);
		crashReportCategory.setDetail("Cached block", this.getBlockState()::toString);
		if (this.level == null) {
			crashReportCategory.setDetail("Block location", (CrashReportDetail<String>)(() -> this.worldPosition + " (world missing)"));
		} else {
			crashReportCategory.setDetail("Actual block", this.level.getBlockState(this.worldPosition)::toString);
			CrashReportCategory.populateBlockLocationDetails(crashReportCategory, this.level, this.worldPosition);
		}
	}

	public String getNameForReporting() {
		return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName();
	}

	public BlockEntityType<?> getType() {
		return this.type;
	}

	@Deprecated
	public void setBlockState(BlockState blockState) {
		this.validateBlockState(blockState);
		this.blockState = blockState;
	}

	protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
	}

	public final void applyComponentsFromItemStack(ItemStack itemStack) {
		this.applyComponents(itemStack.getPrototype(), itemStack.getComponentsPatch());
	}

	public final void applyComponents(DataComponentMap dataComponentMap, DataComponentPatch dataComponentPatch) {
		final Set<DataComponentType<?>> set = new HashSet();
		set.add(DataComponents.BLOCK_ENTITY_DATA);
		set.add(DataComponents.BLOCK_STATE);
		final DataComponentMap dataComponentMap2 = PatchedDataComponentMap.fromPatch(dataComponentMap, dataComponentPatch);
		this.applyImplicitComponents(new DataComponentGetter() {
			@Nullable
			@Override
			public <T> T get(DataComponentType<? extends T> dataComponentType) {
				set.add(dataComponentType);
				return dataComponentMap2.get(dataComponentType);
			}

			@Override
			public <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T object) {
				set.add(dataComponentType);
				return dataComponentMap2.getOrDefault(dataComponentType, object);
			}
		});
		DataComponentPatch dataComponentPatch2 = dataComponentPatch.forget(set::contains);
		this.components = dataComponentPatch2.split().added();
	}

	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
	}

	@Deprecated
	public void removeComponentsFromTag(ValueOutput valueOutput) {
	}

	public final DataComponentMap collectComponents() {
		DataComponentMap.Builder builder = DataComponentMap.builder();
		builder.addAll(this.components);
		this.collectImplicitComponents(builder);
		return builder.build();
	}

	public DataComponentMap components() {
		return this.components;
	}

	public void setComponents(DataComponentMap dataComponentMap) {
		this.components = dataComponentMap;
	}

	@Nullable
	public static Component parseCustomNameSafe(ValueInput valueInput, String string) {
		return (Component)valueInput.read(string, ComponentSerialization.CODEC).orElse(null);
	}

	public ProblemReporter.PathElement problemPath() {
		return new BlockEntity.BlockEntityPathElement(this);
	}

	record BlockEntityPathElement(BlockEntity blockEntity) implements ProblemReporter.PathElement {
		@Override
		public String get() {
			return this.blockEntity.getNameForReporting() + "@" + this.blockEntity.getBlockPos();
		}
	}
}
