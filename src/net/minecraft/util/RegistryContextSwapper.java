package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;

public interface RegistryContextSwapper {
	<T> DataResult<T> swapTo(Codec<T> codec, T object, HolderLookup.Provider provider);
}
