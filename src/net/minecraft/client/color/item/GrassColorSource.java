package net.minecraft.client.color.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GrassColor;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record GrassColorSource(float temperature, float downfall) implements ItemTintSource {
	public static final MapCodec<GrassColorSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("temperature").forGetter(GrassColorSource::temperature),
				ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("downfall").forGetter(GrassColorSource::downfall)
			)
			.apply(instance, GrassColorSource::new)
	);

	public GrassColorSource() {
		this(0.5F, 1.0F);
	}

	@Override
	public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
		return GrassColor.get(this.temperature, this.downfall);
	}

	@Override
	public MapCodec<GrassColorSource> type() {
		return MAP_CODEC;
	}
}
