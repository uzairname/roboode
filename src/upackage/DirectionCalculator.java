package upackage;

import robocode.AdvancedRobot;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class DirectionCalculator {
	

	public static double calcDirection(ScannedRobotEvent e, double currentMovementDirection, AdvancedRobot robot) {
		double movementDirection = moveInBounds(currentMovementDirection, robot);
		double actualBearing = Utils.normalAbsoluteAngleDegrees(e.getBearing() + robot.getHeading());
		if (isInBounds(robot)) {
			double angleNeededBody = Utils.normalRelativeAngleDegrees(((actualBearing + 90) - robot.getHeading()) - (findIncline(e.getDistance())*movementDirection));
			double turnRight;
			if(angleNeededBody > 0) {
				turnRight = (Math.min(angleNeededBody, Rules.MAX_TURN_RATE * 1));
			} else if (angleNeededBody < 0) {
				turnRight = (Math.max(angleNeededBody, Rules.MAX_TURN_RATE * -1));
			} else {
				turnRight = 0;
			}
			robot.setTurnRight(turnRight);
			if (robot.getTime() % e.getDistance() == 1) {
				movementDirection *= -1;
			}
		}
		return movementDirection;
	}
	
	private static double findIncline (double distance) {
		if (distance >= 170) {
			return (-165 * Math.pow(1.003, -1*distance)) + 90;
		} else {
			return (0.3*distance) - 62;
		}
	}
	
	private static boolean isInBounds (Robot robot) {
		return (robot.getX() > robot.getWidth() + 20)
			&& (robot.getBattleFieldWidth() - robot.getX() > robot.getWidth() + 20)
			&& (robot.getY() > robot.getHeight() + 20)
			&& (robot.getBattleFieldHeight() - robot.getHeight() > robot.getHeight() + 20);
	}
	

	private static double moveInBounds (double movementDir, Robot robot) {
		double heading = robot.getHeading();
		double x = robot.getX();
		double y = robot.getY();
		double fieldWidth = robot.getBattleFieldWidth();
		double fieldHeight = robot.getBattleFieldHeight();
		double robotWidth = robot.getWidth() + 20;
		double robotHeight = robot.getHeight() + 20;
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