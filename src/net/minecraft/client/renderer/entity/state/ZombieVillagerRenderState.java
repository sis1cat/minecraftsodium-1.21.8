package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.npc.VillagerData;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ZombieVillagerRenderState extends ZombieRenderState implements VillagerDataHolderRenderState {
	@Nullable
	public VillagerData villagerData;

	@Nullable
	@Override
	public VillagerData getVillagerData() {
		return this.villagerData;
	}
}
