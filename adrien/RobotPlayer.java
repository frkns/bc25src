package adrien;

import battlecode.common.*;
import adrien.units.Mopper;
import adrien.units.Soldier;
import adrien.units.Splasher;
import adrien.units.Tower;

import java.util.Random;


public class RobotPlayer {
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int turnCount = 0;
    static final Random rng = new Random(6147);
    static RobotController rc;
    static Robot robot;

    static void resetRobot(){
        switch (rc.getType()){
            case SOLDIER:
                robot = new Soldier(rc);
                break;
            case MOPPER:
                robot = new Mopper(rc);
                break;
            case SPLASHER:
                robot = new Splasher(rc);
                break;
            default:
                robot = new Tower(rc);
                break;
        }

        turnCount = 0;
        robot.init();
    }

    public static void run(RobotController robotController) throws GameActionException {
        // System.out.println("I'm alive");
        rc = robotController;
        resetRobot();

        while (true) {
            try {
                if(rc.getRoundNum() > 200 && false){
                    rc.resign();
                }

                robot.play();
            }
            catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();
                resetRobot();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
                resetRobot();

            } finally {
                Clock.yield();
            }
        }
    }
}
