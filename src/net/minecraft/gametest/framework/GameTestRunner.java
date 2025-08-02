package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class GameTestRunner {
	public static final int DEFAULT_TESTS_PER_ROW = 8;
	private static final Logger LOGGER = LogUtils.getLogger();
	final ServerLevel level;
	private final GameTestTicker testTicker;
	private final List<GameTestInfo> allTestInfos;
	private ImmutableList<GameTestBatch> batches;
	final List<GameTestBatchListener> batchListeners = Lists.<GameTestBatchListener>newArrayList();
	private final List<GameTestInfo> scheduledForRerun = Lists.<GameTestInfo>newArrayList();
	private final GameTestRunner.GameTestBatcher testBatcher;
	private boolean stopped = true;
	@Nullable
	private Holder<TestEnvironmentDefinition> currentEnvironment;
	private final GameTestRunner.StructureSpawner existingStructureSpawner;
	private final GameTestRunner.StructureSpawner newStructureSpawner;
	final boolean haltOnError;

	protected GameTestRunner(
		GameTestRunner.GameTestBatcher gameTestBatcher,
		Collection<GameTestBatch> collection,
		ServerLevel serverLevel,
		GameTestTicker gameTestTicker,
		GameTestRunner.StructureSpawner structureSpawner,
		GameTestRunner.StructureSpawner structureSpawner2,
		boolean bl
	) {
		this.level = serverLevel;
		this.testTicker = gameTestTicker;
		this.testBatcher = gameTestBatcher;
		this.existingStructureSpawner = structureSpawner;
		this.newStructureSpawner = structureSpawner2;
		this.batches = ImmutableList.copyOf(collection);
		this.haltOnError = bl;
		this.allTestInfos = (List<GameTestInfo>)this.batches.stream().flatMap(gameTestBatch -> gameTestBatch.gameTestInfos().stream()).collect(Util.toMutableList());
		gameTestTicker.setRunner(this);
		this.allTestInfos.forEach(gameTestInfo -> gameTestInfo.addListener(new ReportGameListener()));
	}

	public List<GameTestInfo> getTestInfos() {
		return this.allTestInfos;
	}

	public void start() {
		this.stopped = false;
		this.runBatch(0);
	}

	public void stop() {
		this.stopped = true;
		if (this.currentEnvironment != null) {
			this.endCurrentEnvironment();
		}
	}

	public void rerunTest(GameTestInfo gameTestInfo) {
		GameTestInfo gameTestInfo2 = gameTestInfo.copyReset();
		gameTestInfo.getListeners().forEach(gameTestListener -> gameTestListener.testAddedForRerun(gameTestInfo, gameTestInfo2, this));
		this.allTestInfos.add(gameTestInfo2);
		this.scheduledForRerun.add(gameTestInfo2);
		if (this.stopped) {
			this.runScheduledRerunTests();
		}
	}

	void runBatch(int i) {
		if (i >= this.batches.size()) {
			this.endCurrentEnvironment();
			this.runScheduledRerunTests();
		} else {
			final GameTestBatch gameTestBatch = (GameTestBatch)this.batches.get(i);
			this.existingStructureSpawner.onBatchStart(this.level);
			this.newStructureSpawner.onBatchStart(this.level);
			Collection<GameTestInfo> collection = this.createStructuresForBatch(gameTestBatch.gameTestInfos());
			LOGGER.info(
				"Running test environment '{}' batch {} ({} tests)...", gameTestBatch.environment().getRegisteredName(), gameTestBatch.index(), collection.size()
			);
			if (this.currentEnvironment != gameTestBatch.environment()) {
				this.endCurrentEnvironment();
				this.currentEnvironment = gameTestBatch.environment();
				this.currentEnvironment.value().setup(this.level);
			}

			this.batchListeners.forEach(gameTestBatchListener -> gameTestBatchListener.testBatchStarting(gameTestBatch));
			final MultipleTestTracker multipleTestTracker = new MultipleTestTracker();
			collection.forEach(multipleTestTracker::addTestToTrack);
			multipleTestTracker.addListener(new GameTestListener() {
				private void testCompleted() {
					if (multipleTestTracker.isDone()) {
						GameTestRunner.this.batchListeners.forEach(gameTestBatchListener -> gameTestBatchListener.testBatchFinished(gameTestBatch));
						LongSet longSet = new LongArraySet(GameTestRunner.this.level.getForceLoadedChunks());
						longSet.forEach(l -> GameTestRunner.this.level.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false));
						GameTestRunner.this.runBatch(i + 1);
					}
				}

				@Override
				public void testStructureLoaded(GameTestInfo gameTestInfo) {
				}

				@Override
				public void testPassed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
					this.testCompleted();
				}

				@Override
				public void testFailed(GameTestInfo gameTestInfo, GameTestRunner gameTestRunner) {
					if (GameTestRunner.this.haltOnError) {
						GameTestRunner.this.endCurrentEnvironment();
						LongSet longSet = new LongArraySet(GameTestRunner.this.level.getForceLoadedChunks());
						longSet.forEach(l -> GameTestRunner.this.level.setChunkForced(ChunkPos.getX(l), ChunkPos.getZ(l), false));
						GameTestTicker.SINGLETON.clear();
					} else {
						this.testCompleted();
					}
				}

				@Override
				public void testAddedForRerun(GameTestInfo gameTestInfo, GameTestInfo gameTestInfo2, GameTestRunner gameTestRunner) {
				}
			});
			collection.forEach(this.testTicker::add);
		}
	}

	void endCurrentEnvironment() {
		if (this.currentEnvironment != null) {
			this.currentEnvironment.value().teardown(this.level);
			this.currentEnvironment = null;
		}
	}

	private void runScheduledRerunTests() {
		if (!this.scheduledForRerun.isEmpty()) {
			LOGGER.info(
				"Starting re-run of tests: {}", this.scheduledForRerun.stream().map(gameTestInfo -> gameTestInfo.id().toString()).collect(Collectors.joining(", "))
			);
			this.batches = ImmutableList.copyOf(this.testBatcher.batch(this.scheduledForRerun));
			this.scheduledForRerun.clear();
			this.stopped = false;
			this.runBatch(0);
		} else {
			this.batches = ImmutableList.of();
			this.stopped = true;
		}
	}

	public void addListener(GameTestBatchListener gameTestBatchListener) {
		this.batchListeners.add(gameTestBatchListener);
	}

	private Collection<GameTestInfo> createStructuresForBatch(Collection<GameTestInfo> collection) {
		return collection.stream().map(this::spawn).flatMap(Optional::stream).toList();
	}

	private Optional<GameTestInfo> spawn(GameTestInfo gameTestInfo) {
		return gameTestInfo.getTestBlockPos() == null
			? this.newStructureSpawner.spawnStructure(gameTestInfo)
			: this.existingStructureSpawner.spawnStructure(gameTestInfo);
	}

	public static void clearMarkers(ServerLevel serverLevel) {
		DebugPackets.sendGameTestClearPacket(serverLevel);
	}

	public static class Builder {
		private final ServerLevel level;
		private final GameTestTicker testTicker = GameTestTicker.SINGLETON;
		private GameTestRunner.GameTestBatcher batcher = GameTestBatchFactory.fromGameTestInfo();
		private GameTestRunner.StructureSpawner existingStructureSpawner = GameTestRunner.StructureSpawner.IN_PLACE;
		private GameTestRunner.StructureSpawner newStructureSpawner = GameTestRunner.StructureSpawner.NOT_SET;
		private final Collection<GameTestBatch> batches;
		private boolean haltOnError = false;

		private Builder(Collection<GameTestBatch> collection, ServerLevel serverLevel) {
			this.batches = collection;
			this.level = serverLevel;
		}

		public static GameTestRunner.Builder fromBatches(Collection<GameTestBatch> collection, ServerLevel serverLevel) {
			return new GameTestRunner.Builder(collection, serverLevel);
		}

		public static GameTestRunner.Builder fromInfo(Collection<GameTestInfo> collection, ServerLevel serverLevel) {
			return fromBatches(GameTestBatchFactory.fromGameTestInfo().batch(collection), serverLevel);
		}

		public GameTestRunner.Builder haltOnError(boolean bl) {
			this.haltOnError = bl;
			return this;
		}

		public GameTestRunner.Builder newStructureSpawner(GameTestRunner.StructureSpawner structureSpawner) {
			this.newStructureSpawner = structureSpawner;
			return this;
		}

		public GameTestRunner.Builder existingStructureSpawner(StructureGridSpawner structureGridSpawner) {
			this.existingStructureSpawner = structureGridSpawner;
			return this;
		}

		public GameTestRunner.Builder batcher(GameTestRunner.GameTestBatcher gameTestBatcher) {
			this.batcher = gameTestBatcher;
			return this;
		}

		public GameTestRunner build() {
			return new GameTestRunner(this.batcher, this.batches, this.level, this.testTicker, this.existingStructureSpawner, this.newStructureSpawner, this.haltOnError);
		}
	}

	public interface GameTestBatcher {
		Collection<GameTestBatch> batch(Collection<GameTestInfo> collection);
	}

	public interface StructureSpawner {
		GameTestRunner.StructureSpawner IN_PLACE = gameTestInfo -> Optional.ofNullable(gameTestInfo.prepareTestStructure())
			.map(gameTestInfox -> gameTestInfox.startExecution(1));
		GameTestRunner.StructureSpawner NOT_SET = gameTestInfo -> Optional.empty();

		Optional<GameTestInfo> spawnStructure(GameTestInfo gameTestInfo);

		default void onBatchStart(ServerLevel serverLevel) {
		}
	}
}
