package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CompassAngle implements RangeSelectItemModelProperty {
	public static final MapCodec<CompassAngle> MAP_CODEC = CompassAngleState.MAP_CODEC.xmap(CompassAngle::new, compassAngle -> compassAngle.state);
	private final CompassAngleState state;

	public CompassAngle(boolean bl, CompassAngleState.CompassTarget compassTarget) {
		this(new CompassAngleState(bl, compassTarget));
	}

	private CompassAngle(CompassAngleState compassAngleState) {
		this.state = compassAngleState;
	}

	@Override
	public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
		return this.state.get(itemStack, clientLevel, livingEntity, i);
	}

	@Override
	public MapCodec<CompassAngle> type() {
		return MAP_CODEC;
	}
}
