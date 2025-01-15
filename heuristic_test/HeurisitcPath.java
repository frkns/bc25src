package heuristic_test;

import battlecode.common.*;

public class HeurisitcPath extends RobotPlayer {
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

        MapLocation nearbyEnemyTowerLoc = null;
        for (RobotInfo robot : nearbyRobots) {  // assumes non-defense tower
            if (robot.getTeam() != rc.getTeam() && robot.getType().isTowerType()) {
                nearbyEnemyTowerLoc = robot.getLocation();
                break;
            }
        }

        Direction toSpawnTower = rc.getLocation().directionTo(spawnTowerLocation);

        int INF = (int)2e9;
        for (int i = 0; i < 8; i++) {
            // if we can't move there, set the cost to infinity
            if (!rc.canMove(directions[i])) {
                directionCost[i] = INF;
                continue;
            }

            // add a cost for moving in a direction that gets closer to the last 8 positions
            Direction dir = directions[i];
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

            MapLocation newLoc = rc.getLocation().add(dir);
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
                directionCost[i] += 500;
            }

            if (targetLoc != null) {
                // remove cost for moving in a direction that gets us further away from target
                directionCost[i] += Utils.manhattanDistance(newLoc, targetLoc) * targetIncentive;

                // if (rc.getRoundNum() % 100 == 0)
                //     System.out.println(newLoc.distanceSquaredTo(targetLoc));

                // Direction toTarget = rc.getLocation().directionTo(targetLoc);
                // if (dir.dx == toTarget.dx) {
                //     directionCost[i] -= targetIncentive;
                // }
                // if (dir.dy == toTarget.dy) {
                //     directionCost[i] -= targetIncentive;
                // }
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




}
