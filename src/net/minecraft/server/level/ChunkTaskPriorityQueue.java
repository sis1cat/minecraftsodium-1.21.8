package net.minecraft.server.level;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class ChunkTaskPriorityQueue {
	public static final int PRIORITY_LEVEL_COUNT = ChunkLevel.MAX_LEVEL + 2;
	private final List<Long2ObjectLinkedOpenHashMap<List<Runnable>>> queuesPerPriority = IntStream.range(0, PRIORITY_LEVEL_COUNT)
			.mapToObj(p_140520_ -> new Long2ObjectLinkedOpenHashMap<List<Runnable>>())
			.toList();
	private volatile int topPriorityQueueIndex = PRIORITY_LEVEL_COUNT;
	private final String name;

	public ChunkTaskPriorityQueue(String string) {
		this.name = string;
	}

	protected void resortChunkTasks(int pQueueLevel, ChunkPos pChunkPos, int pTicketLevel) {
		if (pQueueLevel < PRIORITY_LEVEL_COUNT) {
			Long2ObjectLinkedOpenHashMap<List<Runnable>> long2objectlinkedopenhashmap = this.queuesPerPriority.get(pQueueLevel);
			List<Runnable> list = long2objectlinkedopenhashmap.remove(pChunkPos.toLong());
			if (pQueueLevel == this.topPriorityQueueIndex) {
				while (this.hasWork() && this.queuesPerPriority.get(this.topPriorityQueueIndex).isEmpty()) {
					this.topPriorityQueueIndex++;
				}
			}

			if (list != null && !list.isEmpty()) {
				this.queuesPerPriority.get(pTicketLevel).computeIfAbsent(pChunkPos.toLong(), p_140547_ -> Lists.newArrayList()).addAll(list);
				this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, pTicketLevel);
			}
		}
	}

	protected void submit(Runnable pTask, long pChunkPos, int pQueueLevel) {
		this.queuesPerPriority.get(pQueueLevel).computeIfAbsent(pChunkPos, p_140545_ -> Lists.newArrayList()).add(pTask);
		this.topPriorityQueueIndex = Math.min(this.topPriorityQueueIndex, pQueueLevel);
	}

	protected void release(long l, boolean bl) {
		for (Long2ObjectLinkedOpenHashMap<List<Runnable>> long2ObjectLinkedOpenHashMap : this.queuesPerPriority) {
			List<Runnable> list = long2ObjectLinkedOpenHashMap.get(l);
			if (list != null) {
				if (bl) {
					list.clear();
				}

				if (list.isEmpty()) {
					long2ObjectLinkedOpenHashMap.remove(l);
				}
			}
		}

		while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap)this.queuesPerPriority.get(this.topPriorityQueueIndex)).isEmpty()) {
			this.topPriorityQueueIndex++;
		}
	}

	@Nullable
	public ChunkTaskPriorityQueue.TasksForChunk pop() {
		if (!this.hasWork()) {
			return null;
		} else {
			int i = this.topPriorityQueueIndex;
			Long2ObjectLinkedOpenHashMap<List<Runnable>> long2ObjectLinkedOpenHashMap = (Long2ObjectLinkedOpenHashMap<List<Runnable>>)this.queuesPerPriority.get(i);
			long l = long2ObjectLinkedOpenHashMap.firstLongKey();
			List<Runnable> list = long2ObjectLinkedOpenHashMap.removeFirst();

			while (this.hasWork() && ((Long2ObjectLinkedOpenHashMap)this.queuesPerPriority.get(this.topPriorityQueueIndex)).isEmpty()) {
				this.topPriorityQueueIndex++;
			}

			return new ChunkTaskPriorityQueue.TasksForChunk(l, list);
		}
	}

	public boolean hasWork() {
		return this.topPriorityQueueIndex < PRIORITY_LEVEL_COUNT;
	}

	public String toString() {
		return this.name + " " + this.topPriorityQueueIndex + "...";
	}

	public record TasksForChunk(long chunkPos, List<Runnable> tasks) {
	}
}
