package temp_test;

import battlecode.common.*;
import java.util.Arrays;
import java.util.Comparator;

public class AttackBase extends RobotPlayer {
    static RobotController rc;
    static MapLocation spawnLocation;  // actually the spawn tower location
    static UnitType spawnTowerType;

    // create an array of the 3 possible symetries for enemy spawn location
    static MapLocation[] potentialEnemySpawnLocations = new MapLocation[3];

    static MapLocation target;
    static MapLocation prevLoc = null;

    static void init(RobotController r) throws GameActionException {
        rc = r;
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                if (robot.getType().isTowerType()) {
                    if (spawnLocation == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc.getLocation().distanceSquaredTo(spawnLocation)) {
                        spawnLocation = robot.getLocation();
                        spawnTowerType = robot.getType().getBaseType();
                        // add these to potentialEnemySpawnLocations
                        potentialEnemySpawnLocations[0] = Utils.mirror(spawnLocation);
                        potentialEnemySpawnLocations[1] = Utils.verticalMirror(spawnLocation);
                        potentialEnemySpawnLocations[2] = Utils.horizontalMirror(spawnLocation);
                    }
                }
            }
        }

        if (spawnLocation == null)
            return;

        prevLoc = rc.getLocation();
        // Sort potentialEnemySpawnLocations based on distance to current location
        Arrays.sort(potentialEnemySpawnLocations, Comparator.comparingInt(loc -> rc.getLocation().distanceSquaredTo(loc)));
    }

    static boolean[] visited = new boolean[3];
    static MapLocation foundLoc = null;

    static void run() throws GameActionException {

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

        if (foundLoc != null) {
            boolean attacked = false;
            if (rc.canAttack(foundLoc)) {
                attacked = true;
                rc.attack(foundLoc);
                // Direction dir = rc.getLocation().directionTo(foundLoc).opposite();
                // if (rc.canMove(dir)) {
                //     rc.move(dir);
                // }
            } else {
                if (rc.canMove(rc.getLocation().directionTo(foundLoc))) {
                    rc.move(rc.getLocation().directionTo(foundLoc));
                } else
                Pathfinder.move(foundLoc);
            }
            if (rc.canAttack(foundLoc)) {
                attacked = true;
                rc.attack(foundLoc);
            }

            if (!attacked) {
                System.out.println("!!! I *should've* attacked the enemy tower but i can't !!!");
            }
        }

        if (foundLoc == null)
        for (MapInfo tile : nearbyTiles) {
            for (int i = 0; i < 3; i++) {
                if (tile.getMapLocation().equals(potentialEnemySpawnLocations[i])) {
                    // System.out.println("Found **potential** enemy spawn location");
                    visited[i] = true;
                    RobotInfo robot = null;
                    // if (rc.canSenseRobotAtLocation(tile.getMapLocation()))
                    robot = rc.senseRobotAtLocation(tile.getMapLocation());
                    if (robot != null && robot.getTeam() != rc.getTeam() && robot.getType() == spawnTowerType) {
                        System.out.println("Found enemy tower @ " + tile.getMapLocation());
                        foundLoc = tile.getMapLocation();
                        if (!rc.canAttack(foundLoc)) {
                            Pathfinder.move(foundLoc);
                            if (rc.canAttack(foundLoc)) {
                                rc.attack(foundLoc);
                            }

                        } else {
                            // // Direction reverseDir = rc.getLocation().directionTo(foundLoc).opposite();
                            // if (rc.canAttack(foundLoc)) {
                            //     rc.attack(foundLoc);
                            //     if (rc.canMove(rc.getLocation().directionTo(prevLoc))) {
                            //         rc.move(rc.getLocation().directionTo(prevLoc));
                            //     }
                            // } else {
                            //     System.out.println("I can't attack the enemy tower");
                            // }
                        }
                    }
                }
            }
        }

        for (MapLocation m : potentialEnemySpawnLocations) {
            rc.setIndicatorDot(m, 255, 0, 0);
        }

        if (foundLoc == null) {
            // System.out.println("No enemy tower found");
            for (int i = 0; i < 3; i++) {
                if (!visited[i]) {
                    target = potentialEnemySpawnLocations[i];
                    break;
                }
            }
            Pathfinder.move(target);
        }

        if (foundLoc != null && !rc.canSenseRobotAtLocation(foundLoc)) {
            System.out.println("Haha I destroyed the enemy tower");
            birthRound = rc.getRoundNum();  // no longer do it
        }

        prevLoc = rc.getLocation();
    }
}
