package lmx;

import battlecode.common.*;
import java.util.Arrays;
import java.util.Comparator;

public class AttackBase extends RobotPlayer {
    // create an array of the 3 possible symmetries for enemy spawn location
    static MapLocation[] potentialEnemySpawnLocations = new MapLocation[3];
    static boolean[] visited = new boolean[3]; // True if symmetries have been checked
    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode

    static MapLocation foundLoc = null;
    static MapInfo foundLocInfo = null;
    static MapLocation target;

    // simpler if false but also take more damage
    static final boolean ATTACK_MICRO = true;  // do we want to shift back and forth to avoid tower shots?


    static void init() throws GameActionException {
        Debug.print("Init AttackBase");

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
        Debug.print("Action AttackBase");
        rc.setIndicatorString("Base attacker");

        ImpureUtils.updateNearbyUnits();

        rc.setIndicatorDot(avgClump, 0, 0, 255);
        ImpureUtils.updateNearestEnemyTower();

        if (nearestEnemyTower != null) {
            Debug.print(1, "Attacking");
            rc.setIndicatorString("Attacking");

            if (rc.canAttack(nearestEnemyTower)) {
                Debug.print(2, "Attack " + nearestEnemyTower);
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }

            HeuristicPath.move(nearestEnemyTower, Behavior.TOWER_MICRO);

            inTowerRange = false;
            if (rc.canAttack(nearestEnemyTower)) {
                Debug.print(2, "Attack " + nearestEnemyTower);
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
        }

        if (foundLoc == null) {
            Debug.print(1, "Calculating for next tower");
            for (MapInfo tile : nearbyTiles) {
                for (int i = 0; i < 3; i++) {
                    if (tile.getMapLocation().equals(potentialEnemySpawnLocations[i])) {
                        Debug.print("In range of **potential** enemy spawn location @ " + potentialEnemySpawnLocations[i]);
                        visited[i] = true;
                        RobotInfo robot = null;
                        if (rc.canSenseRobotAtLocation(tile.getMapLocation())){
                            robot = rc.senseRobotAtLocation(tile.getMapLocation());
                        }else {
                            Debug.print(2, "No tower" + tile.getMapLocation());
                        }
                        if (robot != null && robot.getTeam() != rc.getTeam() && robot.getType() == spawnTowerType) {
                            Debug.print(2, "Found enemy tower @ " + tile.getMapLocation());
                            foundLocInfo = tile;
                            foundLoc = tile.getMapLocation();
                        }
                    }
                }
            }
        }

        if (foundLoc == null) {
            Debug.print(1, "No enemy tower found");
            for (int i = 0; i < 3; i++) {
                if (!visited[i]) {
                    target = potentialEnemySpawnLocations[i];
                    break;
                }
            }
            if (rc.isMovementReady()) {
                Pathfinder.move(target); // todos : use move with heuristic where target is given by pathfinder
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
                    if (tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance) {
                        distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                        closestRuinToDot = tile.getMapLocation();
                    }
                }
            }
            if (closestRuinToDot != null) {
                MapLocation locToDot = Utils.nearestEmptyOnRuinIfEnemyOrIsUndotted(closestRuinToDot);
                if (locToDot != null && rc.canAttack(locToDot)) {
                    // System.out.println("base attacker: dotted a ruin");
                    rc.attack(locToDot);
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
            behavior = Behavior.SOLDIER;
        } else if (visited[0] && visited[1] && visited[2] && nearestEnemyTower == null) {  // visited everything
            behavior = Behavior.SOLDIER;
        }

    }
}
