package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public record FileIOStat(Duration duration, @Nullable String path, long bytes) {
	public static FileIOStat.Summary summary(Duration duration, List<FileIOStat> list) {
		long l = list.stream().mapToLong(fileIOStat -> fileIOStat.bytes).sum();
		return new FileIOStat.Summary(
			l,
			(double)l / duration.getSeconds(),
			list.size(),
			(double)list.size() / duration.getSeconds(),
			(Duration)list.stream().map(FileIOStat::duration).reduce(Duration.ZERO, Duration::plus),
			((Map)list.stream()
					.filter(fileIOStat -> fileIOStat.path != null)
					.collect(Collectors.groupingBy(fileIOStat -> fileIOStat.path, Collectors.summingLong(fileIOStat -> fileIOStat.bytes))))
				.entrySet()
				.stream()
				.sorted(Entry.comparingByValue().reversed())
				.map(entry -> Pair.of((String)((Entry<Object, Object>)entry).getKey(), (Long)((Entry<Object, Object>)entry).getValue()))
				.limit(10L)
				.toList()
		);
	}

	public record Summary(
		long totalBytes, double bytesPerSecond, long counts, double countsPerSecond, Duration timeSpentInIO, List<Pair<String, Long>> topTenContributorsByTotalBytes
	) {
	}
}
