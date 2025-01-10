package e_actions;

import e_actions.actions.Action;
import e_actions.utils.*;

import battlecode.common.*;

import java.util.ArrayDeque;

public abstract class Robot{

    public static RobotController rc;
    public static ArrayDeque<Action> actions = new ArrayDeque<>();

    // -------------- Internal info --------------
    public static int phase;

    // -------------- External infos --------------
    public static RobotInfo[] allies;
    public static RobotInfo[] enemies;
    public static MapLocation[] ruins;

    // -------------- Methods --------------
    public Robot(RobotController r){
        rc = r;
        // messageUnit = new MessageUnit(this);
        Debug.init();
    }

    public void initTurn() throws GameActionException {
        allies = rc.senseNearbyRobots(-1, rc.getTeam());
        enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent()); //Bytecode improvement possible
        phase = Phase.getPhase(rc.getRoundNum(), rc.getMapWidth() * rc.getMapHeight());
        if (rc.getType().isRobotType()) {
            ruins = rc.senseNearbyRuins(-1);
        }
    }

    public void playTurn() throws GameActionException {
        Debug.reset(rc.getRoundNum()); // Reset clock to benchmark

        Debug.print(0, "Start turn => " + rc.getType() + " at " + rc.getLocation());
        Debug.print(1, "Init.");
        initTurn();

        Debug.print(1, "");
        Debug.print(1, "Calculate actions.");
        Action bestAction = null;
        int bestScore = 0;

        for(Action action: actions){
            Debug.print(2, action.name + " ...");
            action.init();
            int score = action.getScore();
            if(score > bestScore){
                bestScore = score;
                bestAction = action;
            }
            Debug.print(2,  "SCORE : " + score);
            Debug.print(2,  "");
        }

        if(bestScore > 0){
            Debug.print(1, "");
            Debug.print(1, "Playing action: " + bestAction.name + " with score " + bestScore);
            rc.setIndicatorString(bestAction.name + " - " + bestScore);
            bestAction.play();
        }else{
            Debug.print(1, "");
            Debug.print(1, "No action to play");
        }
    }

    public void endTurn(){
        Debug.print(0, "End turn.");
    }
}