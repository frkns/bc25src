package newtst1;

import battlecode.common.*;

public class Soldiers extends RobotPlayer {

    static MapLocation target;

    // how long of not being able to reach target till we change it?
    static int targetChangeWaitTime = Math.max(mapWidth, mapHeight);

    static int lastTargetChangeRound = 9999;
    static boolean wasFillingSRPlastRound = false;

    static int consecutiveRoundsFillingSRP = 0;

    static MapLocation avoidSRPloc;

    static MapLocation lastSRPloc;
    static int lastSRProundNum = 0;
    static boolean inTowerRange = false;

    static int fullFillPhase = Math.max(mapWidth, mapHeight) * 2;

    public static void run() throws GameActionException {

        if (isFillingRuin && rc.canSenseRobotAtLocation(curRuin.getMapLocation())) {
            isFillingRuin = false;
            curRuin = null;
        }
        // Ruin
        UnitType buildTowerType = AuxConstants.buildOrder[rc.getNumberTowers()];

        if (!isRefilling && rc.getNumberTowers() != GameConstants.MAX_NUMBER_OF_TOWERS) {
            int distance = (int)2e9;
            for (MapInfo tile : nearbyTiles) {
                if (tile.hasRuin()) {
                    if (!rc.canSenseRobotAtLocation(tile.getMapLocation())) {
                        if (tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance) {
                            distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                            curRuin = tile;
                            isFillingRuin = true;
                        }
                    }
                }
            }
        }
        if (rc.getNumberTowers() == GameConstants.MAX_NUMBER_OF_TOWERS) {
            isFillingRuin = false;
        }
        if (!isRefilling && !isFillingRuin) {
            int distance = (int)2e9;
            for (MapInfo tile : nearbyTiles) {
                if (tile.getMark() == PaintType.ALLY_PRIMARY) {
                    if (tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance) {
                        if (avoidSRPloc != null && tile.getMapLocation().equals(avoidSRPloc))
                            continue;
                        MapLocation nearestWrongOnIt = FillSRP.pureNearestWrongInSRP(tile.getMapLocation());
                        if (nearestWrongOnIt != null) {
                            distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                            curSRP = tile.getMapLocation();
                            isFillingSRP = true;
                        }
                    }
                }
            }
        }

        if (consecutiveRoundsFillingSRP > 1) {
            consecutiveRoundsFillingSRP = 0;
            avoidSRPloc = curSRP;
            isFillingSRP = false;
            curSRP = null;
        }

        if (isFillingRuin) {  // prefer checking isFillingRuin over curRuin == null
            rc.setIndicatorDot(curRuin.getMapLocation(), 255, 0, 0);
            boolean noEnemyPaint = FillRuin.updateNearestWrongInRuin(buildTowerType);
            if (noEnemyPaint) {
                HeuristicPath.moveToWrongInRuin();
                FillRuin.updateNearestWrongInRuin(buildTowerType);

                if (rc.canCompleteTowerPattern(buildTowerType, curRuin.getMapLocation())) {
                    rc.completeTowerPattern(buildTowerType, curRuin.getMapLocation());
                }

                if (nearestWrongInRuin != null) rc.setIndicatorDot(nearestWrongInRuin, 255, 255, 0);

                FillRuin.tryToPaintRuin(buildTowerType);

                if (rc.getPaint() < 30) {  // we cannot finish the ruin and must refill
                    isFillingRuin = false;
                    isRefilling = true;
                }
                return;
            } else {
                isFillingRuin = false;
                nearestWrongInRuin = null;
            }
        }
        // SRP
        else if (isFillingSRP) {
            rc.setIndicatorDot(curSRP, 255, 0, 0);
            boolean noEnemyPaint = FillSRP.updateNearestWrongInSRP();
            if (noEnemyPaint) {
                HeuristicPath.moveToWrongInSRP();
                FillSRP.updateNearestWrongInSRP();

                if (nearestWrongInSRP != null) rc.setIndicatorDot(nearestWrongInSRP, 255, 255, 0);

                FillSRP.tryToPaintSRP();

                if (rc.getPaint() < 5) {
                    isFillingRuin = false;
                    isRefilling = true;
                }
                return;
            } else {
                isFillingSRP = false;
                nearestWrongInSRP = null;
            }
        }

        assert(!(isFillingRuin && isFillingSRP));

        if (!rc.isMovementReady()) {
            nearbyTiles = rc.senseNearbyMapInfos();
            ImpureUtils.updateNearestEnemyTower();
        }
        ImpureUtils.tryMarkSRP();

        boolean canRefill = true;
        MapLocation paintTarget = nearestPaintTower;
        if (paintTarget == null) {
            paintTarget = spawnTowerLocation;
            if (spawnTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
                canRefill = false;
                isRefilling = false;
            }
        }
        ImpureUtils.withdrawPaintIfPossible(paintTarget);

        if (rc.getPaint() < 100 && canRefill) {
            isRefilling = true;
        } else {
            isRefilling = false;
        }

        if (isRefilling) {
            HeuristicPath.targetIncentive = 1000;
            HeuristicPath.move(paintTarget);
            ImpureUtils.paintFloor();
            rc.setIndicatorLine(rc.getLocation(), paintTarget, 131, 252, 131);
        }

        // if (lastSRPloc != null && rc.getRoundNum() - lastSRProundNum < 25) {
        //     HeuristicPath.circleSRP();
        // }
        // ImpureUtils.tryMarkSRP();

        if (nearestEnemyTower != null && rc.getRoundNum() > siegePhase) {
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

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            if (target != null) rc.setIndicatorDot(target, 0, 0, 0);
            target = new MapLocation(rng.nextInt(mapWidth-1), rng.nextInt(mapHeight-1));
            lastTargetChangeRound = rc.getRoundNum();
        }
        rc.setIndicatorDot(target, 200, 200, 200);
        if (rc.isMovementReady()) {
            HeuristicPath.targetIncentive = 500;
            HeuristicPath.move(target);
            nearbyTiles = rc.senseNearbyMapInfos();
        }

        ImpureUtils.paintFloor();

        if (rc.isActionReady() && rc.getRoundNum() >= fullFillPhase) {
            for (MapInfo tile : nearbyTiles) {
                if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation());
                }
            }
        }

        if (isFillingSRP == wasFillingSRPlastRound) {
            consecutiveRoundsFillingSRP++;
        }
        wasFillingSRPlastRound = isFillingSRP;
    }

}
