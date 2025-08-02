package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class Time extends NeedleDirectionHelper implements RangeSelectItemModelProperty {
	public static final MapCodec<Time> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				Codec.BOOL.optionalFieldOf("wobble", true).forGetter(NeedleDirectionHelper::wobble), Time.TimeSource.CODEC.fieldOf("source").forGetter(time -> time.source)
			)
			.apply(instance, Time::new)
	);
	private final Time.TimeSource source;
	private final RandomSource randomSource = RandomSource.create();
	private final NeedleDirectionHelper.Wobbler wobbler;

	public Time(boolean bl, Time.TimeSource timeSource) {
		super(bl);
		this.source = timeSource;
		this.wobbler = this.newWobbler(0.9F);
	}

	@Override
	protected float calculate(ItemStack itemStack, ClientLevel clientLevel, int i, Entity entity) {
		float f = this.source.get(clientLevel, itemStack, entity, this.randomSource);
		long l = clientLevel.getGameTime();
		if (this.wobbler.shouldUpdate(l)) {
			this.wobbler.update(l, f);
		}

		return this.wobbler.rotation();
	}

	@Override
	public MapCodec<Time> type() {
		return MAP_CODEC;
	}

	@Environment(EnvType.CLIENT)
	public static enum TimeSource implements StringRepresentable {
		RANDOM("random") {
			@Override
			public float get(ClientLevel clientLevel, ItemStack itemStack, Entity entity, RandomSource randomSource) {
				return randomSource.nextFloat();
			}
		},
		DAYTIME("daytime") {
			@Override
			public float get(ClientLevel clientLevel, ItemStack itemStack, Entity entity, RandomSource randomSource) {
				return clientLevel.getTimeOfDay(1.0F);
			}
		},
		MOON_PHASE("moon_phase") {
			@Override
			public float get(ClientLevel clientLevel, ItemStack itemStack, Entity entity, RandomSource randomSource) {
				return clientLevel.getMoonPhase() / 8.0F;
			}
		};

		public static final Codec<Time.TimeSource> CODEC = StringRepresentable.fromEnum(Time.TimeSource::values);
		private final String name;

		TimeSource(final String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		abstract float get(ClientLevel clientLevel, ItemStack itemStack, Entity entity, RandomSource randomSource);
	}
}
