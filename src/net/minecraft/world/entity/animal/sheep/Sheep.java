package net.minecraft.world.entity.animal.sheep;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.Nullable;

public class Sheep extends Animal implements Shearable {
	private static final int EAT_ANIMATION_TICKS = 40;
	private static final EntityDataAccessor<Byte> DATA_WOOL_ID = SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BYTE);
	private static final DyeColor DEFAULT_COLOR = DyeColor.WHITE;
	private static final boolean DEFAULT_SHEARED = false;
	private int eatAnimationTick;
	private EatBlockGoal eatBlockGoal;

	public Sheep(EntityType<? extends Sheep> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		this.eatBlockGoal = new EatBlockGoal(this);
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, itemStack -> itemStack.is(ItemTags.SHEEP_FOOD), false));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
		this.goalSelector.addGoal(5, this.eatBlockGoal);
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.is(ItemTags.SHEEP_FOOD);
	}

	@Override
	protected void customServerAiStep(ServerLevel serverLevel) {
		this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
		super.customServerAiStep(serverLevel);
	}

	@Override
	public void aiStep() {
		if (this.level().isClientSide) {
			this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
		}

		super.aiStep();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.23F);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_WOOL_ID, (byte)0);
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 10) {
			this.eatAnimationTick = 40;
		} else {
			super.handleEntityEvent(b);
		}
	}

	public float getHeadEatPositionScale(float f) {
		if (this.eatAnimationTick <= 0) {
			return 0.0F;
		} else if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
			return 1.0F;
		} else {
			return this.eatAnimationTick < 4 ? (this.eatAnimationTick - f) / 4.0F : -(this.eatAnimationTick - 40 - f) / 4.0F;
		}
	}

	public float getHeadEatAngleScale(float f) {
		if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
			float g = (this.eatAnimationTick - 4 - f) / 32.0F;
			return (float) (Math.PI / 5) + 0.21991149F * Mth.sin(g * 28.7F);
		} else {
			return this.eatAnimationTick > 0 ? (float) (Math.PI / 5) : this.getXRot(f) * (float) (Math.PI / 180.0);
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.is(Items.SHEARS)) {
			if (this.level() instanceof ServerLevel serverLevel && this.readyForShearing()) {
				this.shear(serverLevel, SoundSource.PLAYERS, itemStack);
				this.gameEvent(GameEvent.SHEAR, player);
				itemStack.hurtAndBreak(1, player, getSlotForHand(interactionHand));
				return InteractionResult.SUCCESS_SERVER;
			} else {
				return InteractionResult.CONSUME;
			}
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	@Override
	public void shear(ServerLevel serverLevel, SoundSource soundSource, ItemStack itemStack) {
		serverLevel.playSound(null, this, SoundEvents.SHEEP_SHEAR, soundSource, 1.0F, 1.0F);
		this.dropFromShearingLootTable(
			serverLevel,
			BuiltInLootTables.SHEAR_SHEEP,
			itemStack,
			(serverLevelx, itemStackx) -> {
				for (int i = 0; i < itemStackx.getCount(); i++) {
					ItemEntity itemEntity = this.spawnAtLocation(serverLevelx, itemStackx.copyWithCount(1), 1.0F);
					if (itemEntity != null) {
						itemEntity.setDeltaMovement(
							itemEntity.getDeltaMovement()
								.add(
									(this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
									this.random.nextFloat() * 0.05F,
									(this.random.nextFloat() - this.random.nextFloat()) * 0.1F
								)
						);
					}
				}
			}
		);
		this.setSheared(true);
	}

	@Override
	public boolean readyForShearing() {
		return this.isAlive() && !this.isSheared() && !this.isBaby();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput valueOutput) {
		super.addAdditionalSaveData(valueOutput);
		valueOutput.putBoolean("Sheared", this.isSheared());
		valueOutput.store("Color", DyeColor.LEGACY_ID_CODEC, this.getColor());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput valueInput) {
		super.readAdditionalSaveData(valueInput);
		this.setSheared(valueInput.getBooleanOr("Sheared", false));
		this.setColor((DyeColor)valueInput.read("Color", DyeColor.LEGACY_ID_CODEC).orElse(DEFAULT_COLOR));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SHEEP_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.SHEEP_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SHEEP_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
	}

	public DyeColor getColor() {
		return DyeColor.byId(this.entityData.get(DATA_WOOL_ID) & 15);
	}

	public void setColor(DyeColor dyeColor) {
		byte b = this.entityData.get(DATA_WOOL_ID);
		this.entityData.set(DATA_WOOL_ID, (byte)(b & 240 | dyeColor.getId() & 15));
	}

	@Nullable
	@Override
	public <T> T get(DataComponentType<? extends T> dataComponentType) {
		return dataComponentType == DataComponents.SHEEP_COLOR
			? castComponentValue((DataComponentType<T>)dataComponentType, this.getColor())
			: super.get(dataComponentType);
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
		this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.SHEEP_COLOR);
		super.applyImplicitComponents(dataComponentGetter);
	}

	@Override
	protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T object) {
		if (dataComponentType == DataComponents.SHEEP_COLOR) {
			this.setColor(castComponentValue(DataComponents.SHEEP_COLOR, object));
			return true;
		} else {
			return super.applyImplicitComponent(dataComponentType, object);
		}
	}

	public boolean isSheared() {
		return (this.entityData.get(DATA_WOOL_ID) & 16) != 0;
	}

	public void setSheared(boolean bl) {
		byte b = this.entityData.get(DATA_WOOL_ID);
		if (bl) {
			this.entityData.set(DATA_WOOL_ID, (byte)(b | 16));
		} else {
			this.entityData.set(DATA_WOOL_ID, (byte)(b & -17));
		}
	}

	public static DyeColor getRandomSheepColor(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos) {
		Holder<Biome> holder = serverLevelAccessor.getBiome(blockPos);
		return SheepColorSpawnRules.getSheepColor(holder, serverLevelAccessor.getRandom());
	}

	@Nullable
	public Sheep getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Sheep sheep = EntityType.SHEEP.create(serverLevel, EntitySpawnReason.BREEDING);
		if (sheep != null) {
			DyeColor dyeColor = this.getColor();
			DyeColor dyeColor2 = ((Sheep)ageableMob).getColor();
			sheep.setColor(DyeColor.getMixedColor(serverLevel, dyeColor, dyeColor2));
		}

		return sheep;
	}

	@Override
	public void ate() {
		super.ate();
		this.setSheared(false);
		if (this.isBaby()) {
			this.ageUp(60);
		}
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		this.setColor(getRandomSheepColor(serverLevelAccessor, this.blockPosition()));
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
	}
}
