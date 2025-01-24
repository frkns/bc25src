//package ryan;
//
//import battlecode.common.*;
//
//public class Soldiersc extends RobotPlayer {
//    //#region Fields
//    static MapLocation target;
//    static int targetChangeWaitTime = mx;
//    static int lastTargetChangeRound = 0;
//    static boolean wasFillingSRPlastRound = false;
//    static int consecutiveRoundsFillingSRP = 0;
//    static MapLocation avoidSRPloc;
//    static MapLocation lastSRPloc;
//    static int lastSRProundNum = 0;
//    static int numWrongTilesInRuin;
//    static int numWrongTilesInSRP;
//    static int noSRPuntil = 3;
//    static int noFullFillUntil = 4;
//    static MapInfo[] _attackableNearbyTiles;
//    static MapLocation lastRuinLocWithEnemyPaint;
//    static int lastRuinLocWithEnemyPaintCounter = 0;
//    static MapLocation lastCompletedTower;
//    static boolean inStasis = false;
//    static MapLocation alreadyTryingBuild;
//    static int alreadyTryingBuildCounter = 0;
//    static int strictFollowBuildOrderNumTowers = 3;
//    static boolean noPaintTowers = false;
//    //#endregion
//
//    public static void run() throws GameActionException {
//        //#region Initialization
//        MapRecorder.initTurn();
//        if (lastRuinLocWithEnemyPaintCounter++ > 10) {
//            lastRuinLocWithEnemyPaint = null;
//        }
//        if (alreadyTryingBuildCounter++ > 20) {
//            alreadyTryingBuild = null;
//        }
//        //#endregion
//
//        //#region Stasis Management
//        if (inStasis) {
//            if (!rc.isActionReady())
//                return;
//            inStasis = false;
//            int transferAmt = Math.min(rc.senseRobotAtLocation(lastCompletedTower).getPaintAmount(),
//                    rc.getType().paintCapacity - rc.getPaint());
//            rc.transferPaint(lastCompletedTower, -transferAmt);
//        }
//        //#endregion
//
//        //#region Tower Interactions
//        ImpureUtils.tryUpgradeNearbyTowers();
//        ImpureUtils.updateNearbyMask(false);
//        ImpureUtils.updateNearestEnemyTower();
//        if (nearestEnemyTower != null && rc.getRoundNum() > siegePhase) {
//            if (rc.canAttack(nearestEnemyTower)) {
//                rc.attack(nearestEnemyTower);
//            }
//            if (rc.isMovementReady() && rc.isActionReady()) {
//                Pathfinder.move(nearestEnemyTower);
//            } else if (rc.isMovementReady()) {
//                Pathfinder.move(rc.getLocation().add(rc.getLocation().directionTo(nearestEnemyTower).opposite()));
//            }
//            if (rc.canAttack(nearestEnemyTower)) {
//                rc.attack(nearestEnemyTower);
//            }
//        }
//        //#endregion
//
//        //#region Ruin Management
//        if (isFillingRuin && rc.canSenseRobotAtLocation(curRuin.getMapLocation())) {
//            isFillingRuin = false;
//            curRuin = null;
//        }
//        nearbyRuins = rc.senseNearbyRuins(-1);
//        if (!isRefilling && rc.getNumberTowers() != GameConstants.MAX_NUMBER_OF_TOWERS) {
//            int distance = (int)2e9;
//            for (MapLocation tileLoc : nearbyRuins) {
//                if (!rc.canSenseRobotAtLocation(tileLoc)) {
//                    if (!tileLoc.equals(lastRuinLocWithEnemyPaint) &&
//                        !tileLoc.equals(alreadyTryingBuild) &&
//                        tileLoc.distanceSquaredTo(rc.getLocation()) < distance) {
//                        distance = tileLoc.distanceSquaredTo(rc.getLocation());
//                        curRuin = rc.senseMapInfo(tileLoc);
//                        isFillingRuin = true;
//                    }
//                }
//            }
//        }
//        if (rc.getNumberTowers() == GameConstants.MAX_NUMBER_OF_TOWERS) {
//            isFillingRuin = false;
//        }
//        if (isFillingRuin) {
//            rc.setIndicatorDot(curRuin.getMapLocation(), 255, 0, 0);
//            UnitType buildTowerType = Utils.getBuildType();
//            boolean noEnemyPaint = buildTowerType != null;
//            if (noEnemyPaint) {
//                FillRuin.updateNearestWrongInRuin(buildTowerType);
//                if (nearestWrongInRuin != null) {
//                    Pathfinder.move(nearestWrongInRuin);
//                }
//                FillRuin.updateNearestWrongInRuin(buildTowerType);
//                if (rc.canCompleteTowerPattern(buildTowerType, curRuin.getMapLocation())) {
//                    rc.completeTowerPattern(buildTowerType, curRuin.getMapLocation());
//                }
//                if (nearestWrongInRuin != null) rc.setIndicatorDot(nearestWrongInRuin, 255, 255, 0);
//                FillRuin.tryToPaintRuin(buildTowerType, true);
//                if (rc.getPaint() < 5 * numWrongTilesInRuin) {
//                    isFillingRuin = false;
//                    isRefilling = true;
//                }
//                return;
//            } else {
//                lastRuinLocWithEnemyPaint = curRuin.getMapLocation();
//                lastRuinLocWithEnemyPaintCounter = 0;
//                isFillingRuin = false;
//                nearestWrongInRuin = null;
//            }
//        }
//        //#endregion
//
//        //#region SRP Management
//        if (!isRefilling && !isFillingRuin && rc.getNumberTowers() >= noSRPuntil) {
//            int distance = (int)2e9;
//            for (MapInfo tile : nearbyTiles) {
//                if (tile.getMark() == PaintType.ALLY_PRIMARY) {
//                    MapLocation tileLoc = tile.getMapLocation();
//                    if (avoidSRPloc != null && tileLoc.equals(avoidSRPloc))
//                       continue;
//                    if (alreadyTryingBuild != null && Utils.chessDistance(tileLoc, alreadyTryingBuild) <= 3)
//                        continue;
//                    if (tileLoc.distanceSquaredTo(rc.getLocation()) < distance) {
//                        MapLocation nearestWrongOnIt = FillSRP.pureNearestWrongInSRP(tileLoc);
//                        if (nearestWrongOnIt != null) {
//                            boolean canDo = true;
//                            if (canDo) {
//                                distance = tileLoc.distanceSquaredTo(rc.getLocation());
//                                curSRP = tileLoc;
//                                isFillingSRP = true;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (isFillingSRP) {
//            rc.setIndicatorDot(curSRP, 255, 0, 0);
//            boolean noEnemyPaint = FillSRP.updateNearestWrongInSRP();
//            if (noEnemyPaint) {
//                if (nearestWrongInSRP != null) {
//                    Pathfinder.move(nearestWrongInSRP);
//                }
//                FillSRP.updateNearestWrongInSRP();
//                if (nearestWrongInSRP != null)
//                    rc.setIndicatorDot(nearestWrongInSRP, 255, 255, 0);
//                FillSRP.tryToPaintSRP();
//                if (rc.getPaint() < 5 * numWrongTilesInSRP) {
//                    isFillingRuin = false;
//                    isFillingSRP = false;
//                    isRefilling = true;
//                }
//                return;
//            } else {
//                isFillingSRP = false;
//                nearestWrongInSRP = null;
//            }
//        }
//        //#endregion
//
//        //#region Refill
//        if (!rc.isMovementReady()) {
//            nearbyTiles = rc.senseNearbyMapInfos();
//        }
//        ImpureUtils.tryMarkSRP();
//        isRefilling = rc.getPaint() < 100;
//        MapLocation paintTarget = nearestPaintTower;
//        if (paintTarget != null) {
//            ImpureUtils.withdrawPaintIfPossible(paintTarget);
//        }
//        if (isRefilling && paintTarget != null) {
//            target = paintTarget;
//        }
//        if (paintTarget != null && Utils.manhattanDistance(rc.getLocation(), paintTarget) > refillDistLimit) {
//            isRefilling = false;
//        }
//        if (isRefilling && target != null) {
//            Pathfinder.move(paintTarget);
//            rc.setIndicatorLine(rc.getLocation(), target, 131, 252, 131);
//        }
//        //#endregion
//
//        //#region Dot ruins
//        nearbyRuins = rc.senseNearbyRuins(-1);
//        if (rc.isActionReady()) {
//            MapLocation closestDot = null;
//            for (MapLocation tileLoc : nearbyRuins) {
//                MapLocation tentativeDot = Utils.nearestEmptyOnRuinIfEnemyOrIsUndotted(tileLoc);
//                if (tentativeDot != null) {
//                    if (closestDot == null || rc.getLocation().distanceSquaredTo(tentativeDot) < rc.getLocation().distanceSquaredTo(closestDot)) {
//                        closestDot = tentativeDot;
//                    }
//                }
//            }
//            if (rc.canAttack(closestDot)) {
//                rc.attack(closestDot);
//            }
//        }
//        //#endregion
//
//        //#region Movement and Targeting
//        if (target == null
//                || rc.getLocation().isWithinDistanceSquared(target, 9)
//                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
//            target = Utils.randomLocationInQuadrant(rng.nextInt(4));
//            lastTargetChangeRound = rc.getRoundNum();
//        }
//        //#endregion
//
//        //#region Final Actions
//        boolean fullFilling = rc.getRoundNum() >= fullFillPhase && rc.getNumberTowers() >= noFullFillUntil;
//        if (fullFilling) {
//            ImpureUtils.updateNearestEmptyTile();
//        }
//        if (rc.isMovementReady()) {
//            HeuristicPath.fullFill = fullFilling;
//            HeuristicPath.targetIncentive = 500;
//            MapLocation tt = Utils.chooseTowerTarget();
//            if (tt != null && rc.getNumberTowers() > 8 && rc.isActionReady() && rc.getID() % 10 < 5) {
//                Pathfinder.move(tt);
//            } else {
//                Pathfinder.move(target);
//            }
//        }
//        if (rc.getNumberTowers() >= startPaintingFloorTowerNum && !isRefilling)
//            ImpureUtils.paintFloor();
//        if (rc.isActionReady() && fullFilling && !isRefilling) {
//            _attackableNearbyTiles = rc.senseNearbyMapInfos(9);
//            for (MapInfo tile : _attackableNearbyTiles) {
//                if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
//                    rc.attack(tile.getMapLocation());
//                }
//            }
//        }
//        MapRecorder.recordSym(1000);
//        //#endregion
//    }
//}
