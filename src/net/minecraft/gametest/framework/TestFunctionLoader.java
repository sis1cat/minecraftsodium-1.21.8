package net.minecraft.gametest.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public abstract class TestFunctionLoader {
	private static final List<TestFunctionLoader> loaders = new ArrayList();

	public static void registerLoader(TestFunctionLoader testFunctionLoader) {
		loaders.add(testFunctionLoader);
	}

	public static void runLoaders(Registry<Consumer<GameTestHelper>> registry) {
		for (TestFunctionLoader testFunctionLoader : loaders) {
			testFunctionLoader.load((resourceKey, consumer) -> Registry.register(registry, resourceKey, consumer));
		}
	}

	public abstract void load(BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> biConsumer);
}
