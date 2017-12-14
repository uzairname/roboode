package githubsamples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import robocode.*;
import robocode.util.Utils;


public class SampleWallAvoidance extends AdvancedRobot {
    static int currentEnemyVelocity;
    static int aimingEnemyVelocity;
    double velocityToAimAt;
    boolean fired;
    Rectangle grid = new Rectangle(0, 0, 800, 600);
    double oldTime;
    int count;
    int averageCount;
    static double enemyVelocities[][] = new double[400][4];
    static double turn = 2;
    int turnDir = 1;
    int moveDir = 1;
    double oldEnemyHeading;
    double oldEnergy = 100;


    @Override
    public void run() {
        setBodyColor(Color.blue);
        setGunColor(Color.blue);
        setRadarColor(Color.black);
        setScanColor(Color.yellow);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }


    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        Graphics2D g = getGraphics();
        turn += 0.2 * Math.random();
        if (turn > 8) {
            turn = 2;
        }

        if(oldEnergy - e.getEnergy() <= 3 && oldEnergy - e.getEnergy() >= 0.1) {
            if (Math.random() > .5) {
                turnDir *= -1;
            }
            if(Math.random() > .8) {
                moveDir *= -1;
            }
        }

        setMaxTurnRate(turn);
        setMaxVelocity(12 - turn);
        setAhead(90 * moveDir);
        setTurnLeft(90 * turnDir);
        oldEnergy = e.getEnergy();

        if (e.getVelocity() < -2) {
            currentEnemyVelocity = 0;
        }
        else if (e.getVelocity() > 2) {
            currentEnemyVelocity = 1;
        }
        else if (e.getVelocity() <= 2 && e.getVelocity() >= -2) {
            if (currentEnemyVelocity == 0) {
                currentEnemyVelocity = 2;
            }
            else if (currentEnemyVelocity == 1) {
                    currentEnemyVelocity = 3;
            }
        }
        if (getTime() - oldTime > e.getDistance() / 12.8 && fired == true) {
            aimingEnemyVelocity=currentEnemyVelocity;
        }
        else {
            fired = false;
        }

        enemyVelocities[count][aimingEnemyVelocity] = e.getVelocity();
        count++;

        if(count==400) {
            count=0;
        }

        averageCount = 0;
        velocityToAimAt = 0;

        while(averageCount < 400) {
            velocityToAimAt += enemyVelocities[averageCount][currentEnemyVelocity];
            averageCount++;
        }

        velocityToAimAt /= 400;

        double bulletPower = Math.min(2.4, Math.min(e.getEnergy()/3.5, getEnergy()/9));
        double myX = getX();
        double myY = getY();
        double enemyX = getX() + e.getDistance() * Math.sin(absBearing);
        double enemyY = getY() + e.getDistance() * Math.cos(absBearing);
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - oldEnemyHeading;
        oldEnemyHeading = enemyHeading;
        double deltaTime = 0;
        double battleFieldHeight = getBattleFieldHeight();
        double battleFieldWidth = getBattleFieldWidth();
        double predictedX = enemyX, predictedY = enemyY;

        while ((++deltaTime) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance( myX, myY, predictedX, predictedY)) {      
            predictedX += Math.sin(enemyHeading) * velocityToAimAt;
            predictedY += Math.cos(enemyHeading) * velocityToAimAt;
            enemyHeading += enemyHeadingChange;
            g.setColor(Color.red);
            g.fillOval((int)predictedX - 2,(int)predictedY - 2, 4, 4);

            if (predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0 || predictedY > battleFieldHeight - 18.0) {   
                predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0); 
                predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
                break;
            }
        }

        double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
        setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));

        if(getGunHeat() == 0) {
            fire(bulletPower);
            fired = true;
        }         
    }
}
