package net.minecraft.gametest.framework;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface GameTestEnvironments {
	String DEFAULT = "default";
	ResourceKey<TestEnvironmentDefinition> DEFAULT_KEY = create("default");

	private static ResourceKey<TestEnvironmentDefinition> create(String string) {
		return ResourceKey.create(Registries.TEST_ENVIRONMENT, ResourceLocation.withDefaultNamespace(string));
	}

	static void bootstrap(BootstrapContext<TestEnvironmentDefinition> bootstrapContext) {
		bootstrapContext.register(DEFAULT_KEY, new TestEnvironmentDefinition.AllOf(List.of()));
	}
}
