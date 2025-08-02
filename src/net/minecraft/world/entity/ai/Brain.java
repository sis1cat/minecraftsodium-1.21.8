package net.minecraft.world.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Brain<E extends LivingEntity> {
	static final Logger LOGGER = LogUtils.getLogger();
	private final Supplier<Codec<Brain<E>>> codec;
	private static final int SCHEDULE_UPDATE_DELAY = 20;
	private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = Maps.<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>>newHashMap();
	private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.<SensorType<? extends Sensor<? super E>>, Sensor<? super E>>newLinkedHashMap();
	private final Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
	private Schedule schedule = Schedule.EMPTY;
	private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>>newHashMap();
	private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.<Activity, Set<MemoryModuleType<?>>>newHashMap();
	private Set<Activity> coreActivities = Sets.<Activity>newHashSet();
	private final Set<Activity> activeActivities = Sets.<Activity>newHashSet();
	private Activity defaultActivity = Activity.IDLE;
	private long lastScheduleUpdate = -9999L;

	public static <E extends LivingEntity> Brain.Provider<E> provider(
		Collection<? extends MemoryModuleType<?>> collection, Collection<? extends SensorType<? extends Sensor<? super E>>> collection2
	) {
		return new Brain.Provider<>(collection, collection2);
	}

	public static <E extends LivingEntity> Codec<Brain<E>> codec(
			final Collection<? extends MemoryModuleType<?>> pMemoryTypes, final Collection<? extends SensorType<? extends Sensor<? super E>>> pSensorTypes
	) {
		final MutableObject<Codec<Brain<E>>> mutableobject = new MutableObject<>();
		mutableobject.setValue(
				(new MapCodec<Brain<E>>() {
					@Override
					public <T> Stream<T> keys(DynamicOps<T> p_22029_) {
						return pMemoryTypes.stream()
								.flatMap(
										p_22020_ -> p_22020_.getCodec().map(p_258254_ -> BuiltInRegistries.MEMORY_MODULE_TYPE.getKey((MemoryModuleType<?>)p_22020_)).stream()
								)
								.map(p_22018_ -> p_22029_.createString(p_22018_.toString()));
					}

					@Override
					public <T> DataResult<Brain<E>> decode(DynamicOps<T> p_22022_, MapLike<T> p_22023_) {
						MutableObject<DataResult<Builder<Brain.MemoryValue<?>>>> mutableobject1 = new MutableObject<>(
								DataResult.success(ImmutableList.builder())
						);
						p_22023_.entries()
								.forEach(
										p_358908_ -> {
											DataResult<MemoryModuleType<?>> dataresult = BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().parse(p_22022_, p_358908_.getFirst());
											DataResult<? extends Brain.MemoryValue<?>> dataresult1 = dataresult.flatMap(
													p_147350_ -> this.captureRead((MemoryModuleType<T>)p_147350_, p_22022_, (T)p_358908_.getSecond())
											);
											mutableobject1.setValue(mutableobject1.getValue().apply2(Builder::add, dataresult1));
										}
								);
						ImmutableList<Brain.MemoryValue<?>> immutablelist = mutableobject1.getValue()
								.resultOrPartial(Brain.LOGGER::error)
								.map(Builder::build)
								.orElseGet(ImmutableList::of);
						return DataResult.success(new Brain<>(pMemoryTypes, pSensorTypes, immutablelist, mutableobject::getValue));
					}

					private <T, U> DataResult<Brain.MemoryValue<U>> captureRead(MemoryModuleType<U> p_21997_, DynamicOps<T> p_21998_, T p_21999_) {
						return p_21997_.getCodec()
								.map(DataResult::success)
								.orElseGet(() -> DataResult.error(() -> "No codec for memory: " + p_21997_))
								.<ExpirableValue<U>>flatMap(p_22011_ -> p_22011_.parse(p_21998_, p_21999_))
								.map(p_21992_ -> new Brain.MemoryValue<>(p_21997_, Optional.of(p_21992_)));
					}

					public <T> RecordBuilder<T> encode(Brain<E> p_21985_, DynamicOps<T> p_21986_, RecordBuilder<T> p_21987_) {
						p_21985_.memories().forEach(p_22007_ -> p_22007_.serialize(p_21986_, p_21987_));
						return p_21987_;
					}
				})
						.fieldOf("memories")
						.codec()
		);
		return mutableobject.getValue();
	}

	public Brain(
		Collection<? extends MemoryModuleType<?>> collection,
		Collection<? extends SensorType<? extends Sensor<? super E>>> collection2,
		ImmutableList<Brain.MemoryValue<?>> immutableList,
		Supplier<Codec<Brain<E>>> supplier
	) {
		this.codec = supplier;

		for (MemoryModuleType<?> memoryModuleType : collection) {
			this.memories.put(memoryModuleType, Optional.empty());
		}

		for (SensorType<? extends Sensor<? super E>> sensorType : collection2) {
			this.sensors.put(sensorType, sensorType.create());
		}

		for (Sensor<? super E> sensor : this.sensors.values()) {
			for (MemoryModuleType<?> memoryModuleType2 : sensor.requires()) {
				this.memories.put(memoryModuleType2, Optional.empty());
			}
		}

		for (Brain.MemoryValue<?> memoryValue : immutableList) {
			memoryValue.setMemoryInternal(this);
		}
	}

	public <T> DataResult<T> serializeStart(DynamicOps<T> dynamicOps) {
		return ((Codec)this.codec.get()).encodeStart(dynamicOps, this);
	}

	Stream<Brain.MemoryValue<?>> memories() {
		return this.memories
			.entrySet()
			.stream()
			.map(entry -> Brain.MemoryValue.createUnchecked((MemoryModuleType)entry.getKey(), (Optional<? extends ExpirableValue<?>>)entry.getValue()));
	}

	public boolean hasMemoryValue(MemoryModuleType<?> memoryModuleType) {
		return this.checkMemory(memoryModuleType, MemoryStatus.VALUE_PRESENT);
	}

	public void clearMemories() {
		this.memories.keySet().forEach(memoryModuleType -> this.memories.put(memoryModuleType, Optional.empty()));
	}

	public <U> void eraseMemory(MemoryModuleType<U> memoryModuleType) {
		this.setMemory(memoryModuleType, Optional.empty());
	}

	public <U> void setMemory(MemoryModuleType<U> memoryModuleType, @Nullable U object) {
		this.setMemory(memoryModuleType, Optional.ofNullable(object));
	}

	public <U> void setMemoryWithExpiry(MemoryModuleType<U> memoryModuleType, U object, long l) {
		this.setMemoryInternal(memoryModuleType, Optional.of(ExpirableValue.of(object, l)));
	}

	public <U> void setMemory(MemoryModuleType<U> memoryModuleType, Optional<? extends U> optional) {
		this.setMemoryInternal(memoryModuleType, optional.map(ExpirableValue::of));
	}

	<U> void setMemoryInternal(MemoryModuleType<U> memoryModuleType, Optional<? extends ExpirableValue<?>> optional) {
		if (this.memories.containsKey(memoryModuleType)) {
			if (optional.isPresent() && this.isEmptyCollection(((ExpirableValue)optional.get()).getValue())) {
				this.eraseMemory(memoryModuleType);
			} else {
				this.memories.put(memoryModuleType, optional);
			}
		}
	}

	public <U> Optional<U> getMemory(MemoryModuleType<U> pType) {
		Optional<? extends ExpirableValue<?>> optional = this.memories.get(pType);
		if (optional == null) {
			throw new IllegalStateException("Unregistered memory fetched: " + pType);
		} else {
			return (Optional<U>)optional.map(ExpirableValue::getValue);
		}
	}

	@javax.annotation.Nullable
	public <U> Optional<U> getMemoryInternal(MemoryModuleType<U> pType) {
		Optional<? extends ExpirableValue<?>> optional = this.memories.get(pType);
		return optional == null ? null : (Optional<U>)optional.map(ExpirableValue::getValue);
	}
	public <U> long getTimeUntilExpiry(MemoryModuleType<U> memoryModuleType) {
		Optional<? extends ExpirableValue<?>> optional = (Optional<? extends ExpirableValue<?>>)this.memories.get(memoryModuleType);
		return (Long)optional.map(ExpirableValue::getTimeToLive).orElse(0L);
	}

	@Deprecated
	@VisibleForDebug
	public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
		return this.memories;
	}

	public <U> boolean isMemoryValue(MemoryModuleType<U> memoryModuleType, U object) {
		return !this.hasMemoryValue(memoryModuleType) ? false : this.getMemory(memoryModuleType).filter(object2 -> object2.equals(object)).isPresent();
	}

	public boolean checkMemory(MemoryModuleType<?> memoryModuleType, MemoryStatus memoryStatus) {
		Optional<? extends ExpirableValue<?>> optional = (Optional<? extends ExpirableValue<?>>)this.memories.get(memoryModuleType);
		return optional == null
			? false
			: memoryStatus == MemoryStatus.REGISTERED
				|| memoryStatus == MemoryStatus.VALUE_PRESENT && optional.isPresent()
				|| memoryStatus == MemoryStatus.VALUE_ABSENT && optional.isEmpty();
	}

	public Schedule getSchedule() {
		return this.schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public void setCoreActivities(Set<Activity> set) {
		this.coreActivities = set;
	}

	@Deprecated
	@VisibleForDebug
	public Set<Activity> getActiveActivities() {
		return this.activeActivities;
	}

	@Deprecated
	@VisibleForDebug
	public List<BehaviorControl<? super E>> getRunningBehaviors() {
		List<BehaviorControl<? super E>> list = new ObjectArrayList<>();

		for (Map<Activity, Set<BehaviorControl<? super E>>> map : this.availableBehaviorsByPriority.values()) {
			for (Set<BehaviorControl<? super E>> set : map.values()) {
				for (BehaviorControl<? super E> behaviorControl : set) {
					if (behaviorControl.getStatus() == Behavior.Status.RUNNING) {
						list.add(behaviorControl);
					}
				}
			}
		}

		return list;
	}

	public void useDefaultActivity() {
		this.setActiveActivity(this.defaultActivity);
	}

	public Optional<Activity> getActiveNonCoreActivity() {
		for (Activity activity : this.activeActivities) {
			if (!this.coreActivities.contains(activity)) {
				return Optional.of(activity);
			}
		}

		return Optional.empty();
	}

	public void setActiveActivityIfPossible(Activity activity) {
		if (this.activityRequirementsAreMet(activity)) {
			this.setActiveActivity(activity);
		} else {
			this.useDefaultActivity();
		}
	}

	private void setActiveActivity(Activity activity) {
		if (!this.isActive(activity)) {
			this.eraseMemoriesForOtherActivitesThan(activity);
			this.activeActivities.clear();
			this.activeActivities.addAll(this.coreActivities);
			this.activeActivities.add(activity);
		}
	}

	private void eraseMemoriesForOtherActivitesThan(Activity activity) {
		for (Activity activity2 : this.activeActivities) {
			if (activity2 != activity) {
				Set<MemoryModuleType<?>> set = (Set<MemoryModuleType<?>>)this.activityMemoriesToEraseWhenStopped.get(activity2);
				if (set != null) {
					for (MemoryModuleType<?> memoryModuleType : set) {
						this.eraseMemory(memoryModuleType);
					}
				}
			}
		}
	}

	public void updateActivityFromSchedule(long l, long m) {
		if (m - this.lastScheduleUpdate > 20L) {
			this.lastScheduleUpdate = m;
			Activity activity = this.getSchedule().getActivityAt((int)(l % 24000L));
			if (!this.activeActivities.contains(activity)) {
				this.setActiveActivityIfPossible(activity);
			}
		}
	}

	public void setActiveActivityToFirstValid(List<Activity> list) {
		for (Activity activity : list) {
			if (this.activityRequirementsAreMet(activity)) {
				this.setActiveActivity(activity);
				break;
			}
		}
	}

	public void setDefaultActivity(Activity activity) {
		this.defaultActivity = activity;
	}

	public void addActivity(Activity activity, int i, ImmutableList<? extends BehaviorControl<? super E>> immutableList) {
		this.addActivity(activity, this.createPriorityPairs(i, immutableList));
	}

	public void addActivityAndRemoveMemoryWhenStopped(
		Activity activity, int i, ImmutableList<? extends BehaviorControl<? super E>> immutableList, MemoryModuleType<?> memoryModuleType
	) {
		Set<Pair<MemoryModuleType<?>, MemoryStatus>> set = ImmutableSet.of(Pair.of(memoryModuleType, MemoryStatus.VALUE_PRESENT));
		Set<MemoryModuleType<?>> set2 = ImmutableSet.of(memoryModuleType);
		this.addActivityAndRemoveMemoriesWhenStopped(activity, this.createPriorityPairs(i, immutableList), set, set2);
	}

	public void addActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutableList) {
		this.addActivityAndRemoveMemoriesWhenStopped(activity, immutableList, ImmutableSet.of(), Sets.<MemoryModuleType<?>>newHashSet());
	}

	public void addActivityWithConditions(
		Activity activity, int i, ImmutableList<? extends BehaviorControl<? super E>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set
	) {
		this.addActivityWithConditions(activity, this.createPriorityPairs(i, immutableList), set);
	}

	public void addActivityWithConditions(
		Activity activity,
		ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutableList,
		Set<Pair<MemoryModuleType<?>, MemoryStatus>> set
	) {
		this.addActivityAndRemoveMemoriesWhenStopped(activity, immutableList, set, Sets.<MemoryModuleType<?>>newHashSet());
	}

	public void addActivityAndRemoveMemoriesWhenStopped(
		Activity activity,
		ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutableList,
		Set<Pair<MemoryModuleType<?>, MemoryStatus>> set,
		Set<MemoryModuleType<?>> set2
	) {
		this.activityRequirements.put(activity, set);
		if (!set2.isEmpty()) {
			this.activityMemoriesToEraseWhenStopped.put(activity, set2);
		}

		for (Pair<Integer, ? extends BehaviorControl<? super E>> pair : immutableList) {
			((Set)((Map)this.availableBehaviorsByPriority.computeIfAbsent(pair.getFirst(), integer -> Maps.newHashMap()))
					.computeIfAbsent(activity, activityx -> Sets.newLinkedHashSet()))
				.add(pair.getSecond());
		}
	}

	@VisibleForTesting
	public void removeAllBehaviors() {
		this.availableBehaviorsByPriority.clear();
	}

	public boolean isActive(Activity activity) {
		return this.activeActivities.contains(activity);
	}

	public Brain<E> copyWithoutBehaviors() {
		Brain<E> brain = new Brain<>(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codec);

		for (Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : this.memories.entrySet()) {
			MemoryModuleType<?> memoryModuleType = (MemoryModuleType<?>)entry.getKey();
			if (((Optional)entry.getValue()).isPresent()) {
				brain.memories.put(memoryModuleType, (Optional)entry.getValue());
			}
		}

		return brain;
	}

	public void tick(ServerLevel serverLevel, E livingEntity) {
		this.forgetOutdatedMemories();
		this.tickSensors(serverLevel, livingEntity);
		this.startEachNonRunningBehavior(serverLevel, livingEntity);
		this.tickEachRunningBehavior(serverLevel, livingEntity);
	}

	private void tickSensors(ServerLevel serverLevel, E livingEntity) {
		for (Sensor<? super E> sensor : this.sensors.values()) {
			sensor.tick(serverLevel, livingEntity);
		}
	}

	private void forgetOutdatedMemories() {
		for (Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> entry : this.memories.entrySet()) {
			if (((Optional)entry.getValue()).isPresent()) {
				ExpirableValue<?> expirableValue = (ExpirableValue<?>)((Optional)entry.getValue()).get();
				if (expirableValue.hasExpired()) {
					this.eraseMemory((MemoryModuleType)entry.getKey());
				}

				expirableValue.tick();
			}
		}
	}

	public void stopAll(ServerLevel serverLevel, E livingEntity) {
		long l = livingEntity.level().getGameTime();

		for (BehaviorControl<? super E> behaviorControl : this.getRunningBehaviors()) {
			behaviorControl.doStop(serverLevel, livingEntity, l);
		}
	}

	private void startEachNonRunningBehavior(ServerLevel pLevel, E pEntity) {
		long i = pLevel.getGameTime();

		for (Map<Activity, Set<BehaviorControl<? super E>>> map : this.availableBehaviorsByPriority.values()) {
			for (Entry<Activity, Set<BehaviorControl<? super E>>> entry : map.entrySet()) {
				Activity activity = entry.getKey();
				if (this.activeActivities.contains(activity)) {
					for (BehaviorControl<? super E> behaviorcontrol : entry.getValue()) {
						if (behaviorcontrol.getStatus() == Behavior.Status.STOPPED) {
							behaviorcontrol.tryStart(pLevel, pEntity, i);
						}
					}
				}
			}
		}
	}

	private void tickEachRunningBehavior(ServerLevel serverLevel, E livingEntity) {
		long l = serverLevel.getGameTime();

		for (BehaviorControl<? super E> behaviorControl : this.getRunningBehaviors()) {
			behaviorControl.tickOrStop(serverLevel, livingEntity, l);
		}
	}

	private boolean activityRequirementsAreMet(Activity pActivity) {
		if (!this.activityRequirements.containsKey(pActivity)) {
			return false;
		} else {
			for (Pair<MemoryModuleType<?>, MemoryStatus> pair : this.activityRequirements.get(pActivity)) {
				MemoryModuleType<?> memorymoduletype = pair.getFirst();
				MemoryStatus memorystatus = pair.getSecond();
				if (!this.checkMemory(memorymoduletype, memorystatus)) {
					return false;
				}
			}

			return true;
		}
	}

	private boolean isEmptyCollection(Object object) {
		return object instanceof Collection && ((Collection)object).isEmpty();
	}

	ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> createPriorityPairs(
		int i, ImmutableList<? extends BehaviorControl<? super E>> immutableList
	) {
		int j = i;
		Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> builder = ImmutableList.builder();

		for (BehaviorControl<? super E> behaviorControl : immutableList) {
			builder.add(Pair.of(j++, behaviorControl));
		}

		return builder.build();
	}

	static final class MemoryValue<U> {
		private final MemoryModuleType<U> type;
		private final Optional<? extends ExpirableValue<U>> value;

		static <U> Brain.MemoryValue<U> createUnchecked(MemoryModuleType<U> memoryModuleType, Optional<? extends ExpirableValue<?>> optional) {
			return new Brain.MemoryValue<>(memoryModuleType, (Optional<? extends ExpirableValue<U>>)optional);
		}

		MemoryValue(MemoryModuleType<U> memoryModuleType, Optional<? extends ExpirableValue<U>> optional) {
			this.type = memoryModuleType;
			this.value = optional;
		}

		void setMemoryInternal(Brain<?> brain) {
			brain.setMemoryInternal(this.type, this.value);
		}

		public <T> void serialize(DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
			this.type
				.getCodec()
				.ifPresent(
					codec -> this.value
						.ifPresent(
							expirableValue -> recordBuilder.add(
								BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().encodeStart(dynamicOps, this.type), codec.encodeStart(dynamicOps, expirableValue)
							)
						)
				);
		}
	}

	public static final class Provider<E extends LivingEntity> {
		private final Collection<? extends MemoryModuleType<?>> memoryTypes;
		private final Collection<? extends SensorType<? extends Sensor<? super E>>> sensorTypes;
		private final Codec<Brain<E>> codec;

		Provider(Collection<? extends MemoryModuleType<?>> collection, Collection<? extends SensorType<? extends Sensor<? super E>>> collection2) {
			this.memoryTypes = collection;
			this.sensorTypes = collection2;
			this.codec = Brain.codec(collection, collection2);
		}

		public Brain<E> makeBrain(Dynamic<?> dynamic) {
			return (Brain<E>)this.codec
				.parse(dynamic)
				.resultOrPartial(Brain.LOGGER::error)
				.orElseGet(() -> new Brain(this.memoryTypes, this.sensorTypes, ImmutableList.of(), () -> this.codec));
		}
	}
}
