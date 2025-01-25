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

    static int numWrongTilesInRuin;
    static int numWrongTilesInSRP;

    static int noSRPuntil = 3;  // no SRPs until x towers have been built
    static int noFullFillUntil = 4;

    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode


    // experimental
    // static int stopQuadrantModifierPhase = mx * 2;  // pending deletion?
    /* */

    static MapLocation lastRuinLocWithEnemyPaint;
    static int lastRuinLocWithEnemyPaintCounter = 0;

    static MapLocation lastCompletedTower;
    static boolean inStasis = false;  // wait till able to withdraw paint - isActionReady(), after completing a ruin

    static MapLocation alreadyTryingBuild;
    static int alreadyTryingBuildCounter = 0;

    static int strictFollowBuildOrderNumTowers = 3;  // strictly follow build order if we have not exceeded this number of towers
    static boolean bypassIfPaint = false;  // however bypass strict build order if it is paint

    public static void run() throws GameActionException {
        // Init target according to spawn direction



        if (lastRuinLocWithEnemyPaintCounter++ > 10) {  // reset ruin avoidance after some time has passed
            lastRuinLocWithEnemyPaint = null;
        }
        // if (alreadyTryingBuild == lastAlreadyTryingBuild) {
            if (alreadyTryingBuildCounter++ > 20) {
                // alreadyTryingBuildCounter = 0;
                alreadyTryingBuild = null;
            }
        // }
        // lastAlreadyTryingBuild = alreadyTryingBuild;


        if (inStasis) {
            if (!rc.isActionReady())
                return;
            inStasis = false;

            int transferAmt = Math.min(rc.senseRobotAtLocation(lastCompletedTower).getPaintAmount(),
                    rc.getType().paintCapacity - rc.getPaint());
            rc.transferPaint(lastCompletedTower, -transferAmt);  // should almost always be possible - only fails if tower is destroyed 1 turn after building
        }

        // ImpureUtils.updateNearbyUnits();  // pending removal

        ImpureUtils.updateNearbyMask(false);

        // if (Utils.selfDestructRequirementsMet()) {
        //     System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum() + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
        //     rc.disintegrate();
        // }

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

        ImpureUtils.tryUpgradeNearbyTowers();
        // nearbyRuins = rc.senseNearbyRuins(-1);  // keep updated for SRP as well
        if (!isRefilling && rc.getNumberTowers() != GameConstants.MAX_NUMBER_OF_TOWERS) {
            int distance = (int)2e9;
            for (MapLocation tileLoc : nearbyRuins) {
                // if (tile.hasRuin()) {
                    if (!rc.canSenseRobotAtLocation(tileLoc)) {
                        if (!tileLoc.equals(lastRuinLocWithEnemyPaint) &&
                            !tileLoc.equals(alreadyTryingBuild) &&
                            tileLoc.distanceSquaredTo(rc.getLocation()) < distance) {
                            distance = tileLoc.distanceSquaredTo(rc.getLocation());
                            curRuin = rc.senseMapInfo(tileLoc);
                            isFillingRuin = true;
                        }
                    }
                // }
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
                    MapLocation tileLoc = tile.getMapLocation();
                    if (avoidSRPloc != null && tileLoc.equals(avoidSRPloc))
                       continue;
                    if (alreadyTryingBuild != null && Utils.chessDistance(tileLoc, alreadyTryingBuild) <= 3)
                        continue;
                    if (tileLoc.distanceSquaredTo(rc.getLocation()) < distance) {
                        MapLocation nearestWrongOnIt = FillSRP.pureNearestWrongInSRP(tileLoc);
                        if (nearestWrongOnIt != null) {
                            boolean canDo = true;
                            // for (MapLocation tL : nearbyRuins) {
                            //     if (!rc.canSenseRobotAtLocation(tL) && tL.distanceSquaredTo(nearestWrongOnIt) <= 8) {
                            //         canDo = false;
                            //     }
                            // }
                            if (canDo) {
                                distance = tileLoc.distanceSquaredTo(rc.getLocation());
                                curSRP = tileLoc;
                                isFillingSRP = true;
                            }
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

        // Ruin
        if (isFillingRuin) {  // prefer checking isFillingRuin over curRuin == null

            rc.setIndicatorDot(curRuin.getMapLocation(), 255, 0, 0);
            UnitType buildTowerType = Utils.getBuildType();

            boolean noEnemyPaint = buildTowerType != null;
            if (noEnemyPaint) {
                FillRuin.updateNearestWrongInRuin(buildTowerType);
                HeuristicPath.moveToWrongInRuin();
                FillRuin.updateNearestWrongInRuin(buildTowerType);

                if (rc.canCompleteTowerPattern(buildTowerType, curRuin.getMapLocation())) {
                    rc.completeTowerPattern(buildTowerType, curRuin.getMapLocation());
                }

                if (nearestWrongInRuin != null) rc.setIndicatorDot(nearestWrongInRuin, 255, 255, 0);

                FillRuin.tryToPaintRuin(buildTowerType, true);

                if (rc.getPaint() < 5 * numWrongTilesInRuin) {  // we cannot finish the ruin and must refill
                    // System.out.println("early refill; can't complete ruin");
                    isFillingRuin = false;
                    isRefilling = true;
                }
                return;
            } else {
                lastRuinLocWithEnemyPaint = curRuin.getMapLocation();
                lastRuinLocWithEnemyPaintCounter = 0;
                isFillingRuin = false;
                nearestWrongInRuin = null;
            }
        }

        // SRP
        else if (isFillingSRP) {
            rc.setIndicatorDot(curSRP, 255, 0, 0);
            boolean noEnemyPaint = FillSRP.updateNearestWrongInSRP();
            if (noEnemyPaint /*&& getNearestWrongInSrp != null*/) {
                HeuristicPath.moveToWrongInSRP();
                FillSRP.updateNearestWrongInSRP();

                if (nearestWrongInSRP != null)
                    rc.setIndicatorDot(nearestWrongInSRP, 255, 255, 0);

                FillSRP.tryToPaintSRP();

                if (rc.getPaint() < 5 * numWrongTilesInSRP) {  // cannot finish the SRP and must refill
                    // System.out.println("early refill; can't complete SRP");
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

        // dot nearby empty/ enemy ruins
        nearbyRuins = rc.senseNearbyRuins(-1);
        if (rc.isActionReady()) {
            MapLocation closestDot = null;
            for (MapLocation tileLoc : nearbyRuins) {
                MapLocation tentativeDot = Utils.nearestEmptyOnRuinIfEnemyOrIsUndotted(tileLoc);
                if (tentativeDot != null) {
                    if (closestDot == null || rc.getLocation().distanceSquaredTo(tentativeDot) < rc.getLocation().distanceSquaredTo(closestDot)) {
                        closestDot = tentativeDot;
                    }
                }
            }
            if (rc.canAttack(closestDot)) {
                rc.attack(closestDot);
            }
        }

        if (!rc.isMovementReady()) {
            nearbyTiles = rc.senseNearbyMapInfos();
            // ImpureUtils.updateNearestPaintTower();  // already updated at the start
        }
        ImpureUtils.tryMarkSRP();


        isRefilling = rc.getPaint() < 100;
        MapLocation paintTarget = nearestPaintTower;
        if (paintTarget != null) {
            ImpureUtils.withdrawPaintIfPossible(paintTarget);
        }
        if (isRefilling && paintTarget != null) {
            target = paintTarget;
            sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
        }
        if (paintTarget != null && Utils.manhattanDistance(rc.getLocation(), paintTarget) > refillDistLimit && rc.getRoundNum() > attackBasePhase) {
            isRefilling = false;  // does not actually stop the refill because the move step already happened, but unlocks other options
        }


        wallAdjacent = false;
        for (MapInfo tile : rc.senseNearbyMapInfos(1)) {
            if (tile.isWall()) {
                wallAdjacent = true;
                break;
            }
        }
        if (wallAdjacent) {
            if (wallRounds++ == 0 && target != null) {
                sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
            }
        } else {
            wallRounds = 0;
            if (target != null)
                sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
        }

        // if (target != null)
        //     sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);

        if (isRefilling && target != null) {
            // target = paintTarget;

            // wallAdjacent = false;
            // for (MapInfo tile : rc.senseNearbyMapInfos(1)) {
            //     if (tile.isWall()) {
            //         wallAdjacent = true;
            //         break;
            //     }
            // }
            // if (wallAdjacent) {
            //     if (wallRounds++ == 0 && target != null) {
            //         sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
            //     }
            // } else {
            //     wallRounds = 0;
            //     // sqDistanceToTargetOnWallTouch = (int) 2e9;
            // }

            HeuristicPath.fullFill = false;

            // two options for paint refill :
            // 1. is probably more efficient because it avoids non allied paint but is greedy so not guaranteed to make it
            // 2. is guaranteed to make it but could take longer and make it die of paint loss also does not taking into clumping penalties

            // if (rc.getLocation().isWithinDistanceSquared(target, 18)) {
            //     HeuristicPath.refill(target);  // 1.
            // } else {
                // Pathfinder.move(target);  // 2.
                Pathfinder.move(paintTarget);  // 2.

            // }

            rc.setIndicatorLine(rc.getLocation(), target, 131, 252, 131);
        } /*else {
            // wallAdjacent = false;
            // for (MapInfo tile : rc.senseNearbyMapInfos(1)) {
            //     if (tile.isWall()) {
            //         wallAdjacent = true;
            //         break;
            //     }
            // }
            // if (wallAdjacent) {
            //     if (wallRounds++ == 0 && target != null) {
            //         sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
            //     }
            // } else {
            //     wallRounds = 0;
            //     // sqDistanceToTargetOnWallTouch = (int) 2e9;
            // }
        }*/

        // if (lastSRPloc != null && rc.getRoundNum() - lastSRProundNum < 25) {
        //     HeuristicPath.circleSRP();
        // }
        // ImpureUtils.tryMarkSRP();


        if (fstTowerTarget != null) {
            MapLocation tileLoc = fstTowerTarget;
            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 20)) {
                visFstTowerTarget = true;
            }
        }
        if (sndTowerTarget != null) {
            MapLocation tileLoc = sndTowerTarget;
            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 20)) {
                visSndTowerTarget = true;
            }
        }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            // selecting a random target location on the map has an inherent bias towards the center if e.g. we are in a corner
            // this is more of a problem on big maps
            // try to combat this but also instead sometimes selecting a location in our current quadrant
            /*if (rc.getRoundNum() % 2 == 0 && rc.getRoundNum() < stopQuadrantModifierPhase)
                target = Utils.randomLocationInQuadrant(Utils.currentQuadrant());
            else*/


            target = Utils.randomLocationInQuadrant(rng.nextInt(4));

            lastTargetChangeRound = rc.getRoundNum();
        }


        boolean fullFilling = rc.getRoundNum() >= fullFillPhase && rc.getNumberTowers() >= noFullFillUntil;

        if (fullFilling) {
            ImpureUtils.updateNearestEmptyTile();
        }

        // rc.setIndicatorDot(target, 200, 200, 200);
        if (rc.isMovementReady()) {

            MapLocation tt = Utils.chooseTowerTarget();
            if (tt != null && rc.getNumberTowers() > 8 && rc.isActionReady() && rc.getID() % 10 < 5) {
                Pathfinder.move(tt);
            } else {
                HeuristicPath.fullFill = fullFilling;
                HeuristicPath.targetIncentive = 500;
                HeuristicPath.move(target);
            }

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

        // if (rc.getRoundNum() >= fullAttackBasePhase) {
        //     AttackBase.init();
            // role = 1;
        // }

    }
}
