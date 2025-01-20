package ref_best;

import battlecode.common.*;
import java.util.Arrays;
import java.util.Comparator;

public class AttackBase extends RobotPlayer {

    // create an array of the 3 possible symetries for enemy spawn location
    static MapLocation[] potentialEnemySpawnLocations = new MapLocation[3];

    static MapLocation target;

    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode


    static void init() throws GameActionException {
        potentialEnemySpawnLocations[0] = Utils.mirror(spawnTowerLocation);
        potentialEnemySpawnLocations[1] = Utils.verticalMirror(spawnTowerLocation);
        potentialEnemySpawnLocations[2] = Utils.horizontalMirror(spawnTowerLocation);

        if (spawnTowerLocation == null) {
            System.out.println("Spawn tower location not found? This should not happen.");
            return;
        }

        // Sort potentialEnemySpawnLocations based on distance to current location
        Arrays.sort(potentialEnemySpawnLocations, Comparator.comparingInt(loc -> rc.getLocation().distanceSquaredTo(loc)));
    }

    static boolean[] visited = new boolean[3];
    static MapLocation foundLoc = null;
    static MapInfo foundLocInfo = null;


    static void run() throws GameActionException {


        rc.setIndicatorString("Base attacker");

        // ImpureUtils.updateNearbyUnits();
        ImpureUtils.updateNearestEnemyTower();
        ImpureUtils.updateNearbyMask(false);

        if (nearestEnemyTower != null) {
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
            if (rc.getHealth() < 31) {  // stop attacking if low health, 30 or less means we die to level 1 paint/money tower shot + AoE
                role = 0;
                Soldiers.run();
                return;
            }
            HeuristicPath.towerMicro();
            inTowerRange = rc.getLocation().isWithinDistanceSquared(nearestEnemyTower, 9);
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
        }

        if (foundLoc == null)
        for (MapInfo tile : nearbyTiles) {
            for (int i = 0; i < 3; i++) {
                if (tile.getMapLocation().equals(potentialEnemySpawnLocations[i])) {
                    // System.out.println("Found **potential** enemy spawn location");
                    visited[i] = true;
                    RobotInfo robot = null;
                    if (rc.canSenseRobotAtLocation(tile.getMapLocation()))
                        robot = rc.senseRobotAtLocation(tile.getMapLocation());
                    else {
                        // System.out.println("Haha I destroyed the enemy tower");
                        // role = 0;
                    }
                    if (robot != null && robot.getTeam() != rc.getTeam() && robot.getType() == spawnTowerType) {
                        // System.out.println("Found enemy tower @ " + tile.getMapLocation());
                        foundLocInfo = tile;
                        foundLoc = tile.getMapLocation();
                    }
                }
            }
        }


        // // * needed if using attackBaseMove
        // wallAdjacent = false;
        // for (MapInfo tile : rc.senseNearbyMapInfos(1)) {
        //     if (tile.isWall()) {
        //         wallAdjacent = true;
        //         break;
        //     }
        // }
        // if (wallAdjacent) {
        //     if (wallRounds++ == 0 && target != null) {
        //         sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
        //     }
        // } else {
        //     wallRounds = 0;
        //     if (target != null)
        //         sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
        // }
        // /*  */

        if (foundLoc == null) {
            // System.out.println("No enemy tower found");
            for (int i = 0; i < 3; i++) {
                if (!visited[i]) {
                    target = potentialEnemySpawnLocations[i];
                    break;
                }
            }
            if (rc.isMovementReady()) {
                if (target == null) {
                    role = 0;
                    return;
                }
                Pathfinder.move(target);
                // HeuristicPath.attackBaseMove(target);
            }
        }

        // MapLocation loc = rc.getLocation();
        // MapInfo locInfo = rc.senseMapInfo(loc);

        // dot nearby empty/ enemy ruins
        if (rc.isActionReady()) {
            MapLocation closestDot = null;
            for (MapInfo tile : nearbyTiles) {
                if (tile.hasRuin()) {
                    MapLocation tentativeDot = Utils.nearestEmptyOnRuinIfEnemyOrIsUndotted(tile.getMapLocation());
                    if (tentativeDot != null) {
                        if (closestDot == null || rc.getLocation().distanceSquaredTo(tentativeDot) < rc.getLocation().distanceSquaredTo(closestDot)) {
                            closestDot = tentativeDot;
                        }
                    }
                }
            }
            if (rc.canAttack(closestDot)) {
                rc.attack(closestDot);
            }
        }


        boolean fullFilling = rc.getRoundNum() >= fullFillPhase;
        if (rc.getPaint() > 150 && fullFilling) {
            ImpureUtils.paintFloor();
            // _attackableNearbyTiles = rc.senseNearbyMapInfos(9);
            // for (MapInfo tile : _attackableNearbyTiles) {
            //     if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
            //         rc.attack(tile.getMapLocation());
            //     }
            // }
        }

        if (foundLoc != null && !rc.canSenseRobotAtLocation(foundLoc)) {
            // System.out.println("Haha I destroyed the enemy tower");
            role = 0;
            // Soldiers.run();
            return;
        } else if (visited[0] && visited[1] && visited[2] && nearestEnemyTower == null) {  // visited everything
            role = 0;
            // Soldiers.run();
            return;
        }

    }
}
