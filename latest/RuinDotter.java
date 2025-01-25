package latest;

import battlecode.common.*;

public class RuinDotter extends RobotPlayer {

    static MapLocation target;

    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode


    static void init() throws GameActionException {
        potentialEnemySpawnLocations[0] = Utils.mirror(spawnTowerLocation);
        potentialEnemySpawnLocations[1] = Utils.verticalMirror(spawnTowerLocation);
        potentialEnemySpawnLocations[2] = Utils.horizontalMirror(spawnTowerLocation);
    }

    static boolean[] visited = new boolean[3];


    static void run() throws GameActionException {


        rc.setIndicatorString("Ruin Dotter");

        // ImpureUtils.updateNearbyUnits();
        ImpureUtils.updateNearestEnemyTower();
        ImpureUtils.updateNearbyMask(false);

        for (int i = 0; i < 3; i++) {
            MapLocation tileLoc = potentialEnemySpawnLocations[i];
            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 20)) {
                visited[i] = true;
            }
        }

        for (int i = 0; i < 3; i++) {
            if (!visited[i]) {
                target = potentialEnemySpawnLocations[i];
                break;
            }
        }


        if (visited[0] && visited[1] && visited[2]) {
            System.out.println("Ruin Dotter visited all 3 syms");
            HeuristicPath.move();
        } else {
            Pathfinder.move(target, false);
            assert(potentialEnemySpawnLocations[0].equals(Utils.mirror(spawnTowerLocation)));
            // rc.setIndicatorDot(potentialEnemySpawnLocations[0], 255, 0, 255);
            // rc.setIndicatorDot(Utils.mirror(spawnTowerLocation), 255, 0, 255);
            // rc.setIndicatorDot(spawnTowerLocation, 255, 0, 255);
            rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 255);
        }


        // dot nearby empty/ enemy ruins
        nearbyRuins = rc.senseNearbyRuins(-1);
        if (rc.isActionReady()) {
            MapLocation closestDot = null;
            MapLocation ruinLoc = null;
            for (MapLocation tileLoc : nearbyRuins) {
                MapLocation tentativeDot = Utils.nearestEmptyOnRuinIfEnemyOrIsUndotted(tileLoc);
                if (tentativeDot != null) {
                    if (closestDot == null || rc.getLocation().distanceSquaredTo(tentativeDot) < rc.getLocation().distanceSquaredTo(closestDot)) {
                        closestDot = tentativeDot;
                        ruinLoc = tileLoc;
                    }
                }
            }
            if (rc.canAttack(closestDot)) {
                boolean[][] towerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
                int delta_x = ruinLoc.x - closestDot.x;
                int delta_y = ruinLoc.y - closestDot.y;
                int mask_x = 2 - delta_x; // towerPatter[2][2] is the center
                int mask_y = 2 - delta_y;

                if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
                    System.out.println("ruin dotter deltas are off?");
                }

                boolean useSecondary = towerPattern[mask_x][mask_y];
                rc.attack(closestDot, useSecondary);  // paint the money pattern
            }
        }


    }
}
