package ref_best_tmp;

import battlecode.common.*;

public class AttackBase extends RobotPlayer {


    static MapLocation target;

    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode


    static void init() throws GameActionException {
        potentialEnemySpawnLocations[0] = Utils.mirror(spawnTowerLocation);
        potentialEnemySpawnLocations[1] = Utils.verticalMirror(spawnTowerLocation);
        potentialEnemySpawnLocations[2] = Utils.horizontalMirror(spawnTowerLocation);

        // Sort potentialEnemySpawnLocations based on distance to current location
        // Arrays.sort(potentialEnemySpawnLocations, Comparator.comparingInt(loc -> rc.getLocation().distanceSquaredTo(loc)));
        MapLocation loc1 = potentialEnemySpawnLocations[0];
        MapLocation loc2 = potentialEnemySpawnLocations[1];
        MapLocation loc3 = potentialEnemySpawnLocations[2];
        int dist1 = rc.getLocation().distanceSquaredTo(loc1);
        int dist2 = rc.getLocation().distanceSquaredTo(loc2);
        int dist3 = rc.getLocation().distanceSquaredTo(loc3);
        if (dist1 > dist2) {
            MapLocation temp = loc1;
            loc1 = loc2;
            loc2 = temp;
            int tempDist = dist1;
            dist1 = dist2;
            dist2 = tempDist;
        }
        if (dist2 > dist3) {
            MapLocation temp = loc2;
            loc2 = loc3;
            loc3 = temp;
            int tempDist = dist2;
            dist2 = dist3;
            dist3 = tempDist;
        }
        if (dist1 > dist2) {
            MapLocation temp = loc1;
            loc1 = loc2;
            loc2 = temp;
            int tempDist = dist1;
            dist1 = dist2;
            dist2 = tempDist;
        }
        potentialEnemySpawnLocations[0] = loc1;
        potentialEnemySpawnLocations[1] = loc2;
        potentialEnemySpawnLocations[2] = loc3;
        totalManDist = Utils.manhattanDistance(rc.getLocation(), loc1) + Utils.manhattanDistance(loc1, loc2) + Utils.manhattanDistance(loc2, loc3);
    }

    static boolean[] visited = new boolean[3];
    static MapLocation foundLoc = null;
    static MapInfo foundLocInfo = null;


    static void run() throws GameActionException {


        rc.setIndicatorString("Base attacker");

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
            return;
        }

        if (foundLoc == null)
        for (int i = 0; i < 3; i++) {
            MapLocation tileLoc = potentialEnemySpawnLocations[i];
            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 20)) {
                // System.out.println("Found **potential** enemy spawn location");
                visited[i] = true;
                RobotInfo robot = null;
                if (rc.canSenseRobotAtLocation(tileLoc))
                    robot = rc.senseRobotAtLocation(tileLoc);
                else {
                    // System.out.println("Haha I destroyed the enemy tower");
                    // role = 0;
                }
                if (robot != null && robot.getTeam() != rc.getTeam() && robot.getType() == spawnTowerType) {
                    // System.out.println("Found enemy tower @ " + tile.getMapLocation());
                    // foundLocInfo = tile;
                    foundLoc = tileLoc;
                }
            }
        }

        if (fstTowerTarget != null) {
            MapLocation tileLoc = fstTowerTarget;
            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 20)) {
                visFstTowerTarget = true;
            }
        }
        if (sndTowerTarget != null) {
            MapLocation tileLoc = sndTowerTarget;
            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 20)) {
                visSndTowerTarget = true;
            }
        }


        MapLocation tentativeTarget = Utils.chooseTowerTarget();
        if (tentativeTarget != null && rc.getID() % 3 != 0) {
            target = tentativeTarget;
        } else
        if (foundLoc == null) {
            // System.out.println("No enemy tower found");
            for (int i = 0; i < 3; i++) {
                if (!visited[i]) {
                    target = potentialEnemySpawnLocations[i];
                    break;
                }
            }
        }

        if (rc.isMovementReady()) {
            if (target == null) {
                role = 0;
                Soldiers.run();
                return;
            }

        }

        assert(target != null);

        Pathfinder.move(target, true);

        rc.setIndicatorLine(rc.getLocation(), target, 255, 255, 255);

        // dot nearby empty/ enemy ruins
        nearbyRuins = rc.senseNearbyRuins(-1);
        if (rc.isActionReady()) {
            MapLocation closestDot = null;
            for (MapLocation tileLoc : nearbyRuins) {
                MapLocation tentativeDot = Utils.nearestEmptyOnRuinIfEnemyOrIsUndotted(tileLoc);
                if (tentativeDot != null) {
                    if (closestDot == null || rc.getLocation().distanceSquaredTo(tentativeDot) < rc.getLocation().distanceSquaredTo(closestDot)) {
                        closestDot = tentativeDot;
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
        }
        if (rc.getPaint() > 150 && rc.getRoundNum() >= fullAttackBasePhase) {
            _attackableNearbyTiles = rc.senseNearbyMapInfos(9);
            for (MapInfo tile : _attackableNearbyTiles) {
                if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation());
                }
            }
        }

        if (foundLoc != null && !rc.canSenseRobotAtLocation(foundLoc)) {
            role = 0;
        } else if (visited[0] && visited[1] && visited[2] && nearestEnemyTower == null) {  // visited everything
            role = 0;
        }

    }
}
