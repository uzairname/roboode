package upackage;

import java.util.function.Function;

import robocode.AdvancedRobot;
import robocode.Robot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class DirectionCalculator {
	

	private static final int BOUNDARY_BUFFER = 20;


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
		return (robot.getX() > robot.getWidth() + BOUNDARY_BUFFER)
			&& (robot.getBattleFieldWidth() - robot.getX() > robot.getWidth() + BOUNDARY_BUFFER)
			&& (robot.getY() > robot.getHeight() + BOUNDARY_BUFFER)
			&& (robot.getBattleFieldHeight() - robot.getHeight() > robot.getHeight() + BOUNDARY_BUFFER);
	}
	

	private static double moveInBounds (double currentDirection, Robot robot) {
		double heading = robot.getHeading();
		double x = robot.getX();
		double y = robot.getY();
		double fieldWidth = robot.getBattleFieldWidth();
		double fieldHeight = robot.getBattleFieldHeight();
		double robotWidth = robot.getWidth() + BOUNDARY_BUFFER;
		double robotHeight = robot.getHeight() + BOUNDARY_BUFFER;
		boolean boundsRight = !(fieldWidth - x > robotWidth);
		boolean boundsUp = !(fieldHeight - y > robotHeight);
		boolean boundsLeft = !(x > robotWidth);
		boolean boundsDown = !(y > robotHeight);
		
		Function<Integer, Double> degrees = degree -> {
			if (Utils.normalAbsoluteAngleDegrees(heading + degree) >= 180) {
				return -1.0;
			} else {
				return 1.0;
			}
		};
		
		if (boundsLeft) {
			return degrees.apply(0);
		} else if (boundsDown) {
			return degrees.apply(90);
		} else if (boundsRight) {
			return degrees.apply(180);
		} else if (boundsUp) {
			return degrees.apply(270);
		} else {
			return currentDirection;
		}
	}
}