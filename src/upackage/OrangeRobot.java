package upackage;
import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.lang.reflect.Array;

public class OrangeRobot extends AdvancedRobot {

	public void run() {

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		setBodyColor(new Color(200, 100, 0));
		setGunColor(new Color(190, 255, 100));
		setRadarColor(new Color(150, 50, 0));
		setScanColor(new Color(255, 255, 255));
		setBulletColor(new Color(255, 190, 0));

		while (true) {
			setTurnRadarRight(Rules.RADAR_TURN_RATE); 
			execute();
		}
	}


//	double [] previousStraightDurations = new double[20];
//	double straightDurationIndex = 0;
	double currentStraightHeading;
	
	double previousHeading;
	double previousVelocity;
	
	double averageDuration;
	double durationIndex;

	public void onScannedRobot(ScannedRobotEvent e) {

		
		/*
		 * Shoot angle 
		 */
		
		double firePower = 1.5;

		double actualBearing = Utils.normalAbsoluteAngleDegrees(e.getBearing() + getHeading());
		System.out.println("actual bearing: " + actualBearing);

		double L = e.getDistance();
		double v = e.getVelocity();
		double s = 20 - (3 * firePower);
		double degreesO = findO(actualBearing, e.getHeading(), v);
		double possibleAs[] = findPossibleA(convertToRadians(degreesO), L, v, s);
		double realA = possibleAs[findA(degreesO)];
		double shootAngle;
		if (v == 0) {
			shootAngle = 0;
		} else {
			shootAngle = convertToDegrees(findShootAngle(realA, L, convertToRadians(degreesO)));
		}
		double absoluteShootAngle = shootAngle + actualBearing;

		
		/*
		 * Radar tracking
		 */
		
		double radarInitialTurn = Utils.normalRelativeAngleDegrees(actualBearing - getRadarHeading());
		double extraTurn = convertToDegrees(Math.min((Math.atan(5 / e.getDistance())), Rules.RADAR_TURN_RATE_RADIANS));
		double radarTurn = findRadarTurn(radarInitialTurn, extraTurn);

		System.out.println("ExtraTurn: " + extraTurn);
		System.out.println("Initial" + radarInitialTurn);
		System.out.println("radarTurn: " + radarTurn);

		setTurnRadarRight(radarTurn);
		
		/*
		 * Gun aim 
		 */
		
		
		double angleNeeded = Utils.normalRelativeAngleDegrees(absoluteShootAngle - getGunHeading());
		if(angleNeeded > 0) {
			setTurnGunRight(Math.min(angleNeeded, Rules.GUN_TURN_RATE * 1));
		} else if (angleNeeded < 0) {
			setTurnGunRight(Math.max(angleNeeded, Rules.GUN_TURN_RATE * -1));
		}
		
		/*
		 * Gun shoot 
		 */
		
		
		if((previousHeading == e.getHeading()) && (findSign(previousVelocity) == findSign(e.getVelocity()))) {
			currentStraightHeading++;
		} else {
			if (currentStraightHeading > 2) {
				averageDuration = ((averageDuration*durationIndex) + currentStraightHeading) / (durationIndex + 1);
				durationIndex++;
			}
			currentStraightHeading = 0;
		}
		previousHeading = e.getHeading();
		previousVelocity = e.getVelocity();
		boolean isAim = ((absoluteShootAngle + 2) >= getGunHeading()) && (getGunHeading() >= (absoluteShootAngle - 2));
		
		if (getGunHeat() == 0) {
			if(Math.round(shootAngle/3) == 0 || e.getDistance() < 50) {
					setFire(firePower);			
			} else if (currentStraightHeading > 5) {
				
				if (currentStraightHeading > 15) {
					setFire(firePower);
				} else {
					setFire(firePower);
				}
			}
		}
		
		/*
		 * Print 
		 */
		if (isAim) {
			System.out.println("Aim");
		} else {
			System.out.println();
		}
		System.out.println();
//		System.out.println("absolute shoot angle: " + absoluteShootAngle);
//		System.out.println("shoot angle: " + shootAngle + "  actual Bearing: " + actualBearing);
////		System.out.println("gun heading" + getGunHeading());
//		System.out.println("Angle needed" +  angleNeeded );
//		System.out.println();
//		System.out.println("distance: " + e.getDistance());
		System.out.println("streak:" + currentStraightHeading);
		System.out.println("average duration: " + averageDuration);
		
		System.out.println();
		System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=");
		
		/*
		 * Execute
		 */
		
		execute();
	}
	
	public int findSign(double number) {
		if(number > 0) {
			return 1;
		} else if (number < 0) {
			return -1;
		} else {
			return 0;
		}
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

	private double findShootAngle(double a, double L, double O) {
		return -1*Math.atan((a * Math.tan(O)) / (L + a));
	}

	private double findO(double Bearing, double Heading, double Velocity) {
		if (Velocity >= 0) {
			return Utils.normalAbsoluteAngleDegrees(Bearing - Heading);
		}	else {
			return Utils.normalAbsoluteAngleDegrees((Bearing - Heading) + 180);
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