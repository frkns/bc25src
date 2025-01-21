package lmx;

import battlecode.common.*;

//phase 1 for soldiers
//spread out and build cash towers
public class ActionSplash extends RobotPlayer {
    // ---- Action variables ----
    static MapLocation targetExplore;
    static MapLocation targetSplash;
    static char scoreTargetSplash;
    static int lastTargetChangeRound = 0;
    static int targetChangeWaitTime = 20;

    // ---- Constants and map infos ----
    static char ZERO = '\u8000'; // Char is unsigned, need to define a zero. Max is \uffff, min is \u0000
    static int[] PAINT_SCORE_IF_RECOVER = {0, 0, 0, 0, 0};
    static char MIN_SCORE_FOR_SPLASH = (char) (ZERO + 6);

    // We consider a map of 64*64 where real map start at (2,2) to avoid going outside
    static char[] scores = "\u8000".repeat(4096).toCharArray();
    static char[] paints = "\u0001".repeat(4096).toCharArray();
    static int[] SHIFTS = {-2, -1, 0, 1, 2, 64 - 1, 64, 64+1, 128, -64 - 1, -64, -64+1, -128};


    public static void init(){
        PAINT_SCORE_IF_RECOVER[PaintType.ALLY_PRIMARY.ordinal()] = 0; // Paint with primary
        PAINT_SCORE_IF_RECOVER[PaintType.ALLY_SECONDARY.ordinal()] = -4; // Dont recover already patterns
        PAINT_SCORE_IF_RECOVER[PaintType.EMPTY.ordinal()] = 1;
        PAINT_SCORE_IF_RECOVER[PaintType.ENEMY_PRIMARY.ordinal()] = 2;
        PAINT_SCORE_IF_RECOVER[PaintType.ENEMY_SECONDARY.ordinal()] = 3; // Assuming enemy use secondary for pattern
    }


    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_SPLASH:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//

        ImpureUtils.updateNearestEnemyTower();

        // Update score
        if(targetSplash != null){
            // Avoid changing multiple time of target
            scoreTargetSplash += 5;
        }else{
            scoreTargetSplash = MIN_SCORE_FOR_SPLASH;
        }

        // Update paint on cell
        for(MapInfo info: nearbyTiles){
            MapLocation locCenter = info.getMapLocation();
            int id = 128 + 2 + locCenter.x + 64 * locCenter.y;

            // Can't paint on this zone
            if(info.isWall() && info.hasRuin()){
                paints[id] = (char) info.getPaint().ordinal();
                continue;
            }

            // Check if paint have changed
            if(paints[id] != info.getPaint().ordinal()){
                int score = PAINT_SCORE_IF_RECOVER[info.getPaint().ordinal()] - PAINT_SCORE_IF_RECOVER[paints[id]];
                paints[id] = (char) info.getPaint().ordinal();

                for(int shift: SHIFTS){
                    // int cast because score can be negative, and it's impossible to have negative with char.
                    scores[id + shift] = (char)((int) scores[id + shift] + score);
                }
            }

            // Update best score
            if(scoreTargetSplash < scores[id]){
                targetSplash = info.getMapLocation();
                scoreTargetSplash = scores[id];
            }
        }


        if(targetSplash == null){
            Debug.println("\tX - ACTION_SPLASH        : No target to splash");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        //------------------------------------------------------------------------------//
        // Play Action
        //------------------------------------------------------------------------------//
        Debug.println("\t0 - ACTION_SPLASH        : Playing!");
        action = Action.ACTION_SPLASH;

        // Attack without moving
        if(!rc.canAttack(targetSplash)){
            Pathfinder.move(targetSplash);
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Move
        Pathfinder.move(targetSplash);

        // Re-try attack
        if(rc.canAttack(targetSplash)){
            rc.attack(targetSplash);
            action = Action.ACTION_WAITING_FOR_ACTION;
        }
    }
}
