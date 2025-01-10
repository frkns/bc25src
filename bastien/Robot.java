package bastien;

import bastien.actions.Action;
import bastien.utils.*;

import battlecode.common.*;

import java.util.ArrayDeque;
import java.util.Random;

public abstract class Robot{
    // -------------- Scores --------------
    public static int ACTION_PAINT = 30;
    public static int ACTION_EXPLORE = 15;

    // -------------- Attributes --------------
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static Random rng = new Random();

    public static RobotController rc;
    public static ArrayDeque<Action> actions = new ArrayDeque<>();


    // -------------- Map infos you could need --------------
    public static RobotInfo[] allies;
    public static RobotInfo[] enemies;
    public static MapLocation[] ruins;

    // -------------- Methods --------------
    public Robot(RobotController r){
        rc = r;
        // messageUnit = new MessageUnit(this);
        DebugUnit.init();
    }

    public void initTurn() throws GameActionException {
        allies = rc.senseNearbyRobots(-1, rc.getTeam());
        enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        ruins = rc.senseNearbyRuins(-1);
    }

    public void playTurn() throws GameActionException {
        DebugUnit.reset(rc.getRoundNum()); // Reset clock to benchmark

        DebugUnit.print(0, "Start turn => " + rc.getType() + " at " + rc.getLocation());
        DebugUnit.print(1, "Init.");
        initTurn();

        DebugUnit.print(1, "");
        DebugUnit.print(1, "Calculate actions.");
        Action bestAction = null;
        int bestScore = 0;

        for(Action action: actions){
            DebugUnit.print(2, action.name + " ...");
            // action.init();
            int score = action.getScore();
            if(score > bestScore){
                bestScore = score;
                bestAction = action;
            }
            DebugUnit.print(2,  "SCORE : " + score);
            DebugUnit.print(2,  "");
        }

        if(bestScore > 0){
            DebugUnit.print(1, "");
            DebugUnit.print(1, "Playing action: " + bestAction.name + " with score " + bestScore);
            rc.setIndicatorString(bestAction.name + " - " + bestScore);
            bestAction.play();
        }else{
            DebugUnit.print(1, "");
            DebugUnit.print(1, "No action to play");
        }
    }

    public void endTurn(){
        DebugUnit.print(0, "End turn.");
    }
}