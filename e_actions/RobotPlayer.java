package e_actions;

import battlecode.common.*;
import e_actions.units.Mopper;
import e_actions.units.Soldier;
import e_actions.units.Splasher;
import e_actions.units.Tower;


public class RobotPlayer {
    static RobotController rc;
    static Robot robot;

    static void setRobotType(){
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
                Clock.yield();
            }
        }
    }
}
