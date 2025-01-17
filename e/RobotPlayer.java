package e;

import battlecode.common.*;
import e.knowledge._Info;
import e.units.Mopper;
import e.units.Splasher;
import e.units.Tower;
import e.units.Soldier;

import java.util.ArrayDeque;

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
    }
    
    public static void run(RobotController r) throws GameActionException {
        rc = r;  // Assign r to the public static field rc
        setRobotType();
        while (true) {
            try {
                if(rc.getRoundNum() > 1000){
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
