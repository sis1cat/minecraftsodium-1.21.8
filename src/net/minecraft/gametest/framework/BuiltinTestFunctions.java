package net.minecraft.gametest.framework;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class BuiltinTestFunctions extends TestFunctionLoader {
	public static final ResourceKey<Consumer<GameTestHelper>> ALWAYS_PASS = create("always_pass");
	public static final Consumer<GameTestHelper> ALWAYS_PASS_INSTANCE = GameTestHelper::succeed;

	private static ResourceKey<Consumer<GameTestHelper>> create(String string) {
		return ResourceKey.create(Registries.TEST_FUNCTION, ResourceLocation.withDefaultNamespace(string));
	}

	public static Consumer<GameTestHelper> bootstrap(Registry<Consumer<GameTestHelper>> registry) {
		registerLoader(new BuiltinTestFunctions());
		runLoaders(registry);
		return ALWAYS_PASS_INSTANCE;
	}

	@Override
	public void load(BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> biConsumer) {
		biConsumer.accept(ALWAYS_PASS, ALWAYS_PASS_INSTANCE);
	}
}
