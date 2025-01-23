package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.Pathfinder;
import battlecode.common.*;

import static java.lang.Math.min;

public class ActionMarkSRP extends RobotPlayer {
    // ---- Action variables ----
    static MapLocation targetMark;
    static char scoreTargetMark;

    static MapLocation targetExplore;
    static int lastTargetChangeRound = 0;
    static int targetChangeWaitTime = 20;

    // ---- Constants and map infos ----
    static char ZERO = '\u8000'; // Char is unsigned, need to define a zero. Max is \uffff, min is \u0000
    static int ABS_SCORE_X = 1000; // ABS_SCORE_X * 25 need to be < than ZERO (32.000) but bigger than any standard score.
    static char MIN_SCORE_FOR_MARK = (char) ((int) ZERO + 5); // Mark if score >= ZERO

    static char SCORE_X = (char) (65536 - ABS_SCORE_X); // Impossible because of obstacle / ruin
    static char SCORE_C = 230; // Allow connection in 4 directions
    static char SCORE_B = 20; // Allow connection in 3 directions
    static char SCORE_A = 10; // Allow connection in 3 directions but slighly less good

    static char SCORE_P = 1; // Re-use primary paint
    static char SCORE_S = 1; // Re-use secondary paint
    static int ABS_SCORE_E = 4;
    static char SCORE_E = (char) (65536 - ABS_SCORE_E); // Have enemy paint. 65536 = Max char + 1 for char overflow equivalent to an substraction


    // 65536 = Max char + 1 for char overflow equivalent to an substraction
    static char SCORE_MINUS_X = (char) (ABS_SCORE_X);
    static char SCORE_MINUS_C = (char) (65536 - (int) SCORE_C);
    static char SCORE_MINUS_B = (char) (65536 - (int) SCORE_B);
    static char SCORE_MINUS_A = (char) (65536 - (int) SCORE_A);
    static char SCORE_MINUS_P = (char) (65536 - (int) SCORE_P);
    static char SCORE_MINUS_S = (char) (65536 - (int) SCORE_S);
    static char SCORE_MINUS_E = (char) (ABS_SCORE_E);

    // We consider a map of 70*70 where real map start at (5, 5) to avoid going outside without checking
    static int SHIFT_START = 70 * 5 + 5; // Coordinate of (5, 5)
    static char[] scores;
    ; // Default to ZERO
    static char[] paints;
    ; // Default to empty paint

    public static void init() {
        String upperLine = "\u4000".repeat(70);
        String middleLine = "\u4000".repeat(5 + 2) + "\u8000".repeat(rc.getMapWidth() - 4) + "\u4000".repeat(5 + 2 + (60 - rc.getMapWidth()));

        String stringScores = upperLine.repeat(5 + 2) + middleLine.repeat(rc.getMapHeight() - 4) + upperLine.repeat(5 + 2 + (70 - rc.getMapHeight()));

        scores = stringScores.toCharArray(); // Default to ZERO or IMPOSSIBLE near borders.
        paints = "\u0000".repeat(70 * 70).toCharArray(); // Default to empty paint
    }

    // Return false if haven't completely update
    public static boolean updateScores() throws GameActionException {
        Debug.println("\t\tUpdating scores.");
        for (MapInfo info : rc.senseNearbyMapInfos()) {

            if (Clock.getBytecodesLeft() < 5000) {
                Debug.println("\tW - ACTION_MARK_SRP      : Need more time to update. Skipping to next action.");
                action = Action.ACTION_WAITING_FOR_ACTION;
                return false;
            }

            MapLocation locCenter = info.getMapLocation();
            int id = SHIFT_START + locCenter.x + locCenter.y * 70;

            // Can't paint on this zone
            char type = (char) info.getPaint().ordinal();
            if (info.isWall()) {
                // Around wall
                type = 5;
            } else if (info.hasRuin()) {
                if (rc.canSenseRobotAtLocation(locCenter) && rc.getRoundNum() > 100) {
                    // Around ruin if we have a tower after rush phase
                    type = 5;

                } else {
                    // Don't SRP on empty ruin template
                    type = 6;
                }

            } else if (info.getMark() == PaintType.ALLY_PRIMARY) {
                // Other SRP
                type = 7;
            }

            // Check if paint have changed
            if (paints[id] != type) {
                Debug.printl("\t\tCell " + locCenter + " (=" + id + ") have change from " + (int) paints[id] + " to " + (int) type); // + " (" + ((int) scores[id] - (int) ZERO) + " -> ");
                subScoreTo(id, paints[id]); // Remove old score
                addScoreTo(id, type);       // Add new score
                Debug.println(((int) scores[id] - (int) ZERO) + ")");
                paints[id] = type;
            }
        }

        // Update max
        targetMark = null;
        scoreTargetMark = MIN_SCORE_FOR_MARK;
        for (MapInfo info : nearbyTiles) {
            MapLocation loc = info.getMapLocation();
            int id = SHIFT_START + loc.x + loc.y * 70;

            if ((int)scores[id] > (int)scoreTargetMark) {
                targetMark = loc;
                scoreTargetMark = scores[id];
            }

            int scale = ((int) scores[id] - (int) ZERO) * 255 / 40;
            // Debug.println("Score at : " + info.getMapLocation() + " : " + (int) scores[id] + " (" + ((int) scores[id] - (int) ZERO) + ")");
            if (scale < 0) {
                if(scale < 10000){
//                    rc.setIndicatorDot(loc, 0, 0, 255);
                }else{
//                    rc.setIndicatorDot(loc, min(255, -scale), 0, 0);
                }
            } else {
//                rc.setIndicatorDot(loc, 0, min(255, scale), 0);
            }
        }

        Debug.println("\t\tDone !");
        return true;
    }

    public static void printScores() throws GameActionException {
        for (int x = 0; x < rc.getMapWidth(); x++) {
            for (int y = 0; y < rc.getMapHeight(); y++) {
                int id = SHIFT_START + x + 70 * y;
                MapLocation loc = new MapLocation(x, y);

                int scale = ((int) scores[id] - (int) ZERO) * 255 / 40;
                // Debug.println("Score at : " + info.getMapLocation() + " : " + (int) scores[id] + " (" + ((int) scores[id] - (int) ZERO) + ")");
                if (scale < 0) {
                    if(scale < 10000){
//                        rc.setIndicatorDot(loc, 0, 0, 255);
                    }else{
//                        rc.setIndicatorDot(loc, min(255, -scale), 0, 0);
                    }
                } else {
//                    rc.setIndicatorDot(loc, 0, min(255, scale), 0);
                }
            }
        }
    }

    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_MARK_SRP:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//

        // Update terrains info
        if (!updateScores()) return; // If not enough time.


        if (targetMark == null) {
            Debug.println("\tX - ACTION_MARK_SRP      : No target to mark");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        Debug.println("Score min : " + (int) MIN_SCORE_FOR_MARK);
        Debug.println("Best cell " + targetMark + " (score " + (int) scoreTargetMark + " | " + ((int) scoreTargetMark - (int) ZERO) + ")");

        //------------------------------------------------------------------------------//
        // Play Action
        //------------------------------------------------------------------------------//
        Debug.println("\t0 - ACTION_MARK_SRP      : Playing!");
        action = Action.ACTION_MARK_SRP;

        // Mark without moving if I can see all cell of next SRP (<16)
        if (rc.canMark(targetMark) && rc.getLocation().isWithinDistanceSquared(targetMark, 16)) {
            Debug.println("\t\tMarking cell " + targetMark + " (id" + (SHIFT_START + targetMark.x + 70 * targetMark.y) + ")");
            rc.mark(targetMark, false);
            action = Action.ACTION_WAITING_FOR_ACTION;
            rc.setIndicatorLine(rc.getLocation(), targetMark, 0, 255, 0);

            return;
        }

        // Move
        Pathfinder.move(targetMark);
        if (!updateScores()) return; // If not enough time.


        rc.setIndicatorLine(rc.getLocation(), targetMark, 0, 255, 0);


        // Re-try mark after move
        if (rc.canMark(targetMark) && rc.getLocation().isWithinDistanceSquared(targetMark, 16)) {
            Debug.println("\t\tMarking cell " + targetMark + " (id" + (SHIFT_START + targetMark.x + 70 * targetMark.y) + ")");
            rc.mark(targetMark, false);
            action = Action.ACTION_WAITING_FOR_ACTION;
        }
    }
    public static void addScoreTo(int cell, char type) {
        switch (type) {
            case 0: break;
            case 1:
                scores[cell + -140] += SCORE_P;
                scores[cell + -71] += SCORE_P;
                scores[cell + -70] += SCORE_P;
                scores[cell + -69] += SCORE_P;
                scores[cell + -2] += SCORE_P;
                scores[cell + -1] += SCORE_P;
                scores[cell + 0] += SCORE_P;
                scores[cell + 1] += SCORE_P;
                scores[cell + 2] += SCORE_P;
                scores[cell + 69] += SCORE_P;
                scores[cell + 70] += SCORE_P;
                scores[cell + 71] += SCORE_P;
                scores[cell + 140] += SCORE_P;
                break;
            case 2:
                scores[cell + -142] += SCORE_S;
                scores[cell + -141] += SCORE_S;
                scores[cell + -139] += SCORE_S;
                scores[cell + -138] += SCORE_S;
                scores[cell + -72] += SCORE_S;
                scores[cell + -68] += SCORE_S;
                scores[cell + 68] += SCORE_S;
                scores[cell + 72] += SCORE_S;
                scores[cell + 138] += SCORE_S;
                scores[cell + 139] += SCORE_S;
                scores[cell + 141] += SCORE_S;
                scores[cell + 142] += SCORE_S;
                break;
            case 3:
            case 4:
                scores[cell + -142] += SCORE_E;
                scores[cell + -141] += SCORE_E;
                scores[cell + -140] += SCORE_E;
                scores[cell + -139] += SCORE_E;
                scores[cell + -138] += SCORE_E;
                scores[cell + -72] += SCORE_E;
                scores[cell + -71] += SCORE_E;
                scores[cell + -70] += SCORE_E;
                scores[cell + -69] += SCORE_E;
                scores[cell + -68] += SCORE_E;
                scores[cell + -2] += SCORE_E;
                scores[cell + -1] += SCORE_E;
                scores[cell + 0] += SCORE_E;
                scores[cell + 1] += SCORE_E;
                scores[cell + 2] += SCORE_E;
                scores[cell + 68] += SCORE_E;
                scores[cell + 69] += SCORE_E;
                scores[cell + 70] += SCORE_E;
                scores[cell + 71] += SCORE_E;
                scores[cell + 72] += SCORE_E;
                scores[cell + 138] += SCORE_E;
                scores[cell + 139] += SCORE_E;
                scores[cell + 140] += SCORE_E;
                scores[cell + 141] += SCORE_E;
                scores[cell + 142] += SCORE_E;
                break;
// Wall or ruin with tower
            case 5:
                scores[cell + -213] += SCORE_C;
                scores[cell + -212] += SCORE_B;
                scores[cell + -211] += SCORE_A;
                scores[cell + -210] += SCORE_A;
                scores[cell + -209] += SCORE_A;
                scores[cell + -208] += SCORE_B;
                scores[cell + -207] += SCORE_C;
                scores[cell + -143] += SCORE_B;
                scores[cell + -142] += SCORE_X;
                scores[cell + -141] += SCORE_X;
                scores[cell + -140] += SCORE_X;
                scores[cell + -139] += SCORE_X;
                scores[cell + -138] += SCORE_X;
                scores[cell + -137] += SCORE_B;
                scores[cell + -73] += SCORE_A;
                scores[cell + -72] += SCORE_X;
                scores[cell + -71] += SCORE_X;
                scores[cell + -70] += SCORE_X;
                scores[cell + -69] += SCORE_X;
                scores[cell + -68] += SCORE_X;
                scores[cell + -67] += SCORE_A;
                scores[cell + -3] += SCORE_A;
                scores[cell + -2] += SCORE_X;
                scores[cell + -1] += SCORE_X;
                scores[cell + 0] += SCORE_X;
                scores[cell + 1] += SCORE_X;
                scores[cell + 2] += SCORE_X;
                scores[cell + 3] += SCORE_A;
                scores[cell + 67] += SCORE_A;
                scores[cell + 68] += SCORE_X;
                scores[cell + 69] += SCORE_X;
                scores[cell + 70] += SCORE_X;
                scores[cell + 71] += SCORE_X;
                scores[cell + 72] += SCORE_X;
                scores[cell + 73] += SCORE_A;
                scores[cell + 137] += SCORE_B;
                scores[cell + 138] += SCORE_X;
                scores[cell + 139] += SCORE_X;
                scores[cell + 140] += SCORE_X;
                scores[cell + 141] += SCORE_X;
                scores[cell + 142] += SCORE_X;
                scores[cell + 143] += SCORE_B;
                scores[cell + 207] += SCORE_C;
                scores[cell + 208] += SCORE_B;
                scores[cell + 209] += SCORE_A;
                scores[cell + 210] += SCORE_A;
                scores[cell + 211] += SCORE_A;
                scores[cell + 212] += SCORE_B;
                scores[cell + 213] += SCORE_C;
                break;
// Empty ruin
            case 6:
                scores[cell + -284] += SCORE_X;
                scores[cell + -283] += SCORE_X;
                scores[cell + -282] += SCORE_X;
                scores[cell + -281] += SCORE_X;
                scores[cell + -280] += SCORE_X;
                scores[cell + -279] += SCORE_X;
                scores[cell + -278] += SCORE_X;
                scores[cell + -277] += SCORE_X;
                scores[cell + -276] += SCORE_X;
                scores[cell + -214] += SCORE_X;
                scores[cell + -213] += SCORE_X;
                scores[cell + -212] += SCORE_X;
                scores[cell + -211] += SCORE_X;
                scores[cell + -210] += SCORE_X;
                scores[cell + -209] += SCORE_X;
                scores[cell + -208] += SCORE_X;
                scores[cell + -207] += SCORE_X;
                scores[cell + -206] += SCORE_X;
                scores[cell + -144] += SCORE_X;
                scores[cell + -143] += SCORE_X;
                scores[cell + -142] += SCORE_X;
                scores[cell + -141] += SCORE_X;
                scores[cell + -140] += SCORE_X;
                scores[cell + -139] += SCORE_X;
                scores[cell + -138] += SCORE_X;
                scores[cell + -137] += SCORE_X;
                scores[cell + -136] += SCORE_X;
                scores[cell + -74] += SCORE_X;
                scores[cell + -73] += SCORE_X;
                scores[cell + -72] += SCORE_X;
                scores[cell + -71] += SCORE_X;
                scores[cell + -70] += SCORE_X;
                scores[cell + -69] += SCORE_X;
                scores[cell + -68] += SCORE_X;
                scores[cell + -67] += SCORE_X;
                scores[cell + -66] += SCORE_X;
                scores[cell + -4] += SCORE_X;
                scores[cell + -3] += SCORE_X;
                scores[cell + -2] += SCORE_X;
                scores[cell + -1] += SCORE_X;
                scores[cell + 0] += SCORE_X;
                scores[cell + 1] += SCORE_X;
                scores[cell + 2] += SCORE_X;
                scores[cell + 3] += SCORE_X;
                scores[cell + 4] += SCORE_X;
                scores[cell + 66] += SCORE_X;
                scores[cell + 67] += SCORE_X;
                scores[cell + 68] += SCORE_X;
                scores[cell + 69] += SCORE_X;
                scores[cell + 70] += SCORE_X;
                scores[cell + 71] += SCORE_X;
                scores[cell + 72] += SCORE_X;
                scores[cell + 73] += SCORE_X;
                scores[cell + 74] += SCORE_X;
                scores[cell + 136] += SCORE_X;
                scores[cell + 137] += SCORE_X;
                scores[cell + 138] += SCORE_X;
                scores[cell + 139] += SCORE_X;
                scores[cell + 140] += SCORE_X;
                scores[cell + 141] += SCORE_X;
                scores[cell + 142] += SCORE_X;
                scores[cell + 143] += SCORE_X;
                scores[cell + 144] += SCORE_X;
                scores[cell + 206] += SCORE_X;
                scores[cell + 207] += SCORE_X;
                scores[cell + 208] += SCORE_X;
                scores[cell + 209] += SCORE_X;
                scores[cell + 210] += SCORE_X;
                scores[cell + 211] += SCORE_X;
                scores[cell + 212] += SCORE_X;
                scores[cell + 213] += SCORE_X;
                scores[cell + 214] += SCORE_X;
                scores[cell + 276] += SCORE_X;
                scores[cell + 277] += SCORE_X;
                scores[cell + 278] += SCORE_X;
                scores[cell + 279] += SCORE_X;
                scores[cell + 280] += SCORE_X;
                scores[cell + 281] += SCORE_X;
                scores[cell + 282] += SCORE_X;
                scores[cell + 283] += SCORE_X;
                scores[cell + 284] += SCORE_X;
                break;
// Other SRP
            case 7:
                scores[cell + -284] += SCORE_C;
                scores[cell + -283] += SCORE_X;
                scores[cell + -282] += SCORE_X;
                scores[cell + -281] += SCORE_X;
                scores[cell + -280] += SCORE_C;
                scores[cell + -279] += SCORE_X;
                scores[cell + -278] += SCORE_X;
                scores[cell + -277] += SCORE_X;
                scores[cell + -276] += SCORE_C;
                scores[cell + -214] += SCORE_X;
                scores[cell + -213] += SCORE_X;
                scores[cell + -212] += SCORE_X;
                scores[cell + -211] += SCORE_X;
                scores[cell + -210] += SCORE_X;
                scores[cell + -209] += SCORE_X;
                scores[cell + -208] += SCORE_X;
                scores[cell + -207] += SCORE_X;
                scores[cell + -206] += SCORE_X;
                scores[cell + -144] += SCORE_X;
                scores[cell + -143] += SCORE_X;
                scores[cell + -142] += SCORE_X;
                scores[cell + -141] += SCORE_X;
                scores[cell + -140] += SCORE_X;
                scores[cell + -139] += SCORE_X;
                scores[cell + -138] += SCORE_X;
                scores[cell + -137] += SCORE_X;
                scores[cell + -136] += SCORE_X;
                scores[cell + -74] += SCORE_X;
                scores[cell + -73] += SCORE_X;
                scores[cell + -72] += SCORE_X;
                scores[cell + -71] += SCORE_X;
                scores[cell + -70] += SCORE_X;
                scores[cell + -69] += SCORE_X;
                scores[cell + -68] += SCORE_X;
                scores[cell + -67] += SCORE_X;
                scores[cell + -66] += SCORE_X;
                scores[cell + -4] += SCORE_C;
                scores[cell + -3] += SCORE_X;
                scores[cell + -2] += SCORE_X;
                scores[cell + -1] += SCORE_X;
                scores[cell + 0] += SCORE_X;
                scores[cell + 1] += SCORE_X;
                scores[cell + 2] += SCORE_X;
                scores[cell + 3] += SCORE_X;
                scores[cell + 4] += SCORE_C;
                scores[cell + 66] += SCORE_X;
                scores[cell + 67] += SCORE_X;
                scores[cell + 68] += SCORE_X;
                scores[cell + 69] += SCORE_X;
                scores[cell + 70] += SCORE_X;
                scores[cell + 71] += SCORE_X;
                scores[cell + 72] += SCORE_X;
                scores[cell + 73] += SCORE_X;
                scores[cell + 74] += SCORE_X;
                scores[cell + 136] += SCORE_X;
                scores[cell + 137] += SCORE_X;
                scores[cell + 138] += SCORE_X;
                scores[cell + 139] += SCORE_X;
                scores[cell + 140] += SCORE_X;
                scores[cell + 141] += SCORE_X;
                scores[cell + 142] += SCORE_X;
                scores[cell + 143] += SCORE_X;
                scores[cell + 144] += SCORE_X;
                scores[cell + 206] += SCORE_X;
                scores[cell + 207] += SCORE_X;
                scores[cell + 208] += SCORE_X;
                scores[cell + 209] += SCORE_X;
                scores[cell + 210] += SCORE_X;
                scores[cell + 211] += SCORE_X;
                scores[cell + 212] += SCORE_X;
                scores[cell + 213] += SCORE_X;
                scores[cell + 214] += SCORE_X;
                scores[cell + 276] += SCORE_C;
                scores[cell + 277] += SCORE_X;
                scores[cell + 278] += SCORE_X;
                scores[cell + 279] += SCORE_X;
                scores[cell + 280] += SCORE_C;
                scores[cell + 281] += SCORE_X;
                scores[cell + 282] += SCORE_X;
                scores[cell + 283] += SCORE_X;
                scores[cell + 284] += SCORE_C;
                break;
        }
    }

    public static void subScoreTo(int cell, char type) {
        switch (type) {
            case 0: break;
            case 1:
                scores[cell + -140] += SCORE_MINUS_P;
                scores[cell + -71] += SCORE_MINUS_P;
                scores[cell + -70] += SCORE_MINUS_P;
                scores[cell + -69] += SCORE_MINUS_P;
                scores[cell + -2] += SCORE_MINUS_P;
                scores[cell + -1] += SCORE_MINUS_P;
                scores[cell + 0] += SCORE_MINUS_P;
                scores[cell + 1] += SCORE_MINUS_P;
                scores[cell + 2] += SCORE_MINUS_P;
                scores[cell + 69] += SCORE_MINUS_P;
                scores[cell + 70] += SCORE_MINUS_P;
                scores[cell + 71] += SCORE_MINUS_P;
                scores[cell + 140] += SCORE_MINUS_P;
                break;
            case 2:
                scores[cell + -142] += SCORE_MINUS_S;
                scores[cell + -141] += SCORE_MINUS_S;
                scores[cell + -139] += SCORE_MINUS_S;
                scores[cell + -138] += SCORE_MINUS_S;
                scores[cell + -72] += SCORE_MINUS_S;
                scores[cell + -68] += SCORE_MINUS_S;
                scores[cell + 68] += SCORE_MINUS_S;
                scores[cell + 72] += SCORE_MINUS_S;
                scores[cell + 138] += SCORE_MINUS_S;
                scores[cell + 139] += SCORE_MINUS_S;
                scores[cell + 141] += SCORE_MINUS_S;
                scores[cell + 142] += SCORE_MINUS_S;
                break;
            case 3:
            case 4:
                scores[cell + -142] += SCORE_MINUS_E;
                scores[cell + -141] += SCORE_MINUS_E;
                scores[cell + -140] += SCORE_MINUS_E;
                scores[cell + -139] += SCORE_MINUS_E;
                scores[cell + -138] += SCORE_MINUS_E;
                scores[cell + -72] += SCORE_MINUS_E;
                scores[cell + -71] += SCORE_MINUS_E;
                scores[cell + -70] += SCORE_MINUS_E;
                scores[cell + -69] += SCORE_MINUS_E;
                scores[cell + -68] += SCORE_MINUS_E;
                scores[cell + -2] += SCORE_MINUS_E;
                scores[cell + -1] += SCORE_MINUS_E;
                scores[cell + 0] += SCORE_MINUS_E;
                scores[cell + 1] += SCORE_MINUS_E;
                scores[cell + 2] += SCORE_MINUS_E;
                scores[cell + 68] += SCORE_MINUS_E;
                scores[cell + 69] += SCORE_MINUS_E;
                scores[cell + 70] += SCORE_MINUS_E;
                scores[cell + 71] += SCORE_MINUS_E;
                scores[cell + 72] += SCORE_MINUS_E;
                scores[cell + 138] += SCORE_MINUS_E;
                scores[cell + 139] += SCORE_MINUS_E;
                scores[cell + 140] += SCORE_MINUS_E;
                scores[cell + 141] += SCORE_MINUS_E;
                scores[cell + 142] += SCORE_MINUS_E;
                break;
// Wall or ruin with tower
            case 5:
                scores[cell + -213] += SCORE_MINUS_C;
                scores[cell + -212] += SCORE_MINUS_B;
                scores[cell + -211] += SCORE_MINUS_A;
                scores[cell + -210] += SCORE_MINUS_A;
                scores[cell + -209] += SCORE_MINUS_A;
                scores[cell + -208] += SCORE_MINUS_B;
                scores[cell + -207] += SCORE_MINUS_C;
                scores[cell + -143] += SCORE_MINUS_B;
                scores[cell + -142] += SCORE_MINUS_X;
                scores[cell + -141] += SCORE_MINUS_X;
                scores[cell + -140] += SCORE_MINUS_X;
                scores[cell + -139] += SCORE_MINUS_X;
                scores[cell + -138] += SCORE_MINUS_X;
                scores[cell + -137] += SCORE_MINUS_B;
                scores[cell + -73] += SCORE_MINUS_A;
                scores[cell + -72] += SCORE_MINUS_X;
                scores[cell + -71] += SCORE_MINUS_X;
                scores[cell + -70] += SCORE_MINUS_X;
                scores[cell + -69] += SCORE_MINUS_X;
                scores[cell + -68] += SCORE_MINUS_X;
                scores[cell + -67] += SCORE_MINUS_A;
                scores[cell + -3] += SCORE_MINUS_A;
                scores[cell + -2] += SCORE_MINUS_X;
                scores[cell + -1] += SCORE_MINUS_X;
                scores[cell + 0] += SCORE_MINUS_X;
                scores[cell + 1] += SCORE_MINUS_X;
                scores[cell + 2] += SCORE_MINUS_X;
                scores[cell + 3] += SCORE_MINUS_A;
                scores[cell + 67] += SCORE_MINUS_A;
                scores[cell + 68] += SCORE_MINUS_X;
                scores[cell + 69] += SCORE_MINUS_X;
                scores[cell + 70] += SCORE_MINUS_X;
                scores[cell + 71] += SCORE_MINUS_X;
                scores[cell + 72] += SCORE_MINUS_X;
                scores[cell + 73] += SCORE_MINUS_A;
                scores[cell + 137] += SCORE_MINUS_B;
                scores[cell + 138] += SCORE_MINUS_X;
                scores[cell + 139] += SCORE_MINUS_X;
                scores[cell + 140] += SCORE_MINUS_X;
                scores[cell + 141] += SCORE_MINUS_X;
                scores[cell + 142] += SCORE_MINUS_X;
                scores[cell + 143] += SCORE_MINUS_B;
                scores[cell + 207] += SCORE_MINUS_C;
                scores[cell + 208] += SCORE_MINUS_B;
                scores[cell + 209] += SCORE_MINUS_A;
                scores[cell + 210] += SCORE_MINUS_A;
                scores[cell + 211] += SCORE_MINUS_A;
                scores[cell + 212] += SCORE_MINUS_B;
                scores[cell + 213] += SCORE_MINUS_C;
                break;
// Empty ruin
            case 6:
                scores[cell + -284] += SCORE_MINUS_X;
                scores[cell + -283] += SCORE_MINUS_X;
                scores[cell + -282] += SCORE_MINUS_X;
                scores[cell + -281] += SCORE_MINUS_X;
                scores[cell + -280] += SCORE_MINUS_X;
                scores[cell + -279] += SCORE_MINUS_X;
                scores[cell + -278] += SCORE_MINUS_X;
                scores[cell + -277] += SCORE_MINUS_X;
                scores[cell + -276] += SCORE_MINUS_X;
                scores[cell + -214] += SCORE_MINUS_X;
                scores[cell + -213] += SCORE_MINUS_X;
                scores[cell + -212] += SCORE_MINUS_X;
                scores[cell + -211] += SCORE_MINUS_X;
                scores[cell + -210] += SCORE_MINUS_X;
                scores[cell + -209] += SCORE_MINUS_X;
                scores[cell + -208] += SCORE_MINUS_X;
                scores[cell + -207] += SCORE_MINUS_X;
                scores[cell + -206] += SCORE_MINUS_X;
                scores[cell + -144] += SCORE_MINUS_X;
                scores[cell + -143] += SCORE_MINUS_X;
                scores[cell + -142] += SCORE_MINUS_X;
                scores[cell + -141] += SCORE_MINUS_X;
                scores[cell + -140] += SCORE_MINUS_X;
                scores[cell + -139] += SCORE_MINUS_X;
                scores[cell + -138] += SCORE_MINUS_X;
                scores[cell + -137] += SCORE_MINUS_X;
                scores[cell + -136] += SCORE_MINUS_X;
                scores[cell + -74] += SCORE_MINUS_X;
                scores[cell + -73] += SCORE_MINUS_X;
                scores[cell + -72] += SCORE_MINUS_X;
                scores[cell + -71] += SCORE_MINUS_X;
                scores[cell + -70] += SCORE_MINUS_X;
                scores[cell + -69] += SCORE_MINUS_X;
                scores[cell + -68] += SCORE_MINUS_X;
                scores[cell + -67] += SCORE_MINUS_X;
                scores[cell + -66] += SCORE_MINUS_X;
                scores[cell + -4] += SCORE_MINUS_X;
                scores[cell + -3] += SCORE_MINUS_X;
                scores[cell + -2] += SCORE_MINUS_X;
                scores[cell + -1] += SCORE_MINUS_X;
                scores[cell + 0] += SCORE_MINUS_X;
                scores[cell + 1] += SCORE_MINUS_X;
                scores[cell + 2] += SCORE_MINUS_X;
                scores[cell + 3] += SCORE_MINUS_X;
                scores[cell + 4] += SCORE_MINUS_X;
                scores[cell + 66] += SCORE_MINUS_X;
                scores[cell + 67] += SCORE_MINUS_X;
                scores[cell + 68] += SCORE_MINUS_X;
                scores[cell + 69] += SCORE_MINUS_X;
                scores[cell + 70] += SCORE_MINUS_X;
                scores[cell + 71] += SCORE_MINUS_X;
                scores[cell + 72] += SCORE_MINUS_X;
                scores[cell + 73] += SCORE_MINUS_X;
                scores[cell + 74] += SCORE_MINUS_X;
                scores[cell + 136] += SCORE_MINUS_X;
                scores[cell + 137] += SCORE_MINUS_X;
                scores[cell + 138] += SCORE_MINUS_X;
                scores[cell + 139] += SCORE_MINUS_X;
                scores[cell + 140] += SCORE_MINUS_X;
                scores[cell + 141] += SCORE_MINUS_X;
                scores[cell + 142] += SCORE_MINUS_X;
                scores[cell + 143] += SCORE_MINUS_X;
                scores[cell + 144] += SCORE_MINUS_X;
                scores[cell + 206] += SCORE_MINUS_X;
                scores[cell + 207] += SCORE_MINUS_X;
                scores[cell + 208] += SCORE_MINUS_X;
                scores[cell + 209] += SCORE_MINUS_X;
                scores[cell + 210] += SCORE_MINUS_X;
                scores[cell + 211] += SCORE_MINUS_X;
                scores[cell + 212] += SCORE_MINUS_X;
                scores[cell + 213] += SCORE_MINUS_X;
                scores[cell + 214] += SCORE_MINUS_X;
                scores[cell + 276] += SCORE_MINUS_X;
                scores[cell + 277] += SCORE_MINUS_X;
                scores[cell + 278] += SCORE_MINUS_X;
                scores[cell + 279] += SCORE_MINUS_X;
                scores[cell + 280] += SCORE_MINUS_X;
                scores[cell + 281] += SCORE_MINUS_X;
                scores[cell + 282] += SCORE_MINUS_X;
                scores[cell + 283] += SCORE_MINUS_X;
                scores[cell + 284] += SCORE_MINUS_X;
                break;
// Other SRP
            case 7:
                scores[cell + -284] += SCORE_MINUS_C;
                scores[cell + -283] += SCORE_MINUS_X;
                scores[cell + -282] += SCORE_MINUS_X;
                scores[cell + -281] += SCORE_MINUS_X;
                scores[cell + -280] += SCORE_MINUS_C;
                scores[cell + -279] += SCORE_MINUS_X;
                scores[cell + -278] += SCORE_MINUS_X;
                scores[cell + -277] += SCORE_MINUS_X;
                scores[cell + -276] += SCORE_MINUS_C;
                scores[cell + -214] += SCORE_MINUS_X;
                scores[cell + -213] += SCORE_MINUS_X;
                scores[cell + -212] += SCORE_MINUS_X;
                scores[cell + -211] += SCORE_MINUS_X;
                scores[cell + -210] += SCORE_MINUS_X;
                scores[cell + -209] += SCORE_MINUS_X;
                scores[cell + -208] += SCORE_MINUS_X;
                scores[cell + -207] += SCORE_MINUS_X;
                scores[cell + -206] += SCORE_MINUS_X;
                scores[cell + -144] += SCORE_MINUS_X;
                scores[cell + -143] += SCORE_MINUS_X;
                scores[cell + -142] += SCORE_MINUS_X;
                scores[cell + -141] += SCORE_MINUS_X;
                scores[cell + -140] += SCORE_MINUS_X;
                scores[cell + -139] += SCORE_MINUS_X;
                scores[cell + -138] += SCORE_MINUS_X;
                scores[cell + -137] += SCORE_MINUS_X;
                scores[cell + -136] += SCORE_MINUS_X;
                scores[cell + -74] += SCORE_MINUS_X;
                scores[cell + -73] += SCORE_MINUS_X;
                scores[cell + -72] += SCORE_MINUS_X;
                scores[cell + -71] += SCORE_MINUS_X;
                scores[cell + -70] += SCORE_MINUS_X;
                scores[cell + -69] += SCORE_MINUS_X;
                scores[cell + -68] += SCORE_MINUS_X;
                scores[cell + -67] += SCORE_MINUS_X;
                scores[cell + -66] += SCORE_MINUS_X;
                scores[cell + -4] += SCORE_MINUS_C;
                scores[cell + -3] += SCORE_MINUS_X;
                scores[cell + -2] += SCORE_MINUS_X;
                scores[cell + -1] += SCORE_MINUS_X;
                scores[cell + 0] += SCORE_MINUS_X;
                scores[cell + 1] += SCORE_MINUS_X;
                scores[cell + 2] += SCORE_MINUS_X;
                scores[cell + 3] += SCORE_MINUS_X;
                scores[cell + 4] += SCORE_MINUS_C;
                scores[cell + 66] += SCORE_MINUS_X;
                scores[cell + 67] += SCORE_MINUS_X;
                scores[cell + 68] += SCORE_MINUS_X;
                scores[cell + 69] += SCORE_MINUS_X;
                scores[cell + 70] += SCORE_MINUS_X;
                scores[cell + 71] += SCORE_MINUS_X;
                scores[cell + 72] += SCORE_MINUS_X;
                scores[cell + 73] += SCORE_MINUS_X;
                scores[cell + 74] += SCORE_MINUS_X;
                scores[cell + 136] += SCORE_MINUS_X;
                scores[cell + 137] += SCORE_MINUS_X;
                scores[cell + 138] += SCORE_MINUS_X;
                scores[cell + 139] += SCORE_MINUS_X;
                scores[cell + 140] += SCORE_MINUS_X;
                scores[cell + 141] += SCORE_MINUS_X;
                scores[cell + 142] += SCORE_MINUS_X;
                scores[cell + 143] += SCORE_MINUS_X;
                scores[cell + 144] += SCORE_MINUS_X;
                scores[cell + 206] += SCORE_MINUS_X;
                scores[cell + 207] += SCORE_MINUS_X;
                scores[cell + 208] += SCORE_MINUS_X;
                scores[cell + 209] += SCORE_MINUS_X;
                scores[cell + 210] += SCORE_MINUS_X;
                scores[cell + 211] += SCORE_MINUS_X;
                scores[cell + 212] += SCORE_MINUS_X;
                scores[cell + 213] += SCORE_MINUS_X;
                scores[cell + 214] += SCORE_MINUS_X;
                scores[cell + 276] += SCORE_MINUS_C;
                scores[cell + 277] += SCORE_MINUS_X;
                scores[cell + 278] += SCORE_MINUS_X;
                scores[cell + 279] += SCORE_MINUS_X;
                scores[cell + 280] += SCORE_MINUS_C;
                scores[cell + 281] += SCORE_MINUS_X;
                scores[cell + 282] += SCORE_MINUS_X;
                scores[cell + 283] += SCORE_MINUS_X;
                scores[cell + 284] += SCORE_MINUS_C;
                break;
        }
    }
}
