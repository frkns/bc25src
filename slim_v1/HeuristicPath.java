package slim_v1;

import battlecode.common.*;

public class HeuristicPath extends RobotPlayer {
    // keep track of the last 8 positions and have a cost for all 8 directions,
    // increasing the cost a bit if it's one of the last 8 positions.
    // plus some other costs for preferring to stay on my own paint, avoiding enemy paint etc
    // also try to move away from the our spawn tower

    static int enemyTowerPenalty = 1_000_000;  // set this negative to incentivize moving into tower range
    static int enemyPaintPenalty = 4000;
    static int neutralPaintPenalty = 2000;
    static int targetIncentive = 500;  // 1500 = almost guaranteed to make it

    public static void move() throws GameActionException {
        move(null);
    }

    public static void move(MapLocation targetLoc) throws GameActionException {
        int[] directionCost = new int[8];

        MapLocation nearbyEnemyTowerLoc = nearestEnemyTower;

        Direction toSpawnTower = rc.getLocation().directionTo(spawnTowerLocation);

        int INF = (int)2e9;
        for (int i = 0; i < 8; i++) {
            Direction dir = directions[i];

            // if we can't move there, set the cost to infinity
            if (!rc.canMove(dir)) {
                directionCost[i] = INF;
                continue;
            }

            // add a cost for moving in a direction that gets closer to the last 8 positions
            for (MapLocation prevLoc : locationHistory) {
                if (prevLoc == null)
                    continue;
                if (dir == rc.getLocation().directionTo(prevLoc)) {
                    directionCost[i] += 1000;
                }
            }

            // add a cost for moving in a direction that gets closer to the tower that spawned us
            if (dir == rc.getLocation().directionTo(spawnTowerLocation))
                directionCost[i] += 200;

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);
            // add a cost if the tile is enemy paint
            if (tileInfo.getPaint().isEnemy()) {
                directionCost[i] += enemyPaintPenalty;
            }
            // add a cost if the tile is neutral paint
            else if (tileInfo.getPaint() == PaintType.EMPTY) {
                if ((!rc.isActionReady() || isRefilling))
                    directionCost[i] += neutralPaintPenalty;
                // assume we can paint under ourselves so no cost is added if action ready
            }
            // add a cost for moving in range of an enemy tower
            if (nearbyEnemyTowerLoc != null && newLoc.isWithinDistanceSquared(nearbyEnemyTowerLoc, 9)) {
                directionCost[i] += enemyTowerPenalty;
            }

            // add a cost if the tile is out of exploration bounds
            // if (Utils.outOfExplorationBounds(newLoc)) {
            //     directionCost[i] += 1000;
            // }

            // add a cost if new location is the previous one
            MapLocation lastLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];
            if (newLoc.equals(lastLoc)) {
                directionCost[i] += 1000;
            }

            if (targetLoc != null) {
                // add cost for moving in a direction that gets us further away from target
                directionCost[i] += Utils.manhattanDistance(newLoc, targetLoc) * targetIncentive;
            }

        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
        for (int i = 0; i < 8; i++) {
            if (directionCost[i] < minCost) {
                minCost = directionCost[i];
                minDir = directions[i];
            }
        }
        if (minDir != null)
            rc.move(minDir);
    }


    public static void moveToWrongInRuin() throws GameActionException {
        MapLocation ruinLoc = curRuin.getMapLocation();

        int[] directionCost = new int[8];

        int INF = (int)2e9;
        for (int i = 0; i < 8; i++) {
            Direction dir = directions[i];

            // if we can't move there, set the cost to infinity
            if (!rc.canMove(dir)) {
                directionCost[i] = INF;
                continue;
            }

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);

            // add a cost if new location is the previous one
            MapLocation lastLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];
            if (newLoc.equals(lastLoc)) {
                directionCost[i] += 1000;
            }
            lastLoc = locationHistory[(rc.getRoundNum() - 2 + 8) % 8];
            if (newLoc.equals(lastLoc)) {
                directionCost[i] += 1000;
            }

            if (nearestEmptyTile != null) {
                // add cost for moving in a direction that gets us further away from nearest empty tile
                directionCost[i] += Utils.manhattanDistance(newLoc, nearestWrongInRuin) * 500 * 3;
            }

            if (ruinLoc != null) {
                // add cost for moving in a direction that gets us further away from target
                directionCost[i] += Utils.manhattanDistance(newLoc, ruinLoc) * 500;
            }

        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
        for (int i = 0; i < 8; i++) {
            if (directionCost[i] < minCost) {
                minCost = directionCost[i];
                minDir = directions[i];
            }
        }
        if (minDir != null)
            rc.move(minDir);
    }


    public static void moveToWrongInSRP() throws GameActionException {
        int[] directionCost = new int[8];

        int INF = (int)2e9;
        for (int i = 0; i < 8; i++) {
            Direction dir = directions[i];

            // if we can't move there, set the cost to infinity
            if (!rc.canMove(dir)) {
                directionCost[i] = INF;
                continue;
            }

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);

            // add a cost if new location is the previous one
            MapLocation lastLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];
            if (newLoc.equals(lastLoc)) {
                directionCost[i] += 200;
            }

            if (nearestEmptyTile != null) {
                // add cost for moving in a direction that gets us further away from nearest empty tile
                directionCost[i] += Utils.manhattanDistance(newLoc, nearestWrongInSRP) * 500;
            }

            if (curSRP != null) {
                // add cost for moving in a direction that gets us further away from target
                directionCost[i] += Utils.manhattanDistance(newLoc, curSRP) * 500;
            }

        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
        for (int i = 0; i < 8; i++) {
            if (directionCost[i] < minCost) {
                minCost = directionCost[i];
                minDir = directions[i];
            }
        }
        if (minDir != null)
            rc.move(minDir);
    }


    public static void circleSRP() throws GameActionException {
        int[] directionCost = new int[8];

        int INF = (int)2e9;
        for (int i = 0; i < 8; i++) {
            Direction dir = directions[i];

            // if we can't move there, set the cost to infinity
            if (!rc.canMove(dir)) {
                directionCost[i] = INF;
                continue;
            }

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);

            // add a cost if new location is the previous one
            MapLocation lastLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];
            if (newLoc.equals(lastLoc)) {
                directionCost[i] += 500;
            }

            // add a cost if the tile is enemy paint
            if (tileInfo.getPaint().isEnemy()) {
                directionCost[i] += 3000;
            }

            // add a cost if new location is not in a circle around it
            if (Utils.chessDistance(Soldiers.lastSRPloc, newLoc) != 3) {
                directionCost[i] += 5500;
            }

            // add cost for moving in a direction that gets us further away from target
            directionCost[i] += Utils.manhattanDistance(Soldiers.lastSRPloc, newLoc) * 500;
        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
        for (int i = 0; i < 8; i++) {
            if (directionCost[i] < minCost) {
                minCost = directionCost[i];
                minDir = directions[i];
            }
        }
        if (minDir != null)
            rc.move(minDir);
    }

    public static void towerMicro() throws GameActionException {
        int[] directionCost = new int[8];

        int INF = (int)2e9;
        for (int i = 0; i < 8; i++) {
            Direction dir = directions[i];

            // if we can't move there, set the cost to infinity
            if (!rc.canMove(dir)) {
                directionCost[i] = INF;
                continue;
            }

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);

            // add a cost if the tile is not ally paint
            if (!tileInfo.getPaint().isAlly()) {
                directionCost[i] += 3000;
            }


            // if we are not inside tower range we should move inside it
            if (Soldiers.inTowerRange) {
                if (newLoc.isWithinDistanceSquared(nearestEnemyTower, 9)) {
                    directionCost[i] += 9000;
                }
            } else {
                if (!newLoc.isWithinDistanceSquared(nearestEnemyTower, 9)) {
                    directionCost[i] += 9000;
                }
            }

            // add cost for moving in a direction that gets us further away from target
            directionCost[i] += Utils.manhattanDistance(nearestEnemyTower, newLoc) * 500;
        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
        for (int i = 0; i < 8; i++) {
            if (directionCost[i] < minCost) {
                minCost = directionCost[i];
                minDir = directions[i];
            }
        }
        if (minDir != null)
            rc.move(minDir);
    }

}
