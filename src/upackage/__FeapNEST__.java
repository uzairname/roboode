package upackage;
import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.lang.reflect.Array;

public class __FeapNEST__ extends AdvancedRobot {
//FeapNEST
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
	

	double currentStraightHeading;
	double currentCurvedHeading;
	
	double previousHeading;
	double previousVelocity;
	
	double averageDuration;
	double durationIndex;
	double firePower = 0.1;
	
	double turnDirection = 1;

	public void onScannedRobot(ScannedRobotEvent e) {

		/*
		 * Color flash
		 */
		
		if(getTime() % 6 < 2) {
			setBodyColor(new Color(255, 200, 100));
			setGunColor(new Color(0, 0, 255));
			setRadarColor(new Color(255, 255, 0));
			setScanColor(new Color(255, 255, 255));
			setBulletColor(new Color(0, 0, 0));
		} else if (getTime() % 6 < 4) {
			setAllColors(java.awt.Color.BLACK);
		} else if (getTime() % 6 < 6) {
			setBodyColor(new Color(150, 20, 0));
			setGunColor(new Color(255, 200, 0));
			setRadarColor(new Color(255, 255, 0));
			setScanColor(new Color(255, 50, 0));
			setBulletColor(new Color(0, 0, 0));
		}
		
		
		/*
		 * Shoot angle 
		 */

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

		if((previousHeading == e.getHeading()) && (findSign(previousVelocity) == findSign(e.getVelocity())) && previousVelocity == e.getVelocity()) {
			currentCurvedHeading = 0;
			currentStraightHeading += Math.abs(e.getVelocity());
			//make this based on time rather than distance, to get accurate straightHeading at beginning of match
		} else {
			if (currentStraightHeading > 8) {
				averageDuration = ((averageDuration*durationIndex) + currentStraightHeading) / (durationIndex + 1);
				durationIndex++;
			}
			currentCurvedHeading += Math.abs(e.getVelocity());
			currentStraightHeading = 0;
		}
		
		boolean isDisabled = e.getEnergy() <= 0;
		boolean isLongHeadingDistance = (currentStraightHeading >= 80) || (currentStraightHeading == 0);
		boolean isProjectionLessThanAverage = currentStraightHeading + Math.abs(realA*Math.tan(convertToRadians(degreesO))) < averageDuration;
		boolean isCloseDistance = e.getDistance() < 250;
		boolean isVeryCloseDistance = e.getDistance() < 80;
		boolean isLongCurvedPath = currentCurvedHeading >= 120;
		boolean isStopped = e.getVelocity() == 0;
		boolean isFacingMe = Math.round(shootAngle/3) == 0;
		boolean remainingEnergy = getEnergy() >= 6;
		
		if (remainingEnergy) {
			if (isDisabled) {
				setFire(firePower);
				firePower = 3;
			} else if ( (isStopped || (isFacingMe && isCloseDistance)) && isLongHeadingDistance) {
						setFire(firePower);
						firePower = 3;
			} else if (isProjectionLessThanAverage) {
					if (e.getDistance() < 70) {
						setFire(firePower);
						firePower = 3;
					} else {
						setFire(firePower);
						firePower = 2;
					}
			} else if (isVeryCloseDistance){
					setFire (firePower);
					firePower = 3;
			} else if (isLongCurvedPath) {
				setFire (firePower);
				firePower = 0.5;
			}
		}
	
		previousHeading = e.getHeading();
		previousVelocity = e.getVelocity();
		
		/*
		 * Move
		 */
		if(Math.abs(getX()) == 1) {
			
		}
		double angleNeededBody = Utils.normalRelativeAngleDegrees(((actualBearing + 90) - getHeading()) - 25);
		double turnRight;
		if(angleNeededBody > 0) {
			turnRight = (Math.min(angleNeededBody, Rules.MAX_TURN_RATE * 1));
		} else if (angleNeededBody < 0) {
			turnRight = (Math.max(angleNeededBody, Rules.MAX_TURN_RATE * -1));
		} else {
			turnRight = 0;
		}
		setTurnRight(turnRight);
		setAhead(8);
		
		/*
		 * Print 
		 */
		boolean isAim = ((absoluteShootAngle + 2) >= getGunHeading()) && (getGunHeading() >= (absoluteShootAngle - 2));
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
//		System.out.println("energy:" + e.getEnergy());
		System.out.println("is thing true? " + (currentStraightHeading + Math.abs(realA*Math.tan(convertToRadians(degreesO))) < averageDuration));
		System.out.println("firepower: " + firePower);
		System.out.println("distance: " + e.getDistance());
		System.out.println("straight distance:" + currentStraightHeading);
		System.out.println("curved distance: " + currentCurvedHeading);
//		System.out.println("projected: " + Math.abs(realA*Math.tan(convertToRadians(degreesO))) + " RealA: " + realA + "radians O: " + convertToRadians(degreesO));
		System.out.println("average duration: " + averageDuration);
		System.out.println("index: " + durationIndex);
		
		System.out.println();
		System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=");
		
		/*
		 * Execute
		 */
		
		execute();
	}
	
	public double findSign(double number) {
		if(number > 0) {
			return 1;
		} else if (number < 0) {
			return -1;
		} else {
			return 0;
		}
	}

	public double findRadarTurn(double radarInitialTurn, double extraTurn) {
		return radarInitialTurn + (extraTurn * findSign(radarInitialTurn));
	}

	private double findShootAngle(double a, double L, double O) {
		return -1*Math.atan((a * Math.tan(O)) / (L + a));
	}

	private double findO(double Bearing, double Heading, double Velocity) {
		if (Velocity >= 0) {
			return Utils.normalAbsoluteAngleDegrees(Bearing - Heading);
		} else if (Velocity <= 0) {
			return Utils.normalAbsoluteAngleDegrees((Bearing - Heading) + 180);
		} else {
			return 0;
		}
	}

	private double[] findPossibleA(double O, double L, double v, double s) {
		if (v == 0) {
			double zero [] = {0, 0};
			return zero;
		} else {
			double quadratica = (((s * s) / (v * v)) - 1) / (Math.cos(O) * Math.cos(O));
			double quadraticb = 2 * L * -1;
			double quadraticc = L * L * -1;
			return solve(quadratica, quadraticb, quadraticc);
		}
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