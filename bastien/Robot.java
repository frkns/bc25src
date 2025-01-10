package bastien;

import bastien.actions.Action;
import bastien.utils.*;

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
        DebugUnit.init();
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
            action.init();
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