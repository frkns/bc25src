package e_action;

import e_action.units.Mopper;
import e_action.units.Soldier;
import e_action.units.Splasher;
import e_action.units.Tower;

import battlecode.common.*;

public class RobotPlayer {
    static RobotController rc;
    static Robot robot;
    static int _round_num;

    // Can also be used to reset robot actions
    static void setRobotType() throws GameActionException {
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
    }

    public static void run(RobotController r) throws GameActionException {
        rc = r;  // Assign r to the public static field rc

        setRobotType();
        while (true) {
            _round_num = rc.getRoundNum();
            try {
                robot.initTurn();
                robot.playTurn();
                robot.endTurn();
            }
            catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
            } finally {
                if (rc.getRoundNum() != _round_num) {
                    System.out.println("Went over bytecode limit");
                }
                Clock.yield();
            }
        }
    }
}
