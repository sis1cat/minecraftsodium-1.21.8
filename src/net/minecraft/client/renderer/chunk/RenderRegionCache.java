package net.minecraft.client.renderer.chunk;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.fabric.impl.blockview.client.RenderDataMapConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RenderRegionCache {
	private final Long2ObjectMap<SectionCopy> sectionCopyCache = new Long2ObjectOpenHashMap<>();

	private static final AtomicInteger ERROR_COUNTER = new AtomicInteger();
	private static final Logger LOGGER = LoggerFactory.getLogger(RenderRegionCache.class);

	public RenderSectionRegion createRegion(Level level, long centralSectionPosLong) {
		int i = SectionPos.x(centralSectionPosLong);
		int j = SectionPos.y(centralSectionPosLong);
		int k = SectionPos.z(centralSectionPosLong);

		int sectionXMin = i - 1;
		int sectionYMin = j - 1;
		int sectionZMin = k - 1;
		int sectionXMax = i + 1;
		int sectionYMax = j + 1;
		int sectionZMax = k + 1;

		SectionCopy[] sectionCopys = new SectionCopy[27];
		Long2ObjectOpenHashMap<Object> renderDataMap = null;

		Set<Long> processedChunkPositions = new HashSet<>();

		final int blockXMin = SectionPos.sectionToBlockCoord(sectionXMin);
		final int blockYMin = SectionPos.sectionToBlockCoord(sectionYMin);
		final int blockZMin = SectionPos.sectionToBlockCoord(sectionZMin);

		final int blockXMax = SectionPos.sectionToBlockCoord(sectionXMax) + 15;
		final int blockYMax = SectionPos.sectionToBlockCoord(sectionYMax) + 15;
		final int blockZMax = SectionPos.sectionToBlockCoord(sectionZMax) + 15;


		for (int secZ = sectionZMin; secZ <= sectionZMax; secZ++) {
			for (int secY = sectionYMin; secY <= sectionYMax; secY++) {
				for (int secX = sectionXMin; secX <= sectionXMax; secX++) {

					int index = RenderSectionRegion.index(sectionXMin, sectionYMin, sectionZMin, secX, secY, secZ);
					sectionCopys[index] = this.getSectionDataCopy(level, secX, secY, secZ);


					long chunkPosLong = SectionPos.asLong(secX, 0, secZ);
					if (processedChunkPositions.add(chunkPosLong)) {
						LevelChunk chunk = level.getChunk(secX, secZ);


						if (chunk.getBlockEntities().isEmpty()) {
							continue;
						}

						while (true) {
							try {
								renderDataMap = gatherRenderDataForChunk(
										chunk, renderDataMap,
										blockXMin, blockYMin, blockZMin,
										blockXMax, blockYMax, blockZMax
								);
								break;
							} catch (ConcurrentModificationException e) {
								final int count = ERROR_COUNTER.incrementAndGet();
								if (count <= 5) {
									LOGGER.warn("[Block Entity Render Data] Encountered CME during render region build. Retrying.", e);
									if (count == 5) {
										LOGGER.info("[Block Entity Render Data] Subsequent exceptions will be suppressed.");
									}
								}
							}
						}
					}
				}
			}
		}

		RenderSectionRegion rendererRegion = new RenderSectionRegion(level, sectionXMin, sectionYMin, sectionZMin, sectionCopys);


		if (renderDataMap != null) {

			((RenderDataMapConsumer) rendererRegion).fabric_acceptRenderDataMap(renderDataMap);
		}

		return rendererRegion;
	}

	private SectionCopy getSectionDataCopy(Level level, int i, int j, int k) {
		return this.sectionCopyCache.computeIfAbsent(SectionPos.asLong(i, j, k), l -> {
			LevelChunk levelChunk = level.getChunk(i, k);
			return new SectionCopy(levelChunk, levelChunk.getSectionIndexFromSectionY(j));
		});
	}


	private static Long2ObjectOpenHashMap<Object> gatherRenderDataForChunk(LevelChunk chunk, Long2ObjectOpenHashMap<Object> map,
																		   int minX, int minY, int minZ,
																		   int maxX, int maxY, int maxZ) {
		for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
			final BlockPos pos = entry.getKey();


			if (pos.getX() >= minX && pos.getX() <= maxX &&
					pos.getY() >= minY && pos.getY() <= maxY &&
					pos.getZ() >= minZ && pos.getZ() <= maxZ) {

				final Object data = entry.getValue().getRenderData();

				if (data != null) {
					if (map == null) {
						map = new Long2ObjectOpenHashMap<>();
					}
					map.put(pos.asLong(), data);
				}
			}
		}
		return map;
	}
}