package ryan;

import battlecode.common.*;

import static ryan.ImpureUtils.initExploreTarget;

public class Splashers extends RobotPlayer {

    // ---- Action variables ----
    public static MapLocation targetExplore;
    static MapLocation targetSplash;
    static char scoreTargetSplash;

    // ---- Constants and map infos ----
    static char ZERO = '\u8000'; // Char is unsigned, need to define a zero. Max is \uffff, min is \u0000
    static int[] PAINT_SCORE_IF_RECOVER = {0, 0, 0, 0, 0, 0};
    static char MIN_SCORE_FOR_SPLASH = (char) (ZERO + 6);

    // We consider a map of 64*64 where real map start at (2,2) to avoid going outside
    static char[] scores = "\u8000".repeat(4096).toCharArray();
    static char[] history = "\u0005".repeat(4096).toCharArray();

    static char PAINT_SCORE_BONUS_ENEMY = '\u0002';
    static char PAINT_SCORE_MINUS_BONUS_ENEMY = (char) (65536 - 2);
    static char PAINT_MIN_SCORE = (char) (ZERO + 10);

    public static void init() {
        PAINT_SCORE_IF_RECOVER[PaintType.ALLY_PRIMARY.ordinal()] = 0; // Recover primary with primary -> score = 0
        PAINT_SCORE_IF_RECOVER[PaintType.ALLY_SECONDARY.ordinal()] = -8; // Dont recover already patterns
        PAINT_SCORE_IF_RECOVER[PaintType.EMPTY.ordinal()] = 1;
        PAINT_SCORE_IF_RECOVER[PaintType.ENEMY_PRIMARY.ordinal()] = 3;
        PAINT_SCORE_IF_RECOVER[PaintType.ENEMY_SECONDARY.ordinal()] = 5; // Assuming enemy use secondary for pattern
        PAINT_SCORE_IF_RECOVER[5] = 0;
    }

    public static void run() throws GameActionException {
        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//
        Debug.println("Running.");
        Debug.println("\tInit.");

        int start = Clock.getBytecodeNum();
        Debug.println("\t\tUpdate cells score");
        int scoreInt;
        char score;
        int id;
        for (MapInfo info : nearbyTiles) {
            MapLocation locCenter = info.getMapLocation();
            id = 130 + locCenter.x + 64 * locCenter.y; // 130 = id of (2, 2)

            if(history[id] == info.getPaint().ordinal()){
                continue; // Skip, already update
            }

            // New score - old score
            scoreInt = -PAINT_SCORE_IF_RECOVER[history[id]];
            history[id] = (char) info.getPaint().ordinal();
            scoreInt += PAINT_SCORE_IF_RECOVER[history[id]];

            // Can't paint on this zone
            if (info.isWall() && info.hasRuin()) {
                continue;
            }

            if(scoreInt < 0){
                score = (char) (65536 + scoreInt); // Use overflow to subtract
            }else{
                score = (char) scoreInt;
            }

            /*
            static int[] SHIFTS = {-2, -1, 0, 1, 2, 64 - 1, 64, 64 + 1, 128, -64 - 1, -64, -64 + 1, -128};
            for (int shift : SHIFTS) {
                scores[id + shift] += score;
            }*/
            scores[id - 2] += score;
            scores[id - 1] += score;
            scores[id] += score;
            scores[id + 1] += score;
            scores[id + 2] += score;
            scores[id + 63] += score;
            scores[id + 64] += score;
            scores[id + 65] += score;
            scores[id + 128] += score;
            scores[id - 65] += score;
            scores[id - 64] += score;
            scores[id - 63] += score;
            scores[id - 128] += score;
        }
        Debug.println("\t\t\tDone in " + (Clock.getBytecodeNum() - start) + " bytecodes.");


        // Init target according to spawn direction
        if (turnsAlive <= 1) {
            // Init target according to spawn orientation
            targetExplore = initExploreTarget();
            Debug.println("\t\tInit target explore to : " + targetExplore);
        }

        // nearest paint tower is updated by default
        ImpureUtils.updateNearbyMask(false);
        ImpureUtils.updateNearestEnemyTower();
        ImpureUtils.updateNearestEnemyPaint();

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

        /*
        isRefilling = rc.getPaint() < 100;
        MapLocation paintTarget = nearestPaintTower;
        if (paintTarget != null) {
            ImpureUtils.withdrawPaintIfPossible(paintTarget);
        }
        if (isRefilling && paintTarget != null) {
            targetExplore = paintTarget;
            sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(targetExplore);
        }
        wallAdjacent = false;
        for (MapInfo tile : rc.senseNearbyMapInfos(1)) {
            if (tile.isWall()) {
                wallAdjacent = true;
                break;
            }
        }
        if (wallAdjacent) {
            if (wallRounds++ == 0 && targetExplore != null) {
                sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(targetExplore);
            }
        } else {
            wallRounds = 0;
            if (targetExplore != null)
                sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(targetExplore);
        }
        // Todo : refill
         */

        if (isRefilling && targetExplore != null) {
            // if (Utils.manhattanDistance(rc.getLocation(), target) > 50)
            // Pathfinder.move(target);
            // else
            Debug.println("\t\tRefill to : " + targetExplore);
            Pathfinder.move(targetExplore);
            return;
        }

        // if (nearestPaintTower != null && Utils.manhattanDistance(rc.getLocation(),
        // nearestPaintTower) > refillDistLimit) {
        // isRefilling = false;
        // }


        //------------------------------------------------------------------------------//
        // Attack
        //------------------------------------------------------------------------------//



        // Robots score
        Debug.println("\t\tUpdate enemy score.");
        for (RobotInfo info : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
            MapLocation locCenter = info.getLocation();
            id = 130 + locCenter.x + 64 * locCenter.y;

            scores[id - 2] += PAINT_SCORE_BONUS_ENEMY;
            scores[id - 1] += PAINT_SCORE_BONUS_ENEMY;
            scores[id] += PAINT_SCORE_BONUS_ENEMY;
            scores[id + 1] += PAINT_SCORE_BONUS_ENEMY;
            scores[id + 2] += PAINT_SCORE_BONUS_ENEMY;
            scores[id + 63] += PAINT_SCORE_BONUS_ENEMY;
            scores[id + 64] += PAINT_SCORE_BONUS_ENEMY;
            scores[id + 65] += PAINT_SCORE_BONUS_ENEMY;
            scores[id + 128] += PAINT_SCORE_BONUS_ENEMY;
            scores[id - 65] += PAINT_SCORE_BONUS_ENEMY;
            scores[id - 64] += PAINT_SCORE_BONUS_ENEMY;
            scores[id - 63] += PAINT_SCORE_BONUS_ENEMY;
            scores[id - 128] += PAINT_SCORE_BONUS_ENEMY;
        }

        // Get Max
        targetSplash = null;
        scoreTargetSplash = 0;

        for (MapInfo info : RobotPlayer.rc.senseNearbyMapInfos()) {
            MapLocation locCenter = info.getMapLocation();
            id = 130 + locCenter.x + 64 * locCenter.y;
            if (scoreTargetSplash < scores[id]) {
                if (!info.isWall() && !info.hasRuin()) {
                    targetSplash = locCenter;
                    scoreTargetSplash = scores[id];
                }
            }
        }
        Debug.println("\t\tBest score at " + targetSplash + " (" + (scoreTargetSplash - ZERO) + ")");

        if(scoreTargetSplash < PAINT_MIN_SCORE){
            Debug.println("\t\tBest score is under < " + (int) PAINT_MIN_SCORE + " (" + (PAINT_MIN_SCORE - ZERO) + "). Dont splash.");
            targetSplash = null;
        }

        // Remove score from enemy, we consider they will move.
        for (RobotInfo info : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
            MapLocation locCenter = info.getLocation();
            id = 130 + locCenter.x + 64 * locCenter.y;

            scores[id - 2] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id - 1] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id + 1] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id + 2] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id + 63] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id + 64] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id + 65] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id + 128] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id - 65] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id - 64] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id - 63] += PAINT_SCORE_MINUS_BONUS_ENEMY;
            scores[id - 128] += PAINT_SCORE_MINUS_BONUS_ENEMY;
        }

        if(targetSplash != null) {
            // Attack without moving
            if (rc.canAttack(targetSplash)) {
                Debug.println("\t\tAttacking!");
                rc.attack(targetSplash);
            }else{
                // Move
                Debug.println("\t\tMoving");
                Pathfinder.move(targetSplash);

                // Attack after move
                if (rc.canAttack(targetSplash)) {
                    Debug.println("\t\tAttacking!");
                    rc.attack(targetSplash);
                }
            }
        }

        //------------------------------------------------------------------------------//
        // Explore
        //------------------------------------------------------------------------------//

        explore();
        Debug.println("End.");
        Debug.println("");
        Debug.println("");
    }



    public static void explore() throws GameActionException {
        if (targetExplore == null || rc.getLocation().distanceSquaredTo(targetExplore) <= 9 || (Explore.nearBoundary(3) && !rc.onTheMap(targetExplore))) {
            targetExplore = Explore.getExploreTarget(); // Move a specified number of tiles in the direction with the most empty tiles. Bias toward center for ties.
        }

        if (rc.isMovementReady() && rc.getPaint() > 0) {
            Pathfinder.move(targetExplore);
        }

        rc.setIndicatorString("Explore " + targetExplore);
    }
}
