package upackage;
//import javafx.scene.paint.Color;
import robocode.*;
import robocode.util.Utils;

public class OrangeRobot extends AdvancedRobot {
	
	public void run() {
			
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		
		setBodyColor(java.awt.Color.orange);
		setGunColor(java.awt.Color.red);
		setRadarColor(java.awt.Color.orange);
		setBulletColor(java.awt.Color.lightGray);
		setScanColor(java.awt.Color.black);
		
		while(true) {
						turnRadarRight(45);
		}
	}

	
	public void onScannedRobot(ScannedRobotEvent e) {
	
		double firePower= 1;
		
		double actualBearing = mod(e.getBearing() + getHeading(), 360);
		System.out.println("my mod="+ actualBearing);
				
		double degreesO = findO(actualBearing, e.getHeading());
		double L = e.getDistance();
		double v = e.getVelocity();
		double s = 20-(3*firePower);
		double possibleAs [] = findPossibleA(convertToRadians(degreesO), L, v, s);
		double realA = possibleAs[ findA(degreesO) ];
		double shootAngle = convertToDegrees(findShootAngle(realA, L, convertToRadians(degreesO)));
		
		double radarInitialTurn = Utils.normalRelativeAngleDegrees(actualBearing - getRadarHeading());
		double extraTurn = Math.min( Math.atan( 500 / e.getDistance() ), Rules.GUN_TURN_RATE_RADIANS );
		double radarTurn;
		
		if  (radarInitialTurn < 0) {
			radarTurn = radarInitialTurn - extraTurn;
		} else if (radarInitialTurn > 0) {
			radarTurn = radarInitialTurn + extraTurn;
		} else {
			radarTurn = extraTurn;
		}
		
		turnGunRight(radarTurn);
		
//		System.out.println("shooting angle: " + convertToDegrees(findShootAngle(realA, L, convertToRadians(degreesO))));
//		System.out.println("O radians " + convertToRadians(degreesO));
//		System.out.println("actual a" + realA);
//		System.out.println("s: "+s + " v: "+v + " L: "+L + " O deg: "+degreesO);
		System.out.println();
	}
	
	private double mod(double x, double n) {
		double r = x % n;
		if (r < 0) {
		    r += n;
		}
		return r;
	}


	private double findShootAngle(double a, double L, double O) {
		return Math.atan( (a*Math.tan(O)) / (L+a) );
	}

	private double findO (double Bearing, double Heading) {
		
		if (((Bearing - Heading) % 360) >= 0) {
			return (Bearing - Heading) % 360;
		} else {
			return ((Bearing - Heading) % 360) + 360;
		}
	}
	private double [] findPossibleA (double O, double L, double v, double s) {
		double quadratica = (((s*s)/(v*v))-1)/(Math.cos(O)*Math.cos(O));
		double quadraticb = 2*L*-1;
		double quadraticc = L*L*-1;
		return solve(quadratica, quadraticb, quadraticc);
	}
		
	private int findA (double degO) {
		if (90 <= degO && degO <= 270) {
			if (degO == 90 || degO == 270) {
				return 0; // 0 or 1
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}
	
	private double convertToRadians (double degrees) {
		return (Math.PI*degrees)/180;
	}
	
	private double convertToDegrees (double radians) {
		return (radians*180)/Math.PI;
	}

	private double [] solve (double a, double b, double c) {
		
		double [] roots = new double [2];
		double disc = Math.sqrt((b*b)-(4*a*c));
		roots[0] = ((-1*b)-disc)/(2*a);
		roots[1] = ((-1*b)+disc)/(2*a);
		return roots;
	}	
}