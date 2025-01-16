package ref_best;

import battlecode.common.*;

public class Soldiers extends RobotPlayer {

    static MapLocation target;

    // how long of not being able to reach target till we change it?
    static int targetChangeWaitTime = Math.max(mapWidth, mapHeight);

    static int lastTargetChangeRound = 0;
    static boolean wasFillingSRPlastRound = false;

    static int consecutiveRoundsFillingSRP = 0;

    static MapLocation avoidSRPloc;

    static MapLocation lastSRPloc;
    static int lastSRProundNum = 0;

    static int numWrongTilesInRuin;
    static int numWrongTilesInSRP;

    static int noSRPuntil = 0;  // no SRPs until x towers have been built

    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode

    public static void run() throws GameActionException {

        ImpureUtils.updateNearbyUnits();

        if (Utils.selfDestructRequirementsMet()) {
            System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum() + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
            rc.disintegrate();
        }

        ImpureUtils.updateNearestEnemyTower();
        if (nearestEnemyTower != null && rc.getRoundNum() > siegePhase) {
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
            if (rc.isMovementReady())
                HeuristicPath.towerMicro();
            inTowerRange = false;
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
        }

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
        if (!isRefilling && !isFillingRuin && rc.getNumberTowers() >= noSRPuntil) {
            // SRP code; can disable

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

        // if (consecutiveRoundsFillingSRP > 1) {
        //     consecutiveRoundsFillingSRP = 0;
        //     avoidSRPloc = curSRP;
        //     isFillingSRP = false;
        //     curSRP = null;
        // }

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

                if (rc.getPaint() < 5 * numWrongTilesInRuin) {  // we cannot finish the ruin and must refill
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

                if (rc.getPaint() < 5 * numWrongTilesInSRP) {
                    isFillingSRP = false;
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
            HeuristicPath.fullFill = false;

            // HeuristicPath.targetIncentive = 1000;
            // HeuristicPath.move(paintTarget);

            Pathfinder.move(paintTarget);

            rc.setIndicatorLine(rc.getLocation(), paintTarget, 131, 252, 131);
        }

        // if (lastSRPloc != null && rc.getRoundNum() - lastSRProundNum < 25) {
        //     HeuristicPath.circleSRP();
        // }
        // ImpureUtils.tryMarkSRP();


        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            if (target != null) rc.setIndicatorDot(target, 0, 0, 0);
            target = new MapLocation(rng.nextInt(mapWidth-1), rng.nextInt(mapHeight-1));
            lastTargetChangeRound = rc.getRoundNum();
        }

        boolean fullFilling = rc.getRoundNum() >= fullFillPhase;

        if (fullFilling) {
            ImpureUtils.updateNearestEmptyTile();
        }

        rc.setIndicatorDot(target, 200, 200, 200);
        if (rc.isMovementReady()) {
            HeuristicPath.fullFill = fullFilling;
            HeuristicPath.targetIncentive = 500;
            HeuristicPath.move(target);
            nearbyTiles = rc.senseNearbyMapInfos();
        }

        if (rc.getNumberTowers() >= startPaintingFloorTowerNum)
            ImpureUtils.paintFloor();

        if (rc.isActionReady() && fullFilling) {
            _attackableNearbyTiles = rc.senseNearbyMapInfos(9);
            for (MapInfo tile : _attackableNearbyTiles) {
                if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation());
                }
            }
        }

        // if (isFillingSRP == wasFillingSRPlastRound) {
        //     consecutiveRoundsFillingSRP++;
        // }
        // wasFillingSRPlastRound = isFillingSRP;
    }

}
