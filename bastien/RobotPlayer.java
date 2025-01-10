package bastien;

import battlecode.common.*;
import bastien.units.Mopper;
import bastien.units.Soldier;
import bastien.units.Splasher;
import bastien.units.Tower;

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
    }

    public static void run(RobotController r) throws GameActionException {
        rc = r;  // Assign r to the public static field rc
        resetRobot(); // Also used to init robot

        while (true) {
            try {
                robot.initTurn();
                robot.playTurn();
                robot.endTurn();
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
