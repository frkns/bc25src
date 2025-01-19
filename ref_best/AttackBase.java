package ref_best;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

import gavin.fast.FastLocSet;

public class AttackBase extends RobotPlayer {

    // create an array of the 3 possible symmetries for enemy spawn location
    static MapLocation[] potentialEnemySpawnLocations = new MapLocation[3];
    static boolean[] visited = new boolean[3]; // True if symmetries have been checked
    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode

    static MapLocation foundLoc = null;
    static MapInfo foundLocInfo = null;
    static MapLocation target;

    static FastLocSet dottedRuins = new FastLocSet();

    // simpler if false but also take more damage
    static final boolean ATTACK_MICRO = true;  // do we want to shift back and forth to avoid tower shots?


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

    static void run() throws GameActionException {
        rc.setIndicatorString("Base attacker");

        ImpureUtils.updateNearbyUnits();

        rc.setIndicatorDot(avgClump, 0, 0, 255);
        ImpureUtils.updateNearestEnemyTower();

        if (nearestEnemyTower != null) {
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
            HeuristicPath.towerMicro();
            inTowerRange = false;
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
        }

        if (foundLoc == null) {
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
        }

        // if (nearestEnemyTower != null) {
        //     if (rc.canAttack(nearestEnemyTower)) {
        //         rc.attack(nearestEnemyTower);
        //         inTowerRange = true;
        //     }
        //     if (rc.isActionReady())
        //         HeuristicPath.towerMicro();
        //     inTowerRange = false;
        //     if (rc.canAttack(nearestEnemyTower)) {
        //         rc.attack(nearestEnemyTower);
        //         inTowerRange = true;
        //     }
        // }

        if (foundLoc == null) {
            // System.out.println("No enemy tower found");
            for (int i = 0; i < 3; i++) {
                if (!visited[i]) {
                    target = potentialEnemySpawnLocations[i];
                    break;
                }
            }
            if (rc.isMovementReady()) {
                Pathfinder.move(target);
                // HeuristicPath.attackBaseMove(target);
            }
        }

        MapLocation loc = rc.getLocation();
        MapInfo locInfo = rc.senseMapInfo(loc);

        // dot nearby empty/ enemy ruins
        if (rc.isActionReady()) {
            MapLocation closestRuinToDot = null;

            int distance = (int)2e9;

            for (MapInfo tile : nearbyTiles) {
                if (tile.hasRuin()) {
                    if (tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance && !dottedRuins.contains(tile.getMapLocation())) {
                        distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                        closestRuinToDot = tile.getMapLocation();

                        //rc.setIndicatorLine(rc.getLocation(),tile.getMapLocation(),255,0,0);
                    }
                }
            }
            if (closestRuinToDot != null) {
                MapLocation locToDot = Utils.nearestEmptyOnRuinIfEnemyOrIsUndotted(closestRuinToDot);
                if (locToDot != null && rc.canAttack(locToDot)) {
                    // System.out.println("base attacker: dotted a ruin");
                    rc.attack(locToDot);
                    dottedRuins.add(closestRuinToDot);
                }
            }
        }


        boolean fullFilling = rc.getRoundNum() >= fullFillPhase;
        if (rc.getPaint() > 150 && fullFilling) {
            ImpureUtils.paintFloor();
            _attackableNearbyTiles = rc.senseNearbyMapInfos(9);
            for (MapInfo tile : _attackableNearbyTiles) {
                if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation());
                }
            }
        }

        if (foundLoc != null && !rc.canSenseRobotAtLocation(foundLoc)) {
            // System.out.println("Haha I destroyed the enemy tower");
            role = 0;
        } else if (visited[0] && visited[1] && visited[2] && nearestEnemyTower == null) {  // visited everything
            role = 0;
        }

    }
}
