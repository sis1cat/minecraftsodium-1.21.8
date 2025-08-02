package net.minecraft.client.color.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapItemColor;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record MapColor(int defaultColor) implements ItemTintSource {
	public static final MapCodec<MapColor> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(MapColor::defaultColor)).apply(instance, MapColor::new)
	);

	public MapColor() {
		this(MapItemColor.DEFAULT.rgb());
	}

	@Override
	public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
		MapItemColor mapItemColor = itemStack.get(DataComponents.MAP_COLOR);
		return mapItemColor != null ? ARGB.opaque(mapItemColor.rgb()) : ARGB.opaque(this.defaultColor);
	}

	@Override
	public MapCodec<MapColor> type() {
		return MAP_CODEC;
	}
}
