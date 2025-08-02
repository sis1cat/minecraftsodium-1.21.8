package net.minecraft.world.entity.animal;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

public abstract class ShoulderRidingEntity extends TamableAnimal {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int RIDE_COOLDOWN = 100;
	private int rideCooldownCounter;

	protected ShoulderRidingEntity(EntityType<? extends ShoulderRidingEntity> entityType, Level level) {
		super(entityType, level);
	}

	public boolean setEntityOnShoulder(ServerPlayer serverPlayer) {
		try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
			TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, this.registryAccess());
			this.saveWithoutId(tagValueOutput);
			tagValueOutput.putString("id", this.getEncodeId());
			if (serverPlayer.setEntityOnShoulder(tagValueOutput.buildResult())) {
				this.discard();
				return true;
			}
		}

		return false;
	}

	@Override
	public void tick() {
		this.rideCooldownCounter++;
		super.tick();
	}

	public boolean canSitOnShoulder() {
		return this.rideCooldownCounter > 100;
	}
}
