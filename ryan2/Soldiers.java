package ryan2;

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
        ImpureUtils.updateNearbyUnits();
        ImpureUtils.updateNearbyMask();

        if (Utils.selfDestructRequirementsMet()) {
            System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum() + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
            rc.disintegrate();
        }

        handleSiegeTower();

        UnitType buildTowerType = AuxConstants.buildOrder[rc.getNumberTowers()];
        handleRuinFilling(buildTowerType);
        handleSRPFilling();

        assert(!(isFillingRuin && isFillingSRP));

        if (!rc.isMovementReady()) {
            nearbyTiles = rc.senseNearbyMapInfos();
            ImpureUtils.updateNearestEnemyTower();
        }
        ImpureUtils.tryMarkSRP();

        handlePaintRefill();
        handleExploration();
        handlePainting();
    }

    /**
     * Triggers during the siege phase
     * Attack nearest tower and move in and out to minimize damage
     */
    private static void handleSiegeTower() throws GameActionException {
        ImpureUtils.updateNearestEnemyTower();
        if (nearestEnemyTower != null && rc.getRoundNum() > siegePhase) {
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }

            if (rc.isMovementReady()) {
                HeuristicPath.towerMicro();
                inTowerRange = false;
            }
            
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
        }
    }

    /**
     * Finds nearest ruin
     * Build tower
     * Refill paint if not enough to complete pattern
     */
    private static void handleRuinFilling(UnitType buildTowerType) throws GameActionException {
        // If tower has been completed stop filling
        if (isFillingRuin && rc.canSenseRobotAtLocation(curRuin.getMapLocation())) {
            isFillingRuin = false;
            curRuin = null;
        }


        if (rc.getNumberTowers() == GameConstants.MAX_NUMBER_OF_TOWERS) {
            isFillingRuin = false;
        }

        if (!isRefilling && !isFillingRuin && rc.getNumberTowers() != GameConstants.MAX_NUMBER_OF_TOWERS) {
            findNearestRuin();
        }

        if (isFillingRuin) {
            fillCurrentRuin(buildTowerType);
        }
    }

    private static void findNearestRuin() throws GameActionException {
        int distance = (int)2e9;
        for (MapInfo tile : nearbyTiles) {
            if (tile.hasRuin() && !rc.canSenseRobotAtLocation(tile.getMapLocation())) {
                if (tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance) {
                    distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    curRuin = tile;
                    isFillingRuin = true;
                }
            }
        }
    }

    private static void fillCurrentRuin(UnitType buildTowerType) throws GameActionException {
        rc.setIndicatorDot(curRuin.getMapLocation(), 255, 0, 0);
        boolean noEnemyPaint = FillRuin.updateNearestWrongInRuin(buildTowerType);
        if (noEnemyPaint) {
            HeuristicPath.moveToWrongInRuin();
            FillRuin.updateNearestWrongInRuin(buildTowerType);

            if (rc.canCompleteTowerPattern(buildTowerType, curRuin.getMapLocation())) {
                rc.completeTowerPattern(buildTowerType, curRuin.getMapLocation());
            }

            if (nearestWrongInRuin != null) 
                rc.setIndicatorDot(nearestWrongInRuin, 255, 255, 0);

            FillRuin.tryToPaintRuin(buildTowerType);

            if (rc.getPaint() < 5 * numWrongTilesInRuin) {
                System.out.println("early refill; can't complete ruin");
                isFillingRuin = false;
                isRefilling = true;
            }
        } else {
            isFillingRuin = false;
            nearestWrongInRuin = null;
        }
    }

    /**
     * Build SRP if enough towers have been built
     */
    private static void handleSRPFilling() throws GameActionException {
        if (!isRefilling && !isFillingRuin && rc.getNumberTowers() >= noSRPuntil) {
            findNearestSRP();
        }

        if (isFillingSRP) {
            rc.setIndicatorDot(curSRP, 255, 0, 0);
            boolean noEnemyPaint = FillSRP.updateNearestWrongInSRP();
            if (noEnemyPaint) {
                fillCurrentSRP();
            } else {
                isFillingSRP = false;
                nearestWrongInSRP = null;
            }
        }
    }

    private static void findNearestSRP() throws GameActionException {
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

    private static void fillCurrentSRP() throws GameActionException {
        HeuristicPath.moveToWrongInSRP();
        FillSRP.updateNearestWrongInSRP();

        if (nearestWrongInSRP != null) rc.setIndicatorDot(nearestWrongInSRP, 255, 255, 0);

        FillSRP.tryToPaintSRP();

        if (rc.getPaint() < 5 * numWrongTilesInSRP) {
            System.out.println("early refill; can't complete SRP");
            isFillingRuin = false;
            isFillingSRP = false;
            isRefilling = true;
        }
    }

    /**
     * Find the nearest paint tower and refill paint
     */
    private static void handlePaintRefill() throws GameActionException {
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
            Pathfinder.move(paintTarget);
            rc.setIndicatorLine(rc.getLocation(), paintTarget, 131, 252, 131);
        }
    }

    /**
     * Early game: Explore by quadrant
     * Late game: Explore randomly
     */
    private static void handleExploration() throws GameActionException {
        // Change target if we've reached it, or waited too long to reach it
        if (target == null
            || rc.getLocation().isWithinDistanceSquared(target, 9)
            || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {

            // Early game: explore current quadrant
            // Late game: explore randomly
            if (rc.getRoundNum() % 2 == 0 && rc.getRoundNum() < stopQuadrantModifierPhase)
            target = Utils.randomLocationInQuadrant(Utils.currentQuadrant());
            else
            target = new MapLocation(rng.nextInt(mapWidth), rng.nextInt(mapHeight));
            lastTargetChangeRound = rc.getRoundNum();
        }

        // Check if we should be in full filling mode based on game phase and tower count
        boolean fullFilling = rc.getRoundNum() >= fullFillPhase && rc.getNumberTowers() >= noFullFillUntil;

        // If in full filling mode, find nearest empty tile to paint
        if (fullFilling) {
            ImpureUtils.updateNearestEmptyTile();
        }

        // Move towards target using heuristic pathfinding
        if (rc.isMovementReady()) {
            HeuristicPath.fullFill = fullFilling;
            HeuristicPath.targetIncentive = 500; // Higher incentive to reach target
            HeuristicPath.move(target);
        }
    }

    /**
     * Paint floor tiles if not filling ruin or SRP
     */
    private static void handlePainting() throws GameActionException {
        if (rc.getNumberTowers() >= startPaintingFloorTowerNum && !isRefilling)
            ImpureUtils.paintFloor();

        boolean fullFilling = rc.getRoundNum() >= fullFillPhase && rc.getNumberTowers() >= noFullFillUntil;
        if (rc.isActionReady() && fullFilling && !isRefilling) {
            _attackableNearbyTiles = rc.senseNearbyMapInfos(9);
            for (MapInfo tile : _attackableNearbyTiles) {
                if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation());
                }
            }
        }
    }
}
