package com.mojang.blaze3d;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.GpuDevice;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GraphicsWorkarounds {
	private static final List<String> INTEL_GEN11_CORE = List.of(
		"i3-1000g1",
		"i3-1000g4",
		"i3-1000ng4",
		"i3-1005g1",
		"i3-l13g4",
		"i5-1030g4",
		"i5-1030g7",
		"i5-1030ng7",
		"i5-1034g1",
		"i5-1035g1",
		"i5-1035g4",
		"i5-1035g7",
		"i5-1038ng7",
		"i5-l16g7",
		"i7-1060g7",
		"i7-1060ng7",
		"i7-1065g7",
		"i7-1068g7",
		"i7-1068ng7"
	);
	private static final List<String> INTEL_GEN11_ATOM = List.of("x6211e", "x6212re", "x6214re", "x6413e", "x6414re", "x6416re", "x6425e", "x6425re", "x6427fe");
	private static final List<String> INTEL_GEN11_CELERON = List.of("j6412", "j6413", "n4500", "n4505", "n5095", "n5095a", "n5100", "n5105", "n6210", "n6211");
	private static final List<String> INTEL_GEN11_PENTIUM = List.of("6805", "j6426", "n6415", "n6000", "n6005");
	@Nullable
	private static GraphicsWorkarounds instance;
	private final WeakReference<GpuDevice> gpuDevice;
	private final boolean alwaysCreateFreshImmediateBuffer;

	private GraphicsWorkarounds(GpuDevice gpuDevice) {
		this.gpuDevice = new WeakReference(gpuDevice);
		this.alwaysCreateFreshImmediateBuffer = isIntelGen11(gpuDevice);
	}

	public static GraphicsWorkarounds get(GpuDevice gpuDevice) {
		GraphicsWorkarounds graphicsWorkarounds = instance;
		if (graphicsWorkarounds == null || graphicsWorkarounds.gpuDevice.get() != gpuDevice) {
			instance = graphicsWorkarounds = new GraphicsWorkarounds(gpuDevice);
		}

		return graphicsWorkarounds;
	}

	public boolean alwaysCreateFreshImmediateBuffer() {
		return this.alwaysCreateFreshImmediateBuffer;
	}

	private static boolean isIntelGen11(GpuDevice gpuDevice) {
		String string = GLX._getCpuInfo().toLowerCase(Locale.ROOT);
		String string2 = gpuDevice.getRenderer().toLowerCase(Locale.ROOT);
		if (!string.contains("intel") || !string2.contains("intel") || string2.contains("mesa")) {
			return false;
		} else if (string2.endsWith("gen11")) {
			return true;
		} else {
			return !string2.contains("uhd graphics") && !string2.contains("iris")
				? false
				: string.contains("atom") && INTEL_GEN11_ATOM.stream().anyMatch(string::contains)
					|| string.contains("celeron") && INTEL_GEN11_CELERON.stream().anyMatch(string::contains)
					|| string.contains("pentium") && INTEL_GEN11_PENTIUM.stream().anyMatch(string::contains)
					|| INTEL_GEN11_CORE.stream().anyMatch(string::contains);
		}
	}
}
