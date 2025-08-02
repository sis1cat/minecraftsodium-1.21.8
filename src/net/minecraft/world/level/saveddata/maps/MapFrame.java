package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record MapFrame(BlockPos pos, int rotation, int entityId) {
	public static final Codec<MapFrame> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				BlockPos.CODEC.fieldOf("pos").forGetter(MapFrame::pos),
				Codec.INT.fieldOf("rotation").forGetter(MapFrame::rotation),
				Codec.INT.fieldOf("entity_id").forGetter(MapFrame::entityId)
			)
			.apply(instance, MapFrame::new)
	);

	public String getId() {
		return frameId(this.pos);
	}

	public static String frameId(BlockPos blockPos) {
		return "frame-" + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
	}
}
