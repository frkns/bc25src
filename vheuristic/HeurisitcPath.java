package vheuristic;

import battlecode.common.*;

public class HeurisitcPath extends RobotPlayer {
    // keep track of the last 8 positions and have a cost for all 8 directions,
    // increasing the cost a bit if it's one of the last 8 positions.
    // plus some other costs for preferring to stay on my own paint, avoiding enemy paint etc
    // also try to move away from the our spawn tower

    static int outOfBoundsPenalty = 300;
    static int enemyPaintPenalty = 2000;

    public static void move() throws GameActionException {
        MapLocation prevLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];  // mathematically consistent negative mod

        int[] directionCost = new int[8];

        int[] prevLocDeltaXs = new int[8];
        int[] prevLocDeltaYs = new int[8];
        for (int i = 0; i < 8; i++) {
            MapLocation loc = locationHistory[i];
            if (loc == null) {
                prevLocDeltaXs[i] = 42;  // some random sentinel value
                prevLocDeltaYs[i] = 42;
                continue;
            }
            Direction dir = rc.getLocation().directionTo(loc);
            prevLocDeltaXs[i] = dir.dx;
            prevLocDeltaYs[i] = dir.dy;
        }

        Direction toSpawnTower = rc.getLocation().directionTo(spawnTowerLocation);
        int toSpawnTowerSpawnDeltaX = toSpawnTower.dx;
        int toSpawnTowerSpawnDeltaY = toSpawnTower.dy;

        int INF = (int)2e9;
        for (int i = 0; i < 8; i++) {
            // if we can't move there, set the cost to infinity
            if (!rc.canMove(directions[i])) {
                directionCost[i] = INF;
                continue;
            }

            // add a cost for moving in a direction that gets closer to the last 8 positions
            Direction dir = directions[i];
            for (int deltaX : prevLocDeltaXs) {
                if (deltaX == dir.dx) {
                    directionCost[i] += 500;
                }
            }
            for (int deltaY : prevLocDeltaYs) {
                if (deltaY == dir.dy) {
                    directionCost[i] += 500;
                }
            }

            // add a cost for moving in a direction that gets closer to the tower that spawned us
            if (dir.dx == toSpawnTowerSpawnDeltaX) {
                directionCost[i] += 100;
            }
            if (dir.dy == toSpawnTowerSpawnDeltaY) {
                directionCost[i] += 100;
            }

            MapLocation newLoc = rc.getLocation().add(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);
            // add a cost if the tile is enemy paint
            if (tileInfo.getPaint().isEnemy()) {
                directionCost[i] += enemyPaintPenalty;
            }
            // add a cost if the tile is neutral paint
            else if (tileInfo.getPaint() == PaintType.EMPTY) {
                directionCost[i] += 1000;
            }

            // add a cost for moving back to previous location
            if (newLoc.equals(prevLoc)) {
                directionCost[i] += 1000;
            }

            // add a cost if the tile is out of exploration bounds
            if (Utils.outOfExplorationBounds(newLoc)) {
                directionCost[i] += outOfBoundsPenalty;
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
        else {
            rc.setIndicatorString("No valid direction to move in");
        }
    }




}
