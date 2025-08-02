package net.minecraft.world.item;

import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.ThrownSplashPotion;
import net.minecraft.world.level.Level;

public class SplashPotionItem extends ThrowablePotionItem {
	public SplashPotionItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		level.playSound(
			null,
			player.getX(),
			player.getY(),
			player.getZ(),
			SoundEvents.SPLASH_POTION_THROW,
			SoundSource.PLAYERS,
			0.5F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
		);
		return super.use(level, player, interactionHand);
	}

	@Override
	protected AbstractThrownPotion createPotion(ServerLevel serverLevel, LivingEntity livingEntity, ItemStack itemStack) {
		return new ThrownSplashPotion(serverLevel, livingEntity, itemStack);
	}

	@Override
	protected AbstractThrownPotion createPotion(Level level, Position position, ItemStack itemStack) {
		return new ThrownSplashPotion(level, position.x(), position.y(), position.z(), itemStack);
	}
}
