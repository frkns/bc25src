package kenny_version_lmx;

import battlecode.common.*;


public class Splashers extends RobotPlayer {

    public static MapLocation target;

    // ---- Action variables ----
    static MapLocation targetExplore;
    static MapLocation targetSplash;
    static char scoreTargetSplash;
    static int lastTargetChangeRound = 0;
    static int targetChangeWaitTime = 20;

    // ---- Constants and map infos ----
    static char ZERO = '\u8000'; // Char is unsigned, need to define a zero. Max is \uffff, min is \u0000
    static int[] PAINT_SCORE_IF_RECOVER = {0, 0, 0, 0, 0, 0};

    // We consider a map of 64*64 where real map start at (2,2) to avoid going outside
    static char[] scores = "\u8000".repeat(4096).toCharArray();
    static char[] history = "\u0005".repeat(4096).toCharArray();
    static char[] ruins = "\u0000".repeat(4096).toCharArray();

    static char PAINT_SCORE_BONUS_ENEMY = '\u0003';
    static char PAINT_SCORE_MINUS_BONUS_ENEMY = (char) (65536 - 2);
    static char PAINT_MIN_SCORE = (char) (ZERO + 12);
    static char PAINT_SCORE_BONUS_RUINS = '\u0001';

    public static void init() {
        PAINT_SCORE_IF_RECOVER[PaintType.ALLY_PRIMARY.ordinal()] = 0; // Recover primary with primary -> score = 0
        PAINT_SCORE_IF_RECOVER[PaintType.ALLY_SECONDARY.ordinal()] = -8; // Dont recover already patterns
        PAINT_SCORE_IF_RECOVER[PaintType.EMPTY.ordinal()] = 1;
        PAINT_SCORE_IF_RECOVER[PaintType.ENEMY_PRIMARY.ordinal()] = 3;
        PAINT_SCORE_IF_RECOVER[PaintType.ENEMY_SECONDARY.ordinal()] = 4; // Assuming enemy use secondary for pattern
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

            if (history[id] == info.getPaint().ordinal()) {
                continue; // Skip, already update
            }

            // New score - old score
            scoreInt = -PAINT_SCORE_IF_RECOVER[history[id]];
            history[id] = (char) info.getPaint().ordinal();
            scoreInt += PAINT_SCORE_IF_RECOVER[history[id]];

            if (scoreInt < 0) {
                score = (char) (65536 + scoreInt); // Use overflow to subtract
            } else {
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

        Debug.println("\t\tUpdate Ruins.");
        for (MapLocation locCenter : rc.senseNearbyRuins(-1)) {
            id = 130 + locCenter.x + 64 * locCenter.y;

            if(ruins[id] != 0){
                break;
            }
            ruins[id] = '\u0001';

            scores[id - 2] += PAINT_SCORE_BONUS_RUINS;
            scores[id - 1] += PAINT_SCORE_BONUS_RUINS;
            scores[id] += PAINT_SCORE_BONUS_RUINS;
            scores[id + 1] += PAINT_SCORE_BONUS_RUINS;
            scores[id + 2] += PAINT_SCORE_BONUS_RUINS;
            scores[id + 63] += PAINT_SCORE_BONUS_RUINS;
            scores[id + 64] += PAINT_SCORE_BONUS_RUINS;
            scores[id + 65] += PAINT_SCORE_BONUS_RUINS;
            scores[id + 128] += PAINT_SCORE_BONUS_RUINS;
            scores[id - 65] += PAINT_SCORE_BONUS_RUINS;
            scores[id - 64] += PAINT_SCORE_BONUS_RUINS;
            scores[id - 63] += PAINT_SCORE_BONUS_RUINS;
            scores[id - 128] += PAINT_SCORE_BONUS_RUINS;
        }
        Debug.println("\t\t\tDone in " + (Clock.getBytecodeNum() - start) + " bytecodes.");
        

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

        isRefilling = rc.getPaint() < 100;
        MapLocation paintTarget = nearestPaintTower;
        if (paintTarget != null) {
            ImpureUtils.withdrawPaintIfPossible(paintTarget);
        }
        if (isRefilling && paintTarget != null) {
            target = paintTarget;
            sqDistanceToTargetOnWallTouch = rc.getLocation().distanceSquaredTo(target);
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

        if (isRefilling && target != null) {
            // if (Utils.manhattanDistance(rc.getLocation(), target) > 50)
            // Pathfinder.move(target);
            // else
            Debug.println("\t\tRefill to : " + target);
            HeuristicPath.refill(target); // 1.
            return;
        }

        // if (nearestPaintTower != null && Utils.manhattanDistance(rc.getLocation(),
        // nearestPaintTower) > refillDistLimit) {
        // isRefilling = false;
        // }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            target = Utils.randomLocationInQuadrant(rng.nextInt(4));
            Debug.println("\t\tChange targetExplore to : " + target);
            lastTargetChangeRound = rc.getRoundNum();
        }

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
                targetSplash = locCenter;
                scoreTargetSplash = scores[id];
            }
        }
        Debug.println("\t\tBest score at " + targetSplash + " (" + (scoreTargetSplash - ZERO) + ")");

        if (scoreTargetSplash < PAINT_MIN_SCORE) {
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

        if (targetSplash != null) {
            // Attack without moving
            if (rc.canAttack(targetSplash)) {
                Debug.println("\t\tAttacking!");
                rc.attack(targetSplash);
            } else {
                // Move
                Debug.println("\t\tMoving");
                HeuristicPath.splasherMove(targetSplash);

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

        if (rc.isMovementReady()) {
            MapLocation tt = Utils.chooseTowerTarget();
            if (tt != null && rc.getNumberTowers() > 8 && rc.isActionReady()) {
                if (rc.getID() % 2 == 0)
                    Pathfinder.move(tt);
                else
                    Pathfinder.move(target);
            } else {
                HeuristicPath.splasherMove(target);
            }
        }
        Debug.println("End.");
        Debug.println("");
        Debug.println("");
    }
}
