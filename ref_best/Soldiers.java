package ref_best;

import battlecode.common.*;

public class Soldiers extends RobotPlayer {
    //region Constants and Static Variables
    static MapLocation target;
    static int targetChangeWaitTime = mx;
    static int lastTargetChangeRound = 0;
    
    // not using these
    static boolean wasFillingSRPlastRound = false;
    static int consecutiveRoundsFillingSRP = 0;
    static MapLocation avoidSRPloc;
    static MapLocation lastSRPloc;
    static int lastSRProundNum = 0;
    /* */

    static int stopQuadrantModifierPhase = mx * 2;
    static int numWrongTilesInRuin;
    static int numWrongTilesInSRP;
    static int noSRPuntil = 5;
    static int noFullFillUntil = 5;
    static MapInfo[] _attackableNearbyTiles;
    //endregion

    public static void run() throws GameActionException {
        //region Initialization and Updates
        ImpureUtils.updateNearbyUnits();
        ImpureUtils.updateNearbyMask();

        if (Utils.selfDestructRequirementsMet()) {
            System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum() + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
            rc.disintegrate();
        }
        //endregion

        //region read Messages
        Communication.readMessages();  // Communication code saves information to relevant variables (e.g. ruin locs, enemy tower locs)
        //endregion

        //region Tower Attack Logic
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
        //endregion

        //region Ruin Filling Logic
        if (isFillingRuin && rc.canSenseRobotAtLocation(curRuin.getMapLocation())) {
            isFillingRuin = false;
            curRuin = null;
        }

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
        //endregion

        //region SRP Logic
        if (!isRefilling && !isFillingRuin && rc.getNumberTowers() >= noSRPuntil) {
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
                    System.out.println("early refill; can't complete ruin");
                    isFillingRuin = false;
                    isRefilling = true;
                }
                return;
            } else {
                isFillingRuin = false;
                nearestWrongInRuin = null;
            }
        }
        else if (isFillingSRP) {
            rc.setIndicatorDot(curSRP, 255, 0, 0);
            boolean noEnemyPaint = FillSRP.updateNearestWrongInSRP();
            if (noEnemyPaint) {
                HeuristicPath.moveToWrongInSRP();
                FillSRP.updateNearestWrongInSRP();

                if (nearestWrongInSRP != null) rc.setIndicatorDot(nearestWrongInSRP, 255, 255, 0);

                FillSRP.tryToPaintSRP();

                if (rc.getPaint() < 5 * numWrongTilesInSRP) {  // cannot finish the SRP and must refill
                    System.out.println("early refill; can't complete SRP");
                    isFillingRuin = false;
                    isFillingSRP = false;
                    isRefilling = true;
                }
                return;
            } else {
                isFillingSRP = false;
                nearestWrongInSRP = null;
            }
        }
        //endregion

        //region Paint Refill Logic
        assert(!(isFillingRuin && isFillingSRP));

        if (!rc.isMovementReady()) {
            nearbyTiles = rc.senseNearbyMapInfos();
            ImpureUtils.updateNearestEnemyTower();
        }
        ImpureUtils.tryMarkSRP();

        boolean canRefill = true;
        MapLocation paintTarget = nearestPaintSource;
        if (paintTarget == null) {
            paintTarget = spawnTowerLocation;
            if (spawnTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
                canRefill = false;
                isRefilling = false;
            }
        }
        ImpureUtils.withdrawPaintIfPossible(paintTarget);

        isRefilling = rc.getPaint() < 100 && canRefill;

        if (isRefilling) {
            HeuristicPath.fullFill = false;

            // two options for paint refill :
            // 1. is probably more efficient because it avoids non allied paint but is greedy so not guaranteed to make it
            // 2. is guaranteed to make it but could take longer and make it die of paint loss also does not taking into clumping penalties

            // 1.
            // HeuristicPath.refill(paintTarget);

            // 2.
            Pathfinder.move(paintTarget);

            rc.setIndicatorLine(rc.getLocation(), paintTarget, 131, 252, 131);
        }
        //endregion

        //region Movement and Targeting
        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {

            if (rc.getRoundNum() % 2 == 0 && rc.getRoundNum() < stopQuadrantModifierPhase)
                target = Utils.randomLocationInQuadrant(Utils.currentQuadrant());
            else
                target = new MapLocation(rng.nextInt(mapWidth), rng.nextInt(mapHeight));
            lastTargetChangeRound = rc.getRoundNum();
        }

        boolean fullFilling = rc.getRoundNum() >= fullFillPhase && rc.getNumberTowers() >= noFullFillUntil;

        if (fullFilling) {
            ImpureUtils.updateNearestEmptyTile();
        }

        if (rc.isMovementReady()) {
            HeuristicPath.fullFill = fullFilling;
            HeuristicPath.targetIncentive = 500;
            HeuristicPath.move(target);
        }
        //endregion

        //region Painting
        if (rc.getNumberTowers() >= startPaintingFloorTowerNum && !isRefilling)
            ImpureUtils.paintFloor();

        if (rc.isActionReady() && fullFilling && !isRefilling) {
            _attackableNearbyTiles = rc.senseNearbyMapInfos(9);
            for (MapInfo tile : _attackableNearbyTiles) {
                if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation());
                }
            }
        }
        //endregion

        //region Send Messages
        if (nearestEnemyTower != null) {
            // Report to all allied towers in range
            for (RobotInfo robot : nearbyRobots) {
                if (robot.getTeam() == rc.getTeam() && robot.getType().isTowerType()) {
                    Debug.println(Debug.COMMS, "Reporting to tower ID: " + robot.getID());
                    Communication.sendLocationMessage(robot.getID(), 0, nearestEnemyTower);
                }
            }
        }
        //endregion
    }
}
