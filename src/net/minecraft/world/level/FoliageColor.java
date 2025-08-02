package net.minecraft.world.level;

public class FoliageColor {
	public static final int FOLIAGE_EVERGREEN = -10380959;
	public static final int FOLIAGE_BIRCH = -8345771;
	public static final int FOLIAGE_DEFAULT = -12012264;
	public static final int FOLIAGE_MANGROVE = -7158200;
	public static int[] pixels = new int[65536];

	public static void init(int[] is) {
		pixels = is;
	}

	public static int get(double d, double e) {
		return ColorMapColorUtil.get(d, e, pixels, -12012264);
	}
}
