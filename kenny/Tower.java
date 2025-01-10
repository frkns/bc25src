package kenny;

import battlecode.common.*;

import java.util.Random;

public class Tower extends RobotPlayer {
    static RobotController rc;
    // static Random rng;
    static MapLocation myLoc;
    static int numSpawned = 0;

    static int[] dx8 = {-2, -1, 0, 1, 0, -1, 0, -1};
    static int[] dy8 = {0, 1, 2, 1, 2, -1, -2 , -1};

    static void init(RobotController r) throws GameActionException {
        rc = r;
        myLoc = rc.getLocation();
    }

    static void runTower() throws GameActionException {
        MapLocation nextLoc = new MapLocation(myLoc.x + dx8[numSpawned % 8], myLoc.y + dy8[numSpawned % 8]);
        if (phase <= 2) {
            if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
                numSpawned++;
            }
        }
        if (phase == 3) {
            if (false) {
                if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                    rc.buildRobot(UnitType.SOLDIER, nextLoc);
                    numSpawned++;
                }
            } else {
                if (rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
                    rc.buildRobot(UnitType.SPLASHER, nextLoc);
                    numSpawned++;
                }
            }
        }
    }
}
