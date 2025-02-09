package kenny;

import battlecode.common.*;

public class HeuristicPath extends RobotPlayer {
    // keep track of the last 8 positions and have a cost for all 8 directions,
    // increasing the cost a bit if it's one of the last 8 positions.
    // plus some other costs for preferring to stay on my own paint, avoiding enemy
    // paint etc
    // (also try to move away from our spawn tower)
    // -- Super Cow Powers

    // these are only for soldier explore and refill?
    static int enemyTowerPenalty = 1_000_000;
    static int neutralPaintPenalty = 3000;
    static boolean fullFill = false; // do we want to prioritize starting to paint everything
    /* */

    static int targetIncentive = 500;

    public static void move() throws GameActionException {
        move(null);
    }

    // move = soldier move
    public static void move(MapLocation targetLoc) throws GameActionException {
        // rc.setIndicatorString("HeuristicPath.move() mode");

        int[] directionCost = new int[8];
        Direction toSpawnTower = rc.getLocation().directionTo(spawnTowerLocation);

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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


            // add a cost for moving in a direction that gets closer to the tower that
            // spawned us
            if (dir == rc.getLocation().directionTo(spawnTowerLocation))
                directionCost[i] += 200;

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);
            // add a cost if the tile is enemy paint
            if (tileInfo.getPaint().isEnemy()) {
                MapInfo nextnext = null;
                if (rc.canSenseLocation(newLoc.add(dir)))
                    nextnext = rc.senseMapInfo(newLoc.add(dir));
                if (nextnext != null && nextnext.isPassable() && !nextnext.getPaint().isEnemy()) { // don't add the big
                                                                                                   // cost if it's only
                                                                                                   // 1 layer

                    directionCost[i] += 1000;
                } else {
                    directionCost[i] += 3000;
                }
                if (rc.getRoundNum() < mx) {
                    directionCost[i] -= 1000;  // lessen the cost early game
                }
            }
            // add a cost if the tile is neutral paint
            else if (tileInfo.getPaint() == PaintType.EMPTY) {
                if ((!rc.isActionReady()))
                    directionCost[i] += neutralPaintPenalty;
                // assume we can paint under ourselves so no cost is added if action ready
            }
            // add a cost for moving in range of an enemy tower
            if (nearestEnemyTower != null && newLoc.isWithinDistanceSquared(nearestEnemyTower,
                    nearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                directionCost[i] += enemyTowerPenalty;
            }
            if (sndNearestEnemyTower != null && newLoc.isWithinDistanceSquared(sndNearestEnemyTower,
                    sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                directionCost[i] += enemyTowerPenalty;
            }

            // bug nav?
            if (wallAdjacent && rc.getLocation().distanceSquaredTo(targetLoc) > sqDistanceToTargetOnWallTouch) {
                boolean newLocIsWallAdjacent = false;
// Check direction4[0]
if (rc.canSenseLocation(newLoc.add(directions4[0]))
    && rc.senseMapInfo(newLoc.add(directions4[0])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[1] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[1]))
    && rc.senseMapInfo(newLoc.add(directions4[1])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[2] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[2]))
    && rc.senseMapInfo(newLoc.add(directions4[2])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[3] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[3]))
    && rc.senseMapInfo(newLoc.add(directions4[3])).isWall()) {
    newLocIsWallAdjacent = true;
}
                if (newLocIsWallAdjacent)
                    directionCost[i] -= 10000;
            } else {
                // sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(targetLoc);
            }

            // add a cost if new location is the previous one
            MapLocation lastLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];
            if (newLoc.equals(lastLoc)) {
                directionCost[i] += 1000;
            }

            if (targetLoc != null) {
                // add cost for moving in a direction that gets us further away from target
                directionCost[i] += Utils.manhattanDistance(newLoc, targetLoc) * targetIncentive * 1.5;
            }

            if (fullFill && nearestEmptyTile != null) {
                directionCost[i] += Utils.manhattanDistance(newLoc, nearestEmptyTile) * 700;
            }

            if (rc.getRoundNum() >= fullAttackBasePhase && rc.getID() % 3 == 0) {
                MapLocation tentativeTarget = Utils.chooseTowerTarget();
                if (tentativeTarget != null)
                    directionCost[i] += Utils.manhattanDistance(newLoc, tentativeTarget) * 1000;
            }

            // clump avoidance
            int maskx = dir.dx + 2;
            int masky = dir.dy + 2;
            int allyRobotsInNewLoc = 0;
if (nearbyAlliesMask[maskx + dx8[7]][masky + dy8[7]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[6]][masky + dy8[6]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[5]][masky + dy8[5]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[4]][masky + dy8[4]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[3]][masky + dy8[3]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[2]][masky + dy8[2]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[1]][masky + dy8[1]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[0]][masky + dy8[0]]) {
    allyRobotsInNewLoc++;
}
            directionCost[i] += allyRobotsInNewLoc * 1000;

            // add a stacking cost for staying current quadrant for too long (doesnt seem to
            // work)
            // if (rc.getRoundNum() >= 400) {
            // int curQ = Utils.currentQuadrant();
            // directionCost[i] -= Utils.manhattanDistance(newLoc, quadrantCorners[curQ])
            // * Math.max(100, roundsSpentInQuadrant[curQ]) // only apply the penalty after
            // a some # of rounds in quadrant
            // * 30;
            // }
        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null && rc.canMove(minDir))  // because of negative costs, we have to put canMove check
            rc.move(minDir);
    }

    public static void refill(MapLocation targetLoc) throws GameActionException {
        int[] directionCost = new int[8];

        MapLocation nearbyEnemyTowerLoc = nearestEnemyTower;

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);

            // add a cost if the tile is enemy paint
            if (tileInfo.getPaint().isEnemy()) {
                directionCost[i] += 1100;
            }
            // add a cost if the tile is neutral paint
            else if (tileInfo.getPaint() == PaintType.EMPTY) {
                directionCost[i] += 1100;
            }
            // add a cost for moving in range of an enemy tower
            if (nearestEnemyTower != null && newLoc.isWithinDistanceSquared(nearestEnemyTower,
                    nearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                directionCost[i] += enemyTowerPenalty;
            }
            if (sndNearestEnemyTower != null && newLoc.isWithinDistanceSquared(sndNearestEnemyTower,
                    sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                directionCost[i] += enemyTowerPenalty;
            }


            // bug nav?
            if (wallAdjacent && rc.getLocation().distanceSquaredTo(targetLoc) > sqDistanceToTargetOnWallTouch) {
                boolean newLocIsWallAdjacent = false;
// Check direction4[0]
if (rc.canSenseLocation(newLoc.add(directions4[0]))
    && rc.senseMapInfo(newLoc.add(directions4[0])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[1] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[1]))
    && rc.senseMapInfo(newLoc.add(directions4[1])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[2] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[2]))
    && rc.senseMapInfo(newLoc.add(directions4[2])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[3] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[3]))
    && rc.senseMapInfo(newLoc.add(directions4[3])).isWall()) {
    newLocIsWallAdjacent = true;
}
                if (newLocIsWallAdjacent)
                    directionCost[i] -= 10000;
            } else {
                // sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(targetLoc);
            }


            // add a cost if the tile is out of exploration bounds
            // if (Utils.outOfExplorationBounds(newLoc)) {
            // directionCost[i] += 1000;
            // }

            // add a cost if new location is the previous one
            MapLocation lastLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];
            if (newLoc.equals(lastLoc)) {
                directionCost[i] += 1000;
            }

            if (targetLoc != null) {
                // add cost for moving in a direction that gets us further away from target
                directionCost[i] += Utils.manhattanDistance(newLoc, targetLoc) * 1000;
            }

            // clump avoidance
            int maskx = dir.dx + 2;
            int masky = dir.dy + 2;
            int allyRobotsInNewLoc = 0;
if (nearbyAlliesMask[maskx + dx8[7]][masky + dy8[7]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[6]][masky + dy8[6]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[5]][masky + dy8[5]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[4]][masky + dy8[4]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[3]][masky + dy8[3]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[2]][masky + dy8[2]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[1]][masky + dy8[1]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[0]][masky + dy8[0]]) {
    allyRobotsInNewLoc++;
}
            directionCost[i] += allyRobotsInNewLoc * 1000;

        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null)
            rc.move(minDir);
    }

    public static void moveToWrongInRuin() throws GameActionException {
        MapLocation ruinLoc = curRuin.getMapLocation();

        int[] directionCost = new int[8];

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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
            lastLoc = locationHistory[(rc.getRoundNum() - 2 + 8) % 8];
            if (newLoc.equals(lastLoc)) {
                directionCost[i] += 500;
            }

            if (nearestWrongInRuin != null) {
                // add cost for moving in a direction that gets us further away from nearest
                // empty tile
                directionCost[i] += Math.max(9, newLoc.distanceSquaredTo(nearestWrongInRuin)) * 1500;
            }

            if (ruinLoc != null) {
                // add cost for moving in a direction that gets us further away from target
                directionCost[i] += Utils.manhattanDistance(newLoc, ruinLoc) * 500;
                if (Soldiers.numWrongTilesInRuin < 2)
                    directionCost[i] += Utils.manhattanDistance(newLoc, ruinLoc) * 1000;
            }

            // add cost for moving in a direction that gets us gets us closer to a clump (-)
            int maskx = dir.dx + 2;
            int masky = dir.dy + 2;
            int allyRobotsInNewLoc = 0;
if (nearbyAlliesMask[maskx + dx8[7]][masky + dy8[7]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[6]][masky + dy8[6]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[5]][masky + dy8[5]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[4]][masky + dy8[4]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[3]][masky + dy8[3]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[2]][masky + dy8[2]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[1]][masky + dy8[1]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[0]][masky + dy8[0]]) {
    allyRobotsInNewLoc++;
}
            directionCost[i] += allyRobotsInNewLoc * 1000;
        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null)
            rc.move(minDir);
    }

    public static void moveToWrongInSRP() throws GameActionException {
        int[] directionCost = new int[8];

        // if (rc.getLocation().equals(curSRP)) // if we are at it's center, don't
        // move... upd: seems better to move anyway, enables better collaboration
        // return;

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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

            if (nearestWrongInSRP != null) {
                // add cost for moving in a direction that gets us further away from nearest
                // empty tile
                // directionCost[i] += Utils.manhattanDistance(newLoc, nearestWrongInSRP) * 500;
                directionCost[i] += Math.max(9, newLoc.distanceSquaredTo(nearestWrongInSRP)) * 500;
            }

            if (curSRP != null) {
                // add cost for moving in a direction that gets us further away from target
                directionCost[i] += Utils.manhattanDistance(newLoc, curSRP) * 500;
            }

            // add cost for moving in a direction that gets us gets us closer to a clump
            // directionCost[i] -= Utils.manhattanDistance(newLoc, avgClump) *
            // nearbyFriendlyRobots * 50;
            int maskx = dir.dx + 2;
            int masky = dir.dy + 2;
            int allyRobotsInNewLoc = 0;
if (nearbyAlliesMask[maskx + dx8[7]][masky + dy8[7]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[6]][masky + dy8[6]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[5]][masky + dy8[5]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[4]][masky + dy8[4]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[3]][masky + dy8[3]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[2]][masky + dy8[2]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[1]][masky + dy8[1]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[0]][masky + dy8[0]]) {
    allyRobotsInNewLoc++;
}
            directionCost[i] += allyRobotsInNewLoc * 1000;
        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null)
            rc.move(minDir);
    }

    public static void circleSRP() throws GameActionException {
        int[] directionCost = new int[8];

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null)
            rc.move(minDir);
    }

    public static void towerMicro() throws GameActionException {
        int[] directionCost = new int[8];

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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
                directionCost[i] += 10_000;
            }

            // if we are not inside tower range we should move inside it, iff we are able to
            // attack
            if (inTowerRange || !rc.isActionReady()) {
                if (newLoc.isWithinDistanceSquared(nearestEnemyTower, 9)) {
                    directionCost[i] += 100_000;
                }
            } else {
                if (!newLoc.isWithinDistanceSquared(nearestEnemyTower, 9)) {
                    directionCost[i] += 100_000;
                }
            }
            if (sndNearestEnemyTower != null && newLoc.isWithinDistanceSquared(sndNearestEnemyTower,
                    sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                directionCost[i] += 100_000;
            }

            // add cost for moving in a direction that gets us further away from target
            directionCost[i] += Utils.manhattanDistance(nearestEnemyTower, newLoc) * 2000;

            // add cost for moving in a direction that gets us gets us closer to a clump
            // directionCost[i] -= Utils.manhattanDistance(newLoc, avgClump) *
            // nearbyFriendlyRobots * 50;
            int maskx = dir.dx + 2;
            int masky = dir.dy + 2;
            int allyRobotsInNewLoc = 0;
if (nearbyAlliesMask[maskx + dx8[7]][masky + dy8[7]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[6]][masky + dy8[6]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[5]][masky + dy8[5]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[4]][masky + dy8[4]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[3]][masky + dy8[3]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[2]][masky + dy8[2]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[1]][masky + dy8[1]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[0]][masky + dy8[0]]) {
    allyRobotsInNewLoc++;
}
            directionCost[i] += allyRobotsInNewLoc * 1000;
        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null)
            rc.move(minDir);
    }

    public static void mopperMove(MapLocation targetLoc) throws GameActionException {
        int[] directionCost = new int[8];
        MapLocation nearbyEnemyTowerLoc = nearestEnemyTower;

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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
                    directionCost[i] += 500;
                }
            }

            // add cost for moving in a direction that gets us further away from center
            // if (dir != rc.getLocation().directionTo(mapCenter))
            // directionCost[i] += 300;

            // add a cost for moving in a direction that gets closer to the tower that
            // spawned us
            if (dir == rc.getLocation().directionTo(spawnTowerLocation))
                directionCost[i] += 200;

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);

            // add a cost if the tile is enemy paint
            if (tileInfo.getPaint().isEnemy()) {
                if (rc.getNumberTowers() >= startPaintingFloorTowerNum) {
                    directionCost[i] += 6000;
                } else {
                    directionCost[i] += 1500;
                }
                if (!rc.isActionReady()) { // add additional cost if we are not ready to make an action
                    directionCost[i] += 2500;
                }
                if (rc.getPaint() < 20) {
                    directionCost[i] += 4000;  // increase the penalty if we are low paint
                }
            }
            // add a cost if the tile is neutral paint
            else if (tileInfo.getPaint() == PaintType.EMPTY) {
                if (rc.getNumberTowers() >= startPaintingFloorTowerNum) {
                    directionCost[i] += 2500;
                } else {
                    directionCost[i] += 1400;
                }
                if (!rc.isActionReady()) { // add additional cost if we are not ready to make an action
                    directionCost[i] += 2500;
                }
                if (rc.getPaint() < 20) {
                    directionCost[i] += 3000;  // increase the penalty if we are low paint
                }
            }
            // add a cost for moving in range of an enemy tower
            if (nearestEnemyTower != null && newLoc.isWithinDistanceSquared(nearestEnemyTower,
                    nearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {  // account for defense tower range
                directionCost[i] += 30000;
            }
            if (sndNearestEnemyTower != null && newLoc.isWithinDistanceSquared(sndNearestEnemyTower,
                    sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                directionCost[i] += 30000;
            }

            // // bug nav?
            // if (wallAdjacent && rc.getLocation().distanceSquaredTo(targetLoc) > sqDistanceToTargetOnWallTouch) {
            //     boolean newLocIsWallAdjacent = false;
            //     for (Direction d : directions4) {
            //         if (rc.canSenseLocation(newLoc.add(d)))
            //         if (rc.senseMapInfo(newLoc.add(d)).isWall()) {
            //             newLocIsWallAdjacent = true;
            //             break;
            //         }
            //     }
            //     if (newLocIsWallAdjacent)
            //         directionCost[i] -= 10000;
            // } else {
            //     // sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(targetLoc);
            // }


            // add cost for moving in a direction that gets us further away from target
            directionCost[i] += Utils.manhattanDistance(newLoc, targetLoc) * 500;

            // add cost for moving in a direction that gets us further away from enemyPaint,
            // prioritize enemy paint on ruins
            if (nearestEnemyPaint != null) {
                if (nearestEnemyRobot == null || rc.getLocation().distanceSquaredTo(nearestEnemyRobot) > 8
                        || nearestEnemyRobotInfo.getPaintAmount() == 0) // only take this into account if we don't have
                                                                    // really nearby enemy robots
                if (Moppers.nearestEnemyPaintOnRuin == null) {
                    directionCost[i] += Math.max(1, Utils.manhattanDistance(newLoc, nearestEnemyPaint)) * 1000;
                } else {
                    directionCost[i] += Math.max(1, Utils.manhattanDistance(newLoc, nearestEnemyPaint)) * 400;
                    directionCost[i] += Math.max(1, Utils.manhattanDistance(newLoc, Moppers.nearestEnemyPaintOnRuin))
                            * 700;
                    rc.setIndicatorDot(Moppers.nearestEnemyPaintOnRuin, 255, 0, 255);
                }
            }

            if (nearestEnemyRobot != null && nearestEnemyRobotInfo.getPaintAmount() > 0) {
                int x = Utils.manhattanDistance(newLoc, nearestEnemyRobot);
                directionCost[i] += x * 1500;  // add a cost for moving away from nearest enemy
                if (rc.getRoundNum() < 70) {
                    directionCost[i] += x * 2500;  // should really stick to enemies early game
                }
            }

            if (nearestEnemyPaint == null)
                assert (Moppers.nearestEnemyPaintOnRuin == null);

            // small weird declumping thing, not sure if it works
            directionCost[i] += Utils.chessDistance(newLoc, Utils.mirror(spawnTowerLocation)) * 10
                    * nearbyFriendlyRobots;

            // good anti clumping
            int maskx = dir.dx + 2;
            int masky = dir.dy + 2;
            int allyRobotsInNewLoc = 0;
if (nearbyAlliesMask[maskx + dx8[7]][masky + dy8[7]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[6]][masky + dy8[6]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[5]][masky + dy8[5]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[4]][masky + dy8[4]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[3]][masky + dy8[3]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[2]][masky + dy8[2]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[1]][masky + dy8[1]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[0]][masky + dy8[0]]) {
    allyRobotsInNewLoc++;
}
            directionCost[i] += allyRobotsInNewLoc * 500;

        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null)
            rc.move(minDir);
    }

    public static void attackBaseMove(MapLocation targetLoc) throws GameActionException {
        int[] directionCost = new int[8];
        MapLocation nearbyEnemyTowerLoc = nearestEnemyTower;

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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
                    directionCost[i] += 500;
                }
            }

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);

            // add a cost if the tile is enemy paint
            if (tileInfo.getPaint().isEnemy()) {
                directionCost[i] += 1000;
            }
            // add a cost if the tile is neutral paint
            else if (tileInfo.getPaint() == PaintType.EMPTY) {
                directionCost[i] += 1000;
            }


            // add cost for moving in a direction that gets us further away from target
            directionCost[i] += Utils.manhattanDistance(newLoc, targetLoc) * 1500;

            // add a cost for moving away from nearest enemy tower
            if (nearestEnemyTower != null)
                directionCost[i] += Utils.manhattanDistance(newLoc, nearestEnemyTower) * 2000;

            // bug nav?
            if (wallAdjacent && rc.getLocation().distanceSquaredTo(targetLoc) > sqDistanceToTargetOnWallTouch) {
                boolean newLocIsWallAdjacent = false;
// Check direction4[0]
if (rc.canSenseLocation(newLoc.add(directions4[0]))
    && rc.senseMapInfo(newLoc.add(directions4[0])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[1] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[1]))
    && rc.senseMapInfo(newLoc.add(directions4[1])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[2] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[2]))
    && rc.senseMapInfo(newLoc.add(directions4[2])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[3] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[3]))
    && rc.senseMapInfo(newLoc.add(directions4[3])).isWall()) {
    newLocIsWallAdjacent = true;
}
                if (newLocIsWallAdjacent)
                    directionCost[i] -= 10000;
                // add a cost if new location is the previous one
                MapLocation lastLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];
                if (newLoc.equals(lastLoc)) {
                    directionCost[i] += 5000;
                }
            } else {
                // sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(targetLoc);
            }

            // add cost for moving in a direction that gets us gets us closer to a clump
            int maskx = dir.dx + 2;
            int masky = dir.dy + 2;
            int allyRobotsInNewLoc = 0;
if (nearbyAlliesMask[maskx + dx8[7]][masky + dy8[7]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[6]][masky + dy8[6]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[5]][masky + dy8[5]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[4]][masky + dy8[4]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[3]][masky + dy8[3]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[2]][masky + dy8[2]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[1]][masky + dy8[1]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[0]][masky + dy8[0]]) {
    allyRobotsInNewLoc++;
}
            directionCost[i] += allyRobotsInNewLoc * 1000;
        }
        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null)
            rc.move(minDir);

    }



    public static void splasherMove(MapLocation targetLoc) throws GameActionException {
        int[] directionCost = new int[8];

        int INF = (int) 2e9;
        for (int i = 8; i-- > 0;) {
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
                    directionCost[i] += 500;
                }
            }

            // add a cost for moving in a direction that gets closer to the tower that
            // spawned us
            if (dir == rc.getLocation().directionTo(spawnTowerLocation))
                directionCost[i] += 200;

            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);

            // add a cost if the tile is enemy paint
            if (!tileInfo.getPaint().isEnemy()) {
                MapInfo nextnext = null;
                if (rc.canSenseLocation(newLoc.add(dir)))
                    nextnext = rc.senseMapInfo(newLoc.add(dir));
                if (nextnext != null && nextnext.isPassable() && !nextnext.getPaint().isEnemy()) { // don't add the big
                                                                                                   // cost if it's only
                                                                                                   // 1 layer

                    directionCost[i] += 100;
                } else {
                    directionCost[i] += 3000;
                }
                if (!rc.isActionReady()) {
                    directionCost[i] += 3000;
                }
                if (rc.getPaint() < 100) {
                    directionCost[i] += 1000;
                }
            }
            // add a cost if the tile is neutral paint
            else if (tileInfo.getPaint() == PaintType.EMPTY) {
                if (rc.getNumberTowers() >= startPaintingFloorTowerNum) {
                    directionCost[i] += 2000;
                } else {
                    directionCost[i] += 300;
                }
                if (!rc.isActionReady()) {  // increase penalty if we're not ready to make an action
                    directionCost[i] += 3000;
                }
                if (rc.getPaint() < 100) {
                    directionCost[i] += 1000;  // increase the penalty if we are low paint
                }
            }
            // add a cost for moving in range of an enemy tower
            if (nearestEnemyTower != null && newLoc.isWithinDistanceSquared(nearestEnemyTower,
                    nearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {  // account for defense tower range
                directionCost[i] += 300000;
            }
            if (sndNearestEnemyTower != null && newLoc.isWithinDistanceSquared(sndNearestEnemyTower,
                    sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                directionCost[i] += 300000;
            }

            if (nearestEnemyTower != null && nearestEnemyTowerType != UnitType.LEVEL_ONE_DEFENSE_TOWER) {
                if (rc.getActionCooldownTurns() < 20)
                if (newLoc.isWithinDistanceSquared(nearestEnemyTower, 16)) {
                    rc.setIndicatorLine(newLoc, nearestEnemyTower, 255, 0, 255);
                    directionCost[i] -= 6000;
                }
            }

            // bug nav?
            if (wallAdjacent && rc.getLocation().distanceSquaredTo(targetLoc) > sqDistanceToTargetOnWallTouch) {
                boolean newLocIsWallAdjacent = false;
// Check direction4[0]
if (rc.canSenseLocation(newLoc.add(directions4[0]))
    && rc.senseMapInfo(newLoc.add(directions4[0])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[1] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[1]))
    && rc.senseMapInfo(newLoc.add(directions4[1])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[2] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[2]))
    && rc.senseMapInfo(newLoc.add(directions4[2])).isWall()) {
    newLocIsWallAdjacent = true;
}
// Check direction4[3] only if not already wall-adjacent
if (!newLocIsWallAdjacent
    && rc.canSenseLocation(newLoc.add(directions4[3]))
    && rc.senseMapInfo(newLoc.add(directions4[3])).isWall()) {
    newLocIsWallAdjacent = true;
}
                if (newLocIsWallAdjacent)
                    directionCost[i] -= 10000;
            } else {
                // sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(targetLoc);
            }


            // add cost for moving in a direction that gets us further away from target
            directionCost[i] += Utils.manhattanDistance(newLoc, targetLoc) * 500;

            // add cost for moving in a direction that gets us further away from enemyPaint,
            if (rc.getRoundNum() < fullFillPhase)
            if (nearestEnemyPaint != null) {
                directionCost[i] += Math.max(1, Utils.manhattanDistance(newLoc, nearestEnemyPaint)) * 700;
            }

            // try to attack the target our towers tell us to
            // if (rc.getRoundNum() >= fullAttackBasePhase && role == 1) {
            if (rc.getID() % 10 < 7) {
                MapLocation tentativeTarget = Utils.chooseTowerTarget();
                if (tentativeTarget != null)
                    directionCost[i] += Utils.manhattanDistance(newLoc, tentativeTarget) * 1500;
            }
            // }

            // good anti clumping
            int maskx = dir.dx + 2;
            int masky = dir.dy + 2;
            int allyRobotsInNewLoc = 0;
if (nearbyAlliesMask[maskx + dx8[7]][masky + dy8[7]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[6]][masky + dy8[6]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[5]][masky + dy8[5]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[4]][masky + dy8[4]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[3]][masky + dy8[3]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[2]][masky + dy8[2]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[1]][masky + dy8[1]]) {
    allyRobotsInNewLoc++;
}
if (nearbyAlliesMask[maskx + dx8[0]][masky + dy8[0]]) {
    allyRobotsInNewLoc++;
}
            directionCost[i] += allyRobotsInNewLoc * 300;

        }

        // find the minimum cost Direction and move there
        int minCost = INF;
        Direction minDir = null;
// Check d = 7
if (directionCost[7] < minCost) {
    minCost = directionCost[7];
    minDir = directions[7];
}
// Check d = 6
if (directionCost[6] < minCost) {
    minCost = directionCost[6];
    minDir = directions[6];
}
// Check d = 5
if (directionCost[5] < minCost) {
    minCost = directionCost[5];
    minDir = directions[5];
}
// Check d = 4
if (directionCost[4] < minCost) {
    minCost = directionCost[4];
    minDir = directions[4];
}
// Check d = 3
if (directionCost[3] < minCost) {
    minCost = directionCost[3];
    minDir = directions[3];
}
// Check d = 2
if (directionCost[2] < minCost) {
    minCost = directionCost[2];
    minDir = directions[2];
}
// Check d = 1
if (directionCost[1] < minCost) {
    minCost = directionCost[1];
    minDir = directions[1];
}
// Check d = 0
if (directionCost[0] < minCost) {
    minCost = directionCost[0];
    minDir = directions[0];
}
        if (minDir != null)
            rc.move(minDir);

    }
}
