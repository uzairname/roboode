package upackage;

import robocode.util.Utils;

public class Calc {
	
	public static double findIncline (double distance) {
		if (distance >= 120) {
			return (-100 * Math.pow(1.003, -1*distance)) + 90;
		} else {
			return (0.79*distance) - 75;
		}
	}
	
	public static boolean isInBounds (double x, double y, double fieldWidth, double fieldHeight, double robotWidth, double robotHeight) {
		return (x > robotWidth) && (fieldWidth - x > robotWidth) && (y > robotHeight) && (fieldHeight - y > robotHeight);
	}
	
	public static double moveInBounds (double movementDir, double heading, double x, double y, double fieldWidth, double fieldHeight, double robotWidth, double robotHeight) {
		boolean boundsRight = !(fieldWidth - x > robotWidth);
		boolean boundsUp = !(fieldHeight - y > robotHeight);
		boolean boundsLeft = !(x > robotWidth);
		boolean boundsDown = !(y > robotHeight);
		
		if (boundsLeft) {
			System.out.println("left");
			if (Utils.normalAbsoluteAngleDegrees(heading + 0) >= 180) {
				return -1;
			} else {
				return 1;
			}
		} else if (boundsDown) {
			System.out.println("down");
			if (Utils.normalAbsoluteAngleDegrees(heading + 90) >= 180) {
				return -1;
			} else {
				return 1;
			}
		} else if (boundsRight) {
			System.out.println("right");
			if (Utils.normalAbsoluteAngleDegrees(heading + 180) >= 180) {
				return -1;
			} else {
				return 1;
			}
		} else if (boundsUp) {
			System.out.println("up");
			if (Utils.normalAbsoluteAngleDegrees(heading + 270) >= 180) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return movementDir;
		}
	}
	
}