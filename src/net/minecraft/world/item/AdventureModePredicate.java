package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AdventureModePredicate {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<AdventureModePredicate> CODEC = ExtraCodecs.compactListCodec(
			BlockPredicate.CODEC, ExtraCodecs.nonEmptyList(BlockPredicate.CODEC.listOf())
		)
		.xmap(AdventureModePredicate::new, adventureModePredicate -> adventureModePredicate.predicates);
	public static final StreamCodec<RegistryFriendlyByteBuf, AdventureModePredicate> STREAM_CODEC = StreamCodec.composite(
		BlockPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()), adventureModePredicate -> adventureModePredicate.predicates, AdventureModePredicate::new
	);
	public static final Component CAN_BREAK_HEADER = Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY);
	public static final Component CAN_PLACE_HEADER = Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY);
	private static final Component UNKNOWN_USE = Component.translatable("item.canUse.unknown").withStyle(ChatFormatting.GRAY);
	private final List<BlockPredicate> predicates;
	@Nullable
	private List<Component> cachedTooltip;
	@Nullable
	private BlockInWorld lastCheckedBlock;
	private boolean lastResult;
	private boolean checksBlockEntity;

	public AdventureModePredicate(List<BlockPredicate> list) {
		this.predicates = list;
	}

	private static boolean areSameBlocks(BlockInWorld blockInWorld, @Nullable BlockInWorld blockInWorld2, boolean bl) {
		if (blockInWorld2 == null || blockInWorld.getState() != blockInWorld2.getState()) {
			return false;
		} else if (!bl) {
			return true;
		} else if (blockInWorld.getEntity() == null && blockInWorld2.getEntity() == null) {
			return true;
		} else if (blockInWorld.getEntity() != null && blockInWorld2.getEntity() != null) {
			boolean var7;
			try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER)) {
				RegistryAccess registryAccess = blockInWorld.getLevel().registryAccess();
				CompoundTag compoundTag = saveBlockEntity(blockInWorld.getEntity(), registryAccess, scopedCollector);
				CompoundTag compoundTag2 = saveBlockEntity(blockInWorld2.getEntity(), registryAccess, scopedCollector);
				var7 = Objects.equals(compoundTag, compoundTag2);
			}

			return var7;
		} else {
			return false;
		}
	}

	private static CompoundTag saveBlockEntity(BlockEntity blockEntity, RegistryAccess registryAccess, ProblemReporter problemReporter) {
		TagValueOutput tagValueOutput = TagValueOutput.createWithContext(problemReporter.forChild(blockEntity.problemPath()), registryAccess);
		blockEntity.saveWithId(tagValueOutput);
		return tagValueOutput.buildResult();
	}

	public boolean test(BlockInWorld blockInWorld) {
		if (areSameBlocks(blockInWorld, this.lastCheckedBlock, this.checksBlockEntity)) {
			return this.lastResult;
		} else {
			this.lastCheckedBlock = blockInWorld;
			this.checksBlockEntity = false;

			for (BlockPredicate blockPredicate : this.predicates) {
				if (blockPredicate.matches(blockInWorld)) {
					this.checksBlockEntity = this.checksBlockEntity | blockPredicate.requiresNbt();
					this.lastResult = true;
					return true;
				}
			}

			this.lastResult = false;
			return false;
		}
	}

	private List<Component> tooltip() {
		if (this.cachedTooltip == null) {
			this.cachedTooltip = computeTooltip(this.predicates);
		}

		return this.cachedTooltip;
	}

	public void addToTooltip(Consumer<Component> consumer) {
		this.tooltip().forEach(consumer);
	}

	private static List<Component> computeTooltip(List<BlockPredicate> pPredicates) {
		for (BlockPredicate blockpredicate : pPredicates) {
			if (blockpredicate.blocks().isEmpty()) {
				return List.of(UNKNOWN_USE);
			}
		}

		return pPredicates.stream()
				.flatMap(p_333785_ -> p_333785_.blocks().orElseThrow().stream())
				.distinct()
				.map(p_335858_ -> (Component)p_335858_.value().getName().withStyle(ChatFormatting.DARK_GRAY))
				.toList();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return object instanceof AdventureModePredicate adventureModePredicate ? this.predicates.equals(adventureModePredicate.predicates) : false;
		}
	}

	public int hashCode() {
		return this.predicates.hashCode();
	}

	public String toString() {
		return "AdventureModePredicate{predicates=" + this.predicates + "}";
	}
}
