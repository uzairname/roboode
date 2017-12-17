package upackage;
import robocode.*;
import robocode.util.Utils;
import java.awt.*;

public class __FeapNEST__ extends AdvancedRobot {

	double currentStraightHeading;
	double currentStraightHeadingTime;
	double currentCurvedHeading;
	double currentCurvedHeadingTime;
	
	double previousHeading;
	double previousVelocity;
	
	double averageDuration;
	double durationIndex;
	double firePower = 0.1;
	
	double movementDirection = -1;
	boolean isMovingFromBorder;

	public void run() {

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		while (true) {
			setTurnRadarRight(Rules.RADAR_TURN_RATE); 			
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {

		colorFlash();
	
		double actualBearing = Utils.normalAbsoluteAngleDegrees(e.getBearing() + getHeading());
		double L = e.getDistance();
		double v = e.getVelocity();
		double s = 20 - (3 * firePower);
		double degreesO = findO(actualBearing, e.getHeading(), v);
		double possibleAs[] = findPossibleA(convertToRadians(degreesO), L, v, s);
		double realA = possibleAs[findA(degreesO)];
		double absoluteShootAngle = findAbsShootingAngle(actualBearing, L, v, degreesO, realA);

		moveRadar(e, actualBearing);		
		aimGun(absoluteShootAngle);
		calculateAverageDistances(e);
		setFirePower(e, degreesO, realA);
		setPrevious(e);
		move(e, actualBearing);
		
		print(e);
		
		execute();
	}

	private void print(ScannedRobotEvent e) {
		System.out.println();
		System.out.println("firepower: " + firePower);
		System.out.println("distance: " + e.getDistance());
		System.out.println("straight distance:" + currentStraightHeading);
		System.out.println("curved distance: " + currentCurvedHeading);
		System.out.println("average duration: " + averageDuration);
		System.out.println("direction: " + movementDirection);
		System.out.println("heading: " + getHeading());
		System.out.println("incline: " + Calc.findIncline(e.getDistance()));
		System.out.println("in bounds? " + Calc.isInBounds(getX(), getY(), getBattleFieldWidth(), getBattleFieldHeight(), getWidth() + 20, getHeight() + 20));		
		System.out.println();
		System.out.println("=~=~=~=~=~=~=~=~=~=~=~=~=~=");
	}

	private void setPrevious(ScannedRobotEvent e) {
		previousHeading = e.getHeading();
		previousVelocity = e.getVelocity();
	}

	private void move(ScannedRobotEvent e, double actualBearing) {
		movementDirection = Calc.moveInBounds(movementDirection, getHeading(), getX(), getY(), getBattleFieldWidth(), getBattleFieldHeight(), getWidth() + 20, getHeight() + 20);
		
		if (Calc.isInBounds(getX(), getY(), getBattleFieldWidth(), getBattleFieldHeight(), getWidth() + 29, getHeight() + 29)) {
			double angleNeededBody = Utils.normalRelativeAngleDegrees(((actualBearing + 90) - getHeading()) - (Calc.findIncline(e.getDistance())*movementDirection));
			double turnRight;
			if(angleNeededBody > 0) {
				turnRight = (Math.min(angleNeededBody, Rules.MAX_TURN_RATE * 1));
			} else if (angleNeededBody < 0) {
				turnRight = (Math.max(angleNeededBody, Rules.MAX_TURN_RATE * -1));
			} else {
				turnRight = 0;
			}
			setTurnRight(turnRight);
			if (getTime() % e.getDistance() == 1) {
				movementDirection = -1*movementDirection;
			}
		}
		setAhead(Rules.MAX_VELOCITY * movementDirection);
	}

	private void setFirePower(ScannedRobotEvent e, double degreesO, double realA) {
		boolean isDisabled = e.getEnergy() <= 0;
		boolean isProjectionLessThanAverage = (currentStraightHeadingTime >= 4) && (currentStraightHeading + Math.abs(realA*Math.tan(convertToRadians(degreesO))) < averageDuration);
		boolean isClose = e.getDistance() < 350;
		boolean isVeryClose = e.getDistance() < 200;
		boolean isLongCurvedPath = currentCurvedHeading >= 220;
		boolean isLongStraightTime = currentStraightHeadingTime >= 7;		
		boolean isStopped = e.getVelocity() == 0;
		boolean remainingEnergy = getEnergy() >= 6;
		
		
		if (isDisabled) {
			setFire(Math.min(getEnergy()/1.5, 3));
		} if (remainingEnergy) {
			if (isVeryClose) {
				setFire (firePower);
				firePower = 3; 
			} else if (isStopped || isLongStraightTime) {
					setFire(firePower);
					firePower = 3;
			} else if (isProjectionLessThanAverage) {
					if (isClose) {
						setFire(firePower);
						firePower = 3;
					} else {
						setFire(firePower);
						firePower = 1.5;
					}
			} else if (isLongCurvedPath) {
				if (isClose) {
					setFire (firePower);
					firePower = 1.5;
				} else {
					setFire (firePower);
					firePower = 0.5;
				}
			}
		}
	}

	private void calculateAverageDistances(ScannedRobotEvent e) {
		if((previousHeading == e.getHeading()) && (findSign(previousVelocity) == findSign(e.getVelocity()))) {
			currentCurvedHeading = 0;
			currentCurvedHeadingTime = 0;
			
			currentStraightHeading += Math.abs(e.getVelocity());
			currentStraightHeadingTime ++;
			//make this based on time rather than distance, to get accurate straightHeading at beginning of match
		} else {
			if (currentStraightHeading > 8) {
				averageDuration = ((averageDuration*durationIndex) + currentStraightHeading) / (durationIndex + 1);
				durationIndex++;
			}
			currentCurvedHeading += Math.abs(e.getVelocity());
			currentCurvedHeadingTime ++;
			
			currentStraightHeading = 0;
			currentStraightHeadingTime = 0;
		}
	}

	private void aimGun(double absoluteShootAngle) {
		double angleNeeded = Utils.normalRelativeAngleDegrees(absoluteShootAngle - getGunHeading());
		if(angleNeeded > 0) {
			setTurnGunRight(Math.min(angleNeeded, Rules.GUN_TURN_RATE * 1));
		} else if (angleNeeded < 0) {
			setTurnGunRight(Math.max(angleNeeded, Rules.GUN_TURN_RATE * -1));
		}
	}

	private void moveRadar(ScannedRobotEvent e, double actualBearing) {
		double radarInitialTurn = Utils.normalRelativeAngleDegrees(actualBearing - getRadarHeading());
		double extraTurn = convertToDegrees(Math.min((Math.atan(5 / e.getDistance())), Rules.RADAR_TURN_RATE_RADIANS));
		double radarTurn = findRadarTurn(radarInitialTurn, extraTurn);

		System.out.println("ExtraTurn: " + extraTurn);
		System.out.println("Initial" + radarInitialTurn);
		System.out.println("radarTurn: " + radarTurn);
		
		setTurnRadarRight(radarTurn);
	}
		
	private double findAbsShootingAngle(double actualBearing, double L, double v, double degreesO, double realA) {
		double shootAngle;
		if (v == 0) {
			shootAngle = 0;
		} else {
			shootAngle = convertToDegrees(findShootAngle(realA, L, convertToRadians(degreesO)));
		}
		return shootAngle + actualBearing;
	}

	private void colorFlash() {

		if(getTime() % 6 < 2) {
			setBodyColor  (new Color(255, 200, 100));
			setGunColor   (new Color(0, 0, 255));
			setRadarColor (new Color(0, 0, 0));
			setScanColor  (new Color(255, 255, 255));
			setBulletColor(new Color(0, 0, 0));
		} else if (getTime() % 6 < 4) {
			setBodyColor  (new Color(255, 200, 100));
			setGunColor   (new Color(0, 0, 255));
			setRadarColor (new Color(0, 0, 0));
			setScanColor  (new Color(255, 255, 255));
			setBulletColor(new Color(0, 0, 0));
		} else if (getTime() % 6 < 6) {
			setBodyColor  (new Color(150, 20, 0));
			setGunColor   (new Color(255, 200, 0));
			setRadarColor (new Color(200, 130, 0));
			setScanColor  (new Color(255, 255, 255));
			setBulletColor(new Color(0, 0, 0));
		}
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