package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class LivingEntityRenderState extends EntityRenderState {
	public float bodyRot;
	public float yRot;
	public float xRot;
	public float deathTime;
	public float walkAnimationPos;
	public float walkAnimationSpeed;
	public float scale = 1.0F;
	public float ageScale = 1.0F;
	public boolean isUpsideDown;
	public boolean isFullyFrozen;
	public boolean isBaby;
	public boolean isInWater;
	public boolean isAutoSpinAttack;
	public boolean hasRedOverlay;
	public boolean isInvisibleToPlayer;
	public boolean appearsGlowing;
	@Nullable
	public Direction bedOrientation;
	@Nullable
	public Component customName;
	public Pose pose = Pose.STANDING;
	public final ItemStackRenderState headItem = new ItemStackRenderState();
	public float wornHeadAnimationPos;
	@Nullable
	public SkullBlock.Type wornHeadType;
	@Nullable
	public ResolvableProfile wornHeadProfile;

	public boolean hasPose(Pose pose) {
		return this.pose == pose;
	}
}
