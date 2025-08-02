package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface SearchTree<T> {
	static <T> SearchTree<T> empty() {
		return string -> List.of();
	}

	static <T> SearchTree<T> plainText(List<T> pContents, Function<T, Stream<String>> pFilter) {
		if (pContents.isEmpty()) {
			return empty();
		} else {
			SuffixArray<T> suffixarray = new SuffixArray<>();

			for (T t : pContents) {
				pFilter.apply(t).forEach(p_342612_ -> suffixarray.add(t, p_342612_.toLowerCase(Locale.ROOT)));
			}

			suffixarray.generate();
			return suffixarray::search;
		}
	}

	List<T> search(String string);
}
