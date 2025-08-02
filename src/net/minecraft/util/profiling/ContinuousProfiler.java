package net.minecraft.util.profiling;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public class ContinuousProfiler {
	private final LongSupplier realTime;
	private final IntSupplier tickCount;
	private final BooleanSupplier suppressWarnings;
	private ProfileCollector profiler = InactiveProfiler.INSTANCE;

	public ContinuousProfiler(LongSupplier longSupplier, IntSupplier intSupplier, BooleanSupplier booleanSupplier) {
		this.realTime = longSupplier;
		this.tickCount = intSupplier;
		this.suppressWarnings = booleanSupplier;
	}

	public boolean isEnabled() {
		return this.profiler != InactiveProfiler.INSTANCE;
	}

	public void disable() {
		this.profiler = InactiveProfiler.INSTANCE;
	}

	public void enable() {
		this.profiler = new ActiveProfiler(this.realTime, this.tickCount, this.suppressWarnings);
	}

	public ProfilerFiller getFiller() {
		return this.profiler;
	}

	public ProfileResults getResults() {
		return this.profiler.getResults();
	}
}
