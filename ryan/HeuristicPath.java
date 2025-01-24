package ryan;

import battlecode.common.*;

public class HeuristicPath extends RobotPlayer {

    static final int dx8[] = {0, 1, 1, 1, 0, -1, -1, -1};
    static final int dy8[] = {-1, -1, 0, 1, 1, 1, 0, -1};

    public static void towerMicro(boolean retreat) throws GameActionException {
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

            // Add a cost if the tile is not ally paint
            if (!tileInfo.getPaint().isAlly()) {
                directionCost[i] += 10_000;
            }
            RobotInfo nearestEnemyTowerInfo = rc.senseRobotAtLocation(nearestEnemyTower);
            UnitType nearestEnemyTowerType = nearestEnemyTowerInfo.getType();
            int towerRange = 9;
            if (nearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ||
                nearestEnemyTowerType == UnitType.LEVEL_TWO_DEFENSE_TOWER ||
                nearestEnemyTowerType == UnitType.LEVEL_THREE_DEFENSE_TOWER) {
                towerRange = 16;
            }
            if (newLoc.isWithinDistanceSquared(nearestEnemyTower, towerRange) && retreat) { 
                directionCost[i] += 100_000;
            } else if (!newLoc.isWithinDistanceSquared(nearestEnemyTower, 9) && !retreat) { // If we haven't attacked yet, we want to get in range to attack the tower
                directionCost[i] += 100_000;
            }
            if (sndNearestEnemyTower != null) {
                    int sndTowerRange = 9;
                if (sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ||
                    sndNearestEnemyTowerType == UnitType.LEVEL_TWO_DEFENSE_TOWER ||
                    sndNearestEnemyTowerType == UnitType.LEVEL_THREE_DEFENSE_TOWER) {
                    towerRange = 16;
                }
                if (newLoc.isWithinDistanceSquared(sndNearestEnemyTower, sndTowerRange)){
                    directionCost[i] += 100_000;
                }
            }
            // add cost for moving in a direction that gets us gets us closer to a clump
            ImpureUtils.updateNearbyMask(false);
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
}
