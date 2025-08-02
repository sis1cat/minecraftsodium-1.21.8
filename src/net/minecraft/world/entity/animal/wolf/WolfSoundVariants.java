package net.minecraft.world.entity.animal.wolf;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;

public class WolfSoundVariants {
	public static final ResourceKey<WolfSoundVariant> CLASSIC = createKey(WolfSoundVariants.SoundSet.CLASSIC);
	public static final ResourceKey<WolfSoundVariant> PUGLIN = createKey(WolfSoundVariants.SoundSet.PUGLIN);
	public static final ResourceKey<WolfSoundVariant> SAD = createKey(WolfSoundVariants.SoundSet.SAD);
	public static final ResourceKey<WolfSoundVariant> ANGRY = createKey(WolfSoundVariants.SoundSet.ANGRY);
	public static final ResourceKey<WolfSoundVariant> GRUMPY = createKey(WolfSoundVariants.SoundSet.GRUMPY);
	public static final ResourceKey<WolfSoundVariant> BIG = createKey(WolfSoundVariants.SoundSet.BIG);
	public static final ResourceKey<WolfSoundVariant> CUTE = createKey(WolfSoundVariants.SoundSet.CUTE);

	private static ResourceKey<WolfSoundVariant> createKey(WolfSoundVariants.SoundSet soundSet) {
		return ResourceKey.create(Registries.WOLF_SOUND_VARIANT, ResourceLocation.withDefaultNamespace(soundSet.getIdentifier()));
	}

	public static void bootstrap(BootstrapContext<WolfSoundVariant> bootstrapContext) {
		register(bootstrapContext, CLASSIC, WolfSoundVariants.SoundSet.CLASSIC);
		register(bootstrapContext, PUGLIN, WolfSoundVariants.SoundSet.PUGLIN);
		register(bootstrapContext, SAD, WolfSoundVariants.SoundSet.SAD);
		register(bootstrapContext, ANGRY, WolfSoundVariants.SoundSet.ANGRY);
		register(bootstrapContext, GRUMPY, WolfSoundVariants.SoundSet.GRUMPY);
		register(bootstrapContext, BIG, WolfSoundVariants.SoundSet.BIG);
		register(bootstrapContext, CUTE, WolfSoundVariants.SoundSet.CUTE);
	}

	private static void register(
		BootstrapContext<WolfSoundVariant> bootstrapContext, ResourceKey<WolfSoundVariant> resourceKey, WolfSoundVariants.SoundSet soundSet
	) {
		bootstrapContext.register(resourceKey, (WolfSoundVariant)SoundEvents.WOLF_SOUNDS.get(soundSet));
	}

	public static Holder<WolfSoundVariant> pickRandomSoundVariant(RegistryAccess registryAccess, RandomSource randomSource) {
		return (Holder<WolfSoundVariant>)registryAccess.lookupOrThrow(Registries.WOLF_SOUND_VARIANT).getRandom(randomSource).orElseThrow();
	}

	public static enum SoundSet {
		CLASSIC("classic", ""),
		PUGLIN("puglin", "_puglin"),
		SAD("sad", "_sad"),
		ANGRY("angry", "_angry"),
		GRUMPY("grumpy", "_grumpy"),
		BIG("big", "_big"),
		CUTE("cute", "_cute");

		private final String identifier;
		private final String soundEventSuffix;

		private SoundSet(final String string2, final String string3) {
			this.identifier = string2;
			this.soundEventSuffix = string3;
		}

		public String getIdentifier() {
			return this.identifier;
		}

		public String getSoundEventSuffix() {
			return this.soundEventSuffix;
		}
	}
}
