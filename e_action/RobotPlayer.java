package e_action;

import e_action.units.Mopper;
import e_action.units.Soldier;
import e_action.units.Splasher;
import e_action.units.Tower;
import e_action.knowledge._Info;

import battlecode.common.*;

public class RobotPlayer {
    static RobotController rc;
    static Robot robot;

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
        Robot.initRobot();
    }

    public static void run(RobotController r) throws GameActionException {
        rc = r;  // Assign r to the public static field rc
        setRobotType();
        while (true) {
            try {
                if (_Info.round > 100) {
                    rc.resign();
                }
                robot.initTurn();
                robot.playTurn();
                robot.endTurn();
            }
            catch (GameActionException e) {
                System.out.println("GameActionException");
                 e.printStackTrace();
                 rc.resign();
            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
                rc.resign();
            } finally {
                if (rc.getRoundNum() != _Info.round) {
                    System.out.println("Went over bytecode limit");
                }
                Clock.yield();
            }
        }
    }
}
