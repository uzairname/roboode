package upackage;

import java.awt.Color;

public class Colors {

	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color WHITE = new Color(255, 255, 255);

	public static Color makeColor(int saturation, long turn) {
		int[] colorRatios = colorRatios(turn);
		multiply(saturation, colorRatios);
		return new Color(colorRatios[0], colorRatios[1], colorRatios[2]);
	}

	private static void multiply (int multiplier, int[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] *= multiplier;
		}
	}
	
	public static int [] colorRatios (double turn) {
		int red   = (int)Math.min(8, Math.abs( ((turn +  8) % 24) - 12));
		int green = (int)Math.min(8, Math.abs( ((turn + 16) % 24) - 12));
		int blue  = (int)Math.min(8, Math.abs( ((turn + 24) % 24) - 12));
		red*=4;
		blue*=4;
		green*=4;
		return new int [] {red, blue, green};
	}
}
