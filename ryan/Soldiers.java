package ryan;


import ryan.fast.*;
import battlecode.common.*;

public class Soldiers extends RobotPlayer {
    // -------- Tower building variables -------------
    public static MapLocation[] nearbyRuins;
    public static FastLocSet ruinsWithEnemyPaint = new FastLocSet();
    public static MapLocation currentRuinLoc = null;
    public static UnitType buildTowerType;
    public static int numWrongTilesInRuin = 0; // Used to determine if there is enough paint to complete
    public static int strictFollowBuildOrderNumTowers;
    // -------- SRP building variables -----------
    public static int numWrongTilesInSRP;
    // -------- Enemy tower attack variables -----------
    // -------- Exploration variables ------------
    public static MapLocation exploreTarget;

    public static void run() throws GameActionException {
        
        
        nearbyRuins = rc.senseNearbyRuins(-1);


        // Try each action in priority order
        if (shouldRefillPaint()) {
            refillPaint();
        } else if (shouldCompleteTower()) {
            completeTower();
        } else if (shouldCompleteSRP()) {
            completeSRP();
        } else if (shouldAttackEnemyTower()) {
            attackEnemyTower();
        } else {
            explore();
        }
    }


    public static boolean shouldRefillPaint() throws GameActionException {
        if (rc.getPaint() < 50) {
            return true;
        }
        return false;
    }
    public static void refillPaint() throws GameActionException {
        if (nearestPaintSource != null) {
            ImpureUtils.withdrawPaintIfPossible(nearestPaintSource);
            if (rc.isMovementReady() && rc.getPaint() > 0) {
                Pathfinder.move(nearestPaintSource);
            }
            Debug.setIndicatorLine(rc.getLocation(), nearestPaintSource, 255, 0, 0);
        }
        rc.setIndicatorString("refillPaint");
    }

    /*
    * Returns true if the following conditions are met:
    *   1. If there are no ruins, find a ruin without enemy paint. Set this as the new current ruin.
    *   2. Check if the current ruin is still valid (no enemy paint, no towers built on it)
    * Notes: 
    *   - Also determines the type of tower to build on the current ruin
    */
    public static boolean shouldCompleteTower() throws GameActionException {
        // If there are no ruins nearby, we can't build a tower
        if (nearbyRuins.length == 0) {
            return falseCompleteTower();
        }

        // If no target ruin, find the nearest ruin without enemy paint
        if (currentRuinLoc == null) {
            for (MapLocation ruinLoc : nearbyRuins) {
                if (!rc.canSenseRobotAtLocation(ruinLoc) && !ruinsWithEnemyPaint.contains(ruinLoc)) {
                    currentRuinLoc = ruinLoc;
                }
            }
        }

        // Check if the ruin we've found or our old target is still valid (no enemy paint, and no tower)
        if (currentRuinLoc != null){
            if (rc.canSenseRobotAtLocation(currentRuinLoc)) {
                return falseCompleteTower();
            }
            buildTowerType = Utils.getBuildType(currentRuinLoc); // Returns null if there is enemy paint
            if (buildTowerType != null) {
                return true;
            } else {
                ruinsWithEnemyPaint.add(currentRuinLoc);
                return falseCompleteTower();
            }
        }
        return falseCompleteTower();
    }
    public static boolean falseCompleteTower(){ // Ensures we don't forget to set currentRuinLoc to false!
        currentRuinLoc = null;
        return false;
    }
    /**
     * 1. Find the nearest incorrectly painted tile within the ruin
     * 2. Moving towards either the wrong tile or the ruin location if there are no wrong tiles
     * 3. Paint incorrect tiles if found
     * 4. Attempt to complete the tower pattern if there are no wrong tiles
     */
    public static void completeTower() throws GameActionException {
        // Try to paint wrong tiles
        MapLocation nearestWrongInRuin = CompleteTower.nearestWrongInRuin(buildTowerType, currentRuinLoc); // Returns null if no wrong tiles OR enemy paint
        if (rc.isMovementReady() && rc.getPaint() > 0) {
            if (nearestWrongInRuin != null) {
                Pathfinder.move(nearestWrongInRuin);
            } else {
                Pathfinder.move(currentRuinLoc);
            }
        }
        if (nearestWrongInRuin != null) {
            Debug.setIndicatorLine(rc.getLocation(), nearestWrongInRuin, 0, 255, 0);
            CompleteTower.paintTower(buildTowerType, currentRuinLoc, nearestWrongInRuin);
        } else {
            if (rc.canCompleteTowerPattern(buildTowerType, currentRuinLoc)) {
                rc.completeTowerPattern(buildTowerType, currentRuinLoc);
            }
            Debug.setIndicatorLine(rc.getLocation(), currentRuinLoc, 0, 255, 0);
        }
        rc.setIndicatorString("completeTower");
    }

    public static boolean shouldCompleteSRP() throws GameActionException {
        return false; // TODO: Implement nearby SRP check
    }
    public static void completeSRP() throws GameActionException {
        // Find nearest incomplete SRP
        // Move towards SRP
        // Complete construction when in range
    }

    public static boolean shouldAttackEnemyTower() throws GameActionException {
        return false; // TODO: Implement enemy tower detection
    }
    public static void attackEnemyTower() throws GameActionException {
        // Locate nearest enemy tower
        // Move within attack range
        // Attack if possible
    }


    public static void explore() throws GameActionException {
        if (exploreTarget == null || rc.getLocation().distanceSquaredTo(exploreTarget) <= 9 || nearBoundary(3)) {
            exploreTarget = getExploreTarget();
        }

        if (rc.isMovementReady() && rc.getPaint() > 0) {
            Pathfinder.move(exploreTarget);
        }
        MapLocation robotLoc = rc.getLocation();
        if (rc.isActionReady() && rc.getPaint() > 0){
            if (rc.senseMapInfo(robotLoc).getPaint() == PaintType.EMPTY){ // TODO target other empty tiles
                if (rc.canAttack(robotLoc)){
                    rc.attack(robotLoc, false);
                }
            }
        }
        Debug.setIndicatorLine(rc.getLocation(), exploreTarget, 255, 255, 0);
        rc.setIndicatorString("Explore");
        }
    public static boolean nearBoundary(int tilesFromEdge) throws GameActionException {
        MapLocation loc = rc.getLocation();
        return loc.x < tilesFromEdge || loc.x >= rc.getMapWidth() - tilesFromEdge || 
               loc.y < tilesFromEdge || loc.y >= rc.getMapHeight() - tilesFromEdge;
    }
    public static MapLocation getExploreTarget() throws GameActionException {
        int[] directionScores = new int[8];

        MapLocation checkLoc;
        MapLocation robotLoc = rc.getLocation();
        // Add bias towards center
        directionScores[robotLoc.directionTo(mapCenter).ordinal()] += 3;
        directionScores[robotLoc.directionTo(mapCenter).rotateLeft().ordinal()] += 2;
        directionScores[robotLoc.directionTo(mapCenter).rotateRight().ordinal()] += 2;
        directionScores[robotLoc.directionTo(mapCenter).rotateLeft().rotateLeft().ordinal()] += 1;
        directionScores[robotLoc.directionTo(mapCenter).rotateRight().rotateRight().ordinal()] += 1;

        // Check 2 tiles away
        checkLoc = robotLoc.translate(2, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.EAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-2, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.WEST.ordinal()] += 5;
        checkLoc = robotLoc.translate(0, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTH.ordinal()] += 5;
        checkLoc = robotLoc.translate(0, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTH.ordinal()] += 5;
        checkLoc = robotLoc.translate(2, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHEAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-2, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHWEST.ordinal()] += 5;
        checkLoc = robotLoc.translate(2, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHEAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-2, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHWEST.ordinal()] += 5;

        // Check 3 tiles away
        checkLoc = robotLoc.translate(3, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.EAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-3, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.WEST.ordinal()] += 5;
        checkLoc = robotLoc.translate(0, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTH.ordinal()] += 5;
        checkLoc = robotLoc.translate(0, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTH.ordinal()] += 5;
        checkLoc = robotLoc.translate(3, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHEAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-3, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHWEST.ordinal()] += 5;
        checkLoc = robotLoc.translate(3, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHEAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-3, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHWEST.ordinal()] += 5;

        Direction bestDir = directions[0];
        int bestScore = directionScores[0];
        for (int i = 7; i >= 0; i--) {
            if (directionScores[i] > bestScore) {
                bestScore = directionScores[i];
                bestDir = directions[i];
            }
        }

        // We translate exactly 12 tiles because it ensures the robot gets a completely new field of vision by the time it reaches the target
        // It is 12 tiles instead of 9 because we stop moving once we SEE the target, not when we reach it. 
        // That way if the target is unreachable, we don't get stuck trying to reach it
        return robotLoc.translate(bestDir.dx * 12, bestDir.dy * 12);
    }
}