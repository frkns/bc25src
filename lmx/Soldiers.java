package lmx;

import battlecode.common.*;

public class Soldiers extends RobotPlayer {

    static MapLocation target;

    // how long of not being able to reach target till we change it?
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

    static int noSRPuntil = 5;  // no SRPs until x towers have been built
    static int noFullFillUntil = 5;

    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode

    public static void run() throws GameActionException {
        Debug.print(1, "Init");
        ImpureUtils.updateNearbyUnits();  // pending removal
        ImpureUtils.updateNearbyMask();
        ImpureUtils.updateNearestEnemyTower();

        if (Utils.selfDestructRequirementsMet()) {
            System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum() + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
            rc.disintegrate();
        }

        if (nearestEnemyTower != null && rc.getRoundNum() > siegePhase) {
            Debug.print(1, "Micro for attacking tower");
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
            if (rc.isMovementReady())
                HeuristicPath.move(nearestEnemyTower, Behavior.TOWER_MICRO);
            inTowerRange = false;
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
        }

        // Check if tower has been built in ruins, or reach max tower
        if (isFillingRuin && rc.canSenseRobotAtLocation(curRuin.getMapLocation())
        ||  rc.getNumberTowers() == GameConstants.MAX_NUMBER_OF_TOWERS
        ) {
            isFillingRuin = false;
            curRuin = null;
        }

        if (!isRefilling && rc.getNumberTowers() != GameConstants.MAX_NUMBER_OF_TOWERS) {
            int distance = (int)2e9;
            for (MapInfo tile : nearbyTiles) {
                if (tile.hasRuin()) {
                    if (!rc.canSenseRobotAtLocation(tile.getMapLocation())) {
                        if (tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance) {
                            distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                            curRuin = tile;
                            isFillingRuin = true;
                            Debug.print(1, "Ruins have been found at : " + curRuin);
                        }
                    }
                }
            }
        }

        if (!isRefilling && !isFillingRuin && rc.getNumberTowers() >= noSRPuntil) {
            Debug.print(1, "Check for SRP");
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


        if (isFillingRuin) {  // prefer checking isFillingRuin over curRuin == null
            Debug.print(1, "Filling ruin");

            UnitType buildTowerType = AuxConstants.buildOrder[rc.getNumberTowers()];
            rc.setIndicatorDot(curRuin.getMapLocation(), 255, 0, 0);
            boolean noEnemyPaint = FillRuin.updateNearestWrongInRuin(buildTowerType);
            if (noEnemyPaint) {
                HeuristicPath.move(nearestWrongInRuin, Behavior.WRONG_RUINS);
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
            Debug.print(1, "Filling SRP");
            rc.setIndicatorDot(curSRP, 255, 0, 0);
            boolean noEnemyPaint = FillSRP.updateNearestWrongInSRP();
            if (noEnemyPaint) {
                HeuristicPath.move(nearestWrongInSRP, Behavior.WRONG_SRP);
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

        assert(!(isFillingRuin && isFillingSRP));

        if (!rc.isMovementReady()) {
            Debug.print(1, "Movement ready");
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

        // if (lastSRPloc != null && rc.getRoundNum() - lastSRProundNum < 25) {
        //     HeuristicPath.circleSRP();
        // }
        // ImpureUtils.tryMarkSRP();


        // dot nearby empty/ enemy ruins
        if (rc.isActionReady()) {
            MapLocation closestRuinToDot = null;

            int distance = (int)2e9;
            for (MapInfo tile : nearbyTiles) {
                if (tile.hasRuin()) {
                    if (tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance) {
                        distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                        closestRuinToDot = tile.getMapLocation();
                    }
                }
            }
            if (closestRuinToDot != null) {
                MapLocation locToDot = Utils.nearestEmptyOnRuinIfEnemyOrIsUndotted(closestRuinToDot);
                if (locToDot != null && rc.canAttack(locToDot)) {
                    // System.out.println("dotted a ruin");
                    rc.attack(locToDot);
                }
            }
        }


        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {

            // selecting a random target location on the map has an inherent bias towards the center if e.g. we are in a corner
            // this is more of a problem on big maps
            // try to combat this but also instead sometimes selecting a location in our current quadrant
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

        // rc.setIndicatorDot(target, 200, 200, 200);
        if (rc.isMovementReady()) {
            HeuristicPath.fullFill = fullFilling;
            HeuristicPath.targetIncentive = 500;
            HeuristicPath.move(target, Behavior.SOLDIER);
            // nearbyTiles = rc.senseNearbyMapInfos();
        }

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

        // if (isFillingSRP == wasFillingSRPlastRound) {
        //     consecutiveRoundsFillingSRP++;
        // }
        // wasFillingSRPlastRound = isFillingSRP;
    }

}
