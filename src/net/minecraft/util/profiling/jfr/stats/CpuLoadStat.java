package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record CpuLoadStat(double jvm, double userJvm, double system) {
	public static CpuLoadStat from(RecordedEvent recordedEvent) {
		return new CpuLoadStat(recordedEvent.getFloat("jvmSystem"), recordedEvent.getFloat("jvmUser"), recordedEvent.getFloat("machineTotal"));
	}
}
