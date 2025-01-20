package ref_better;

import battlecode.common.*;

//phase 1 for soldiers
//spread out and build cash towers
public class Splashers extends RobotPlayer {
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

    // We consider a map of 64*64 where real map start at (2,2) to avoid goind outside
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
        //------------------------------------------------------------------------------//
        // Updating infos
        //------------------------------------------------------------------------------//
        ImpureUtils.updateNearestEnemyTower();

        // Update score for each cell if we paint at this location
        targetSplash = null;
        scoreTargetSplash = MIN_SCORE_FOR_SPLASH;
        for(MapInfo info: nearbyTiles){
            int id = 128 + 2 + info.getMapLocation().x + 64 * info.getMapLocation().y;

            // Check if paint have change
            if(paints[id] != info.getPaint().ordinal()){

                // Can't paint on this zone
                if(info.isWall() && info.hasRuin()){
                    paints[id] = (char) info.getPaint().ordinal();
                    continue;
                }

                int score = PAINT_SCORE_IF_RECOVER[info.getPaint().ordinal()] - PAINT_SCORE_IF_RECOVER[paints[id]];
                paints[id] = (char) info.getPaint().ordinal();
                System.out.println("Adding score " + score + "(" + (int) scores[id] + " / " + (int)scoreTargetSplash + ")");

                for(int shift: SHIFTS){
                    // int cast because score can be negative
                    scores[id + shift] = (char)((int) scores[id + shift] + score);
                }
            }

            if(scoreTargetSplash < scores[id]){
                System.out.println("Max score was : " + (int)scoreTargetSplash + " and is now " + (int)scores[id]);
                targetSplash = info.getMapLocation();
                scoreTargetSplash = scores[id];
            }
        }

        /*
        // Only available on ref_best
        //------------------------------------------------------------------------------//
        // Messages
        //------------------------------------------------------------------------------//

        Communication.readMessages();  // Communication code saves information to relevant variables (e.g. ruin locs, enemy tower locs)
        if (nearestEnemyTower != null) {
            // Report to all allied towers in range
            for (RobotInfo robot : nearbyRobots) {
                if (robot.getTeam() == rc.getTeam() && robot.getType().isTowerType()) {
                    Debug.println(Debug.COMMS, "Reporting to tower ID: " + robot.getID());
                    Communication.sendLocationMessage(robot.getID(), 0, nearestEnemyTower);
                }
            }
        }

        //------------------------------------------------------------------------------//
        // Refill
        //------------------------------------------------------------------------------//
        isRefilling = rc.getPaint() < 50;
        if (isRefilling) {
            HeuristicPath.fullFill = false;
            Pathfinder.move(nearestPaintSource);
            rc.setIndicatorLine(rc.getLocation(), nearestPaintSource, 131, 252, 131);
            return;
        }
        */

        //------------------------------------------------------------------------------//
        // Splash or move to splash target
        //------------------------------------------------------------------------------//
        if(targetSplash != null){
            if(!rc.canAttack(targetSplash)){
                Pathfinder.move(targetSplash);
            }

            if(rc.canAttack(targetSplash)){
                rc.attack(targetSplash);
            }
            return;
        }

        //------------------------------------------------------------------------------//
        // Explore
        //------------------------------------------------------------------------------//
        if (targetExplore == null
                || rc.getLocation().isWithinDistanceSquared(targetExplore, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {

            if (rng.nextInt(2) == 0)
                targetExplore = Utils.randomLocationInQuadrant(Utils.currentQuadrant());
            else
                targetExplore = new MapLocation(rng.nextInt(mapWidth), rng.nextInt(mapHeight));
            lastTargetChangeRound = rc.getRoundNum();
        }
        Pathfinder.move(targetExplore);
    }
}
