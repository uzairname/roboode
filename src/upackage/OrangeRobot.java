package upackage;

//import javafx.scene.paint.Color;
import robocode.*;
import robocode.util.Utils;

public class OrangeRobot extends AdvancedRobot {

	public void run() {

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		setColors();

		while (true) {
			setTurnRadarRight(Rules.RADAR_TURN_RATE);
			execute();
		}
	}


	private void setColors() {
		setBodyColor(java.awt.Color.orange);
		setGunColor(java.awt.Color.red);
		setRadarColor(java.awt.Color.orange);
		setBulletColor(java.awt.Color.lightGray);
		setScanColor(java.awt.Color.black);
	}


	public void onScannedRobot(ScannedRobotEvent e) {

		double firePower = 1;

		double actualBearing = mod(e.getBearing() + getHeading(), 360);
		System.out.println("my mod=" + actualBearing);

		double degreesO = findO(actualBearing, e.getHeading());
		double L = e.getDistance();
		double v = e.getVelocity();
		double s = 20 - (3 * firePower);
		double possibleAs[] = findPossibleA(convertToRadians(degreesO), L, v, s);
		double realA = possibleAs[findA(degreesO)];
		
		double shootAngle;
		if (v == 0) {
			shootAngle = 0;
		} else {
			shootAngle = convertToDegrees(findShootAngle(realA, L, convertToRadians(degreesO)));
		}
		double absoluteShootAngle = shootAngle + actualBearing;

		double radarInitialTurn = Utils.normalRelativeAngleDegrees(actualBearing - getRadarHeading());
		double extraTurn = convertToDegrees(Math.min((Math.atan(5 / e.getDistance())), Rules.RADAR_TURN_RATE_RADIANS));
		double radarTurn = findRadarTurn(radarInitialTurn, extraTurn);

		System.out.println("ExtraTurn: " + extraTurn);
		System.out.println("Initial" + radarInitialTurn);
		System.out.println("radarTurn: " + radarTurn);

		setTurnRadarRight(radarTurn);
		
		double angleNeeded = Utils.normalRelativeAngleDegrees(absoluteShootAngle - getGunHeading());
//		double sign = Math.ceil( (angleNeeded) /360);
		if(angleNeeded > 0) {
			setTurnGunRight(Math.min(angleNeeded, Rules.GUN_TURN_RATE * 1));
		} else if (angleNeeded < 0) {
			setTurnGunRight(Math.max(angleNeeded, Rules.GUN_TURN_RATE * -1));
		}
		
		if (((absoluteShootAngle + 1) >= getGunHeading()) && (getGunHeading() >= (absoluteShootAngle - 1))) {
			System.out.println("Aim");
		} else {
			System.out.println();
		}
		System.out.println("absolute shoot angle: " + absoluteShootAngle);
		System.out.println("shoot angle: " + shootAngle + "  actual Bearing: " + actualBearing);
		System.out.println();
		System.out.println("gun heading" + getGunHeading());
		System.out.println("Angle needed" +  angleNeeded );
		

		System.out.println();
		System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=");
		System.out.println();
		execute();
	}

	public double findRadarTurn(double radarInitialTurn, double extraTurn) {
		if (radarInitialTurn < 0) {
			return radarInitialTurn - extraTurn;
		} else if (radarInitialTurn > 0) {
			return radarInitialTurn + extraTurn;
		} else {
			return extraTurn;
		}
	}

	private double mod(double x, double n) {
		double r = x % n;
		if (r < 0) {
			r += n;
		}
		return r;
	}

	private double findShootAngle(double a, double L, double O) {
		return Math.atan((a * Math.tan(O)) / (L + a));
	}

	private double findO(double Bearing, double Heading) {

		if (((Bearing - Heading) % 360) >= 0) {
			return (Bearing - Heading) % 360;
		} else {
			return ((Bearing - Heading) % 360) + 360;
		}
	}

	private double[] findPossibleA(double O, double L, double v, double s) {
		double quadratica = (((s * s) / (v * v)) - 1) / (Math.cos(O) * Math.cos(O));
		double quadraticb = 2 * L * -1;
		double quadraticc = L * L * -1;
		return solve(quadratica, quadraticb, quadraticc);
	}

	private int findA(double degO) {
		return (90 <= degO && degO <= 270) ? 0 : 1;
	}

	private double convertToRadians(double degrees) {
		return (Math.PI * degrees) / 180;
	}

	private double convertToDegrees(double radians) {
		return (radians * 180) / Math.PI;
	}

	private double[] solve(double a, double b, double c) {

		double[] roots = new double[2];
		double disc = Math.sqrt((b * b) - (4 * a * c));
		roots[0] = ((-1 * b) - disc) / (2 * a);
		roots[1] = ((-1 * b) + disc) / (2 * a);
		return roots;
	}
}