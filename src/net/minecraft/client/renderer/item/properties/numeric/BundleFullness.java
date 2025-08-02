package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record BundleFullness() implements RangeSelectItemModelProperty {
	public static final MapCodec<BundleFullness> MAP_CODEC = MapCodec.unit(new BundleFullness());

	@Override
	public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
		return BundleItem.getFullnessDisplay(itemStack);
	}

	@Override
	public MapCodec<BundleFullness> type() {
		return MAP_CODEC;
	}
}
