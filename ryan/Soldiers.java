package ryan;


import ryan.fast.*;
import battlecode.common.*;

public class Soldiers extends RobotPlayer {
    // -------- Tower building variables -------------
    public static MapLocation[] nearbyRuins;
    public static FastLocSet ruinsWithEnemyPaint = new FastLocSet();
    public static UnitType buildTowerType;
    public static MapLocation currentRuinLoc;
    public static MapLocation nearestWrongInRuin;
    public static int numWrongTilesInRuin = 0; // Used to determine if there is enough paint to complete
    public static int strictFollowBuildOrderNumTowers;
    // -------- Srp building variables -----------
    public static FastLocSet invalidSrp = new FastLocSet();
    public static MapLocation currentSrpLoc;
    public static MapLocation nearestWrongInSrp;
    public static int numWrongTilesInSrp;
    // -------- Enemy tower attack variables -----------
    // -------- Exploration variables ------------
    public static MapLocation exploreTarget;


    public static void initTurn() throws GameActionException {
        // Initialize variables
        nearbyRuins = rc.senseNearbyRuins(-1);
        MapRecorder.initTurn();
    }

    public static void playTurn() throws GameActionException {
                // Try each action in priority order
        if (shouldRefillPaint()) {
            refillPaint();
        } else if (shouldCompleteTower()) {
            completeTower();
        } else if (shouldCompleteSrp()) {
            completeSrp();
        } else if (shouldAttackEnemyTower()) {
            attackEnemyTower();
        } else {
            explore();
        }
    }

    public static void endTurn() throws GameActionException {
        ImpureUtils.tryMarkSrp();
        MapRecorder.recordSym(1000);
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
        return falseCompleteTower(); // If the code has reached this, there are ruins nearby, but they are all marked by enemy paint.
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
        nearestWrongInRuin = CompleteTower.getNearestWrongInRuin(buildTowerType, currentRuinLoc); // Returns null if no wrong tiles. Returns MapLocation(-1, -1) if enemy paint
        if (rc.isMovementReady() && rc.getPaint() > 0) {
            if (nearestWrongInRuin != null) {
                Pathfinder.move(nearestWrongInRuin);
            } else {
                Pathfinder.move(currentRuinLoc);
            }
        }
        if (nearestWrongInRuin != null) { // Omitting the check for enemy paint after moving leads to painting 1 extra tile but not important enough to rewrite
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


    public static boolean shouldCompleteSrp() throws GameActionException {
        // If no target Srp, find the nearest possible center of Srp
        if (currentSrpLoc == null) {
            for (MapInfo tile : nearbyTiles) {
                if (tile.getMark() == PaintType.ALLY_PRIMARY && !invalidSrp.contains(tile.getMapLocation())) {
                    currentSrpLoc = tile.getMapLocation();
                }
            }
        }
        
        // Check if the Srp we've found or our old target is still valid (no enemy paint)
        if (currentSrpLoc != null) {
            nearestWrongInSrp = CompleteSrp.getNearestWrongInSrp(currentSrpLoc); // Returns null if no wrong tiles. Returns MapLocation(-1, -1) if enemy paint
            if (nearestWrongInSrp != null && nearestWrongInSrp.equals(new MapLocation(-1, -1))){
                invalidSrp.add(currentSrpLoc);
                return falseCompleteSrp();
            } else if (nearestWrongInSrp == null && rc.getLocation().isWithinDistanceSquared(currentSrpLoc, 4) && !rc.canCompleteResourcePattern(currentSrpLoc) && rc.getChips() > 200){
                invalidSrp.add(currentSrpLoc);
                return falseCompleteSrp(); // The Srp must be completed, since it is close enough to confirm there are no wrong tiles in the entire SRP, and canComplete returns false.
            } else {
                return true; // We are not in range to see all the tiles
            }
        }
        return falseCompleteSrp();  // If the code has reached this, there are marked Srp centers nearby, but they are all marked by enemy paint.
        }

        public static boolean falseCompleteSrp(){ // Ensures we don't forget to set currentSrpLoc to false!
            currentSrpLoc = null;
            return false;
        }

        public static void completeSrp() throws GameActionException {

        
            if (rc.isMovementReady() && rc.getPaint() > 0) {
                if (nearestWrongInSrp != null && !nearestWrongInSrp.equals(new MapLocation(-1, -1))){
                Pathfinder.move(nearestWrongInSrp);
                } else {
                Pathfinder.move(currentSrpLoc);
                }
            }

            if (nearestWrongInSrp != null && !nearestWrongInSrp.equals(new MapLocation(-1, -1))) {
                Debug.setIndicatorLine(rc.getLocation(), nearestWrongInSrp, 0, 0, 255);
                CompleteSrp.paintSrp(currentSrpLoc, nearestWrongInSrp);
            } else {
                if (rc.canCompleteResourcePattern(currentSrpLoc)) {
                    invalidSrp.add(currentSrpLoc);
                    rc.completeResourcePattern(currentSrpLoc);
                }
                Debug.setIndicatorLine(rc.getLocation(), currentSrpLoc, 0, 0, 255);
            }
            rc.setIndicatorString("completeSrp");
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
        if (exploreTarget == null || rc.getLocation().distanceSquaredTo(exploreTarget) <= 9 || (Explore.nearBoundary(3) && !rc.onTheMap(exploreTarget))) {
            exploreTarget = Explore.getExploreTarget();
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
        if (!rc.onTheMap(exploreTarget)) {
            MapLocation indicatorExploreTarget = new MapLocation(
                Math.min(Math.max(exploreTarget.x, 0), rc.getMapWidth() - 1),
                Math.min(Math.max(exploreTarget.y, 0), rc.getMapHeight() - 1)
            );
            Debug.setIndicatorLine(rc.getLocation(), indicatorExploreTarget, 255, 255, 0);
        } else {
            Debug.setIndicatorLine(rc.getLocation(), exploreTarget, 255, 255, 0);
        }
        rc.setIndicatorString("Explore");
    }
}