package net.minecraft.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;

public class EncoderCache {
	final LoadingCache<EncoderCache.Key<?, ?>, DataResult<?>> cache;

	public EncoderCache(int i) {
		this.cache = CacheBuilder.newBuilder().maximumSize(i).concurrencyLevel(1).softValues().build(new CacheLoader<EncoderCache.Key<?, ?>, DataResult<?>>() {
			public DataResult<?> load(EncoderCache.Key<?, ?> key) {
				return key.resolve();
			}
		});
	}

	public <A> Codec<A> wrap(final Codec<A> pCodec) {
		return new Codec<A>() {
			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> p_335845_, T p_329817_) {
				return pCodec.decode(p_335845_, p_329817_);
			}

			@Override
			public <T> DataResult<T> encode(A p_328409_, DynamicOps<T> p_330058_, T p_328392_) {
				return (DataResult<T>) EncoderCache.this.cache
						.getUnchecked(new EncoderCache.Key<>(pCodec, p_328409_, p_330058_))
						.map(p_336406_ -> p_336406_ instanceof Tag tag ? tag.copy() : p_336406_);
			}
		};
	}

	record Key<A, T>(Codec<A> codec, A value, DynamicOps<T> ops) {
		public DataResult<T> resolve() {
			return this.codec.encodeStart(this.ops, this.value);
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else {
				return !(object instanceof EncoderCache.Key<?, ?> key) ? false : this.codec == key.codec && this.value.equals(key.value) && this.ops.equals(key.ops);
			}
		}

		public int hashCode() {
			int i = System.identityHashCode(this.codec);
			i = 31 * i + this.value.hashCode();
			return 31 * i + this.ops.hashCode();
		}
	}
}
