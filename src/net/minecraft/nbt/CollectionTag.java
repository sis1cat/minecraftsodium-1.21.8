package net.minecraft.nbt;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface CollectionTag extends Iterable<Tag>, Tag permits ListTag, ByteArrayTag, IntArrayTag, LongArrayTag {
	void clear();

	boolean setTag(int i, Tag tag);

	boolean addTag(int i, Tag tag);

	Tag remove(int i);

	Tag get(int i);

	int size();

	default boolean isEmpty() {
		return this.size() == 0;
	}

	default Iterator<Tag> iterator() {
		return new Iterator<Tag>() {
			private int index;

			public boolean hasNext() {
				return this.index < CollectionTag.this.size();
			}

			public Tag next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				} else {
					return CollectionTag.this.get(this.index++);
				}
			}
		};
	}

	default Stream<Tag> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}
}
