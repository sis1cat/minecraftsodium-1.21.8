package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface ResolvableModel {
	void resolveDependencies(ResolvableModel.Resolver resolver);

	@Environment(EnvType.CLIENT)
	public interface Resolver {
		void markDependency(ResourceLocation resourceLocation);
	}
}
