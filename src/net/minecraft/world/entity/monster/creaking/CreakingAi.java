package net.minecraft.world.entity.monster.creaking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

public class CreakingAi {
	protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Creaking>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS
	);
	protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.ATTACK_COOLING_DOWN
	);

	static void initCoreActivity(Brain<Creaking> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Swim<Creaking>(0.8F) {
			protected boolean checkExtraStartConditions(ServerLevel serverLevel, Creaking creaking) {
				return creaking.canMove() && super.checkExtraStartConditions(serverLevel, creaking);
			}
		}, new LookAtTargetSink(45, 90), new MoveToTargetSink()));
	}

	static void initIdleActivity(Brain<Creaking> brain) {
		brain.addActivity(
			Activity.IDLE,
			10,
			ImmutableList.of(
				StartAttacking.create(
					(serverLevel, creaking) -> creaking.isActive(),
					(serverLevel, creaking) -> creaking.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
				),
				SetEntityLookTargetSometimes.create(8.0F, UniformInt.of(30, 60)),
				new RunOne<>(
					ImmutableList.of(Pair.of(RandomStroll.stroll(0.3F), 2), Pair.of(SetWalkTargetFromLookTarget.create(0.3F, 3), 2), Pair.of(new DoNothing(30, 60), 1))
				)
			)
		);
	}

	static void initFightActivity(Creaking creaking, Brain<Creaking> brain) {
		brain.addActivityWithConditions(
			Activity.FIGHT,
			10,
			ImmutableList.of(
				SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
				MeleeAttack.create(Creaking::canMove, 40),
				StopAttackingIfTargetInvalid.create((serverLevel, livingEntity) -> !isAttackTargetStillReachable(creaking, livingEntity))
			),
			ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))
		);
	}

	private static boolean isAttackTargetStillReachable(Creaking creaking, LivingEntity livingEntity) {
		Optional<List<Player>> optional = creaking.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS);
		return (Boolean)optional.map(list -> livingEntity instanceof Player player && list.contains(player)).orElse(false);
	}

	public static Brain.Provider<Creaking> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	public static Brain<Creaking> makeBrain(Creaking creaking, Brain<Creaking> brain) {
		initCoreActivity(brain);
		initIdleActivity(brain);
		initFightActivity(creaking, brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.useDefaultActivity();
		return brain;
	}

	public static void updateActivity(Creaking creaking) {
		if (!creaking.canMove()) {
			creaking.getBrain().useDefaultActivity();
		} else {
			creaking.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
		}
	}
}
