package gavin531_PM_MST;

import battlecode.common.*;

import java.util.HashSet;
import java.util.Set;

public class Sprint1 extends RobotPlayer {
    static MapLocation target;

    static void runSprint1(RobotController rc) throws GameActionException {
        System.out.println("running Sprint1");

        // find location of defense/normal enemy towers

        Set<MapLocation> defenseTowerLocs = new HashSet<>(); // range of 4 = sqrt(16)
        Set<MapLocation> normalTowerLocs = new HashSet<>(); // range of 3 = sqrt(9)

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam().opponent() && robot.getType().isTowerType()) {
                if (robot.getType().getBaseType() == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
                    defenseTowerLocs.add(robot.getLocation());
                } else {
                    normalTowerLocs.add(robot.getLocation());
                }
            }
        }

        if (target == null || rc.getLocation() == target) {
            // random MapLocation in width, height
            target = new MapLocation(rng.nextInt(rc.getMapWidth()-1), rng.nextInt(rc.getMapHeight()-1));
        }
        Direction dir;
        MapLocation newLoc;

        int tries = 8;
        while (tries-- > 0) {
            dir = rc.getLocation().directionTo(target);
            newLoc = rc.getLocation().add(dir);
            boolean safe = true;
            for (MapLocation loc : normalTowerLocs) {
                if (newLoc.distanceSquaredTo(loc) <= 9) {
                    safe = false;
                    break;
                }
            }
            if (!safe || !rc.senseMapInfo(newLoc).getPaint().isAlly()) {
                target = new MapLocation(rng.nextInt(rc.getMapWidth()-1), rng.nextInt(rc.getMapHeight()-1));
            } else {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    break;
                } else {
                    target = new MapLocation(rng.nextInt(rc.getMapWidth()-1), rng.nextInt(rc.getMapHeight()-1));
                }
            }
            dir = dir.rotateRight();
        }


        MapLocation loc = rc.getLocation();
        MapInfo locInfo = rc.senseMapInfo(loc);
        if (locInfo.getPaint() == PaintType.EMPTY && rc.canPaint(loc) && rc.getPaint() > 5) {  // need at least 1 paint to survive
            System.out.println("painting " + loc);
            rc.attack(loc);
        }

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos(9);
        for (MapInfo tile : nearbyTiles) {
            loc = tile.getMapLocation();
            locInfo = rc.senseMapInfo(loc);
            if (locInfo.getPaint() == PaintType.EMPTY && rc.canPaint(loc) && rc.getPaint() > 5) {  // need at least 1 paint to survive
                System.out.println("painting " + loc);
                rc.attack(loc);
            }
        }

    }
}
