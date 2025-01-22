package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.ImpureUtils;
import architecture.Tools.Pathfinder;
import battlecode.common.*;
import scala.Char;

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
                int score = PAINT_SCORE_IF_RECOVER[info.getPaint().ordinal()] - PAINT_SCORE_IF_RECOVER[paints[id]] - 10;
                char score_c;
                if(score < 0){
                    score_c = (char)(65536 + score); // 65536 = Max char + 1 for char overflow equivalent to an substraction
                }else{
                    score_c = (char)score;
                }

                paints[id] = (char) info.getPaint().ordinal();

                for(int shift: SHIFTS){
                    scores[id + shift] += score_c;
                }
            }
        }

        // Get Max
        for(MapInfo info: nearbyTiles){
            MapLocation locCenter = info.getMapLocation();
            int id = 128 + 2 + locCenter.x + 70 * locCenter.y;
            if(scoreTargetSplash < scores[id]){
                if(!info.isWall() && !info.hasRuin()) {
                    targetSplash = locCenter;
                    scoreTargetSplash = scores[id];
                }
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
        if(rc.canAttack(targetSplash)){
            rc.attack(targetSplash);
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Move
        Pathfinder.move(targetSplash);

        // Attack after move
        if(rc.canAttack(targetSplash)){
            rc.attack(targetSplash);
            action = Action.ACTION_WAITING_FOR_ACTION;
        }
    }
}
