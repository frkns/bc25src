package e_action;

import e_action.interests.Interest;
import e_action.actions.Action;
import e_action.knowledge._Info;
import e_action.utils.*;

import battlecode.common.*;

import java.util.ArrayDeque;


public abstract class Robot {

    public static RobotController rc;
    public static ArrayDeque<Action> actions = new ArrayDeque<>();
    public static ArrayDeque<Interest> interests = new ArrayDeque<>();


    public Robot(RobotController r) throws GameActionException {
        rc = r;
        Debug.init();
        Debug.print(0, "Create unit => " + rc.getType() + " at " + rc.getLocation());
        _Info.init();
        Pathfinder.init(rc);
        Communication.init(rc);
    }

    // This runs after the Robot's interests and actions are added
    public static void initUnit() throws GameActionException {
        for (Interest interest : interests) {
            interest.initUnit();
        }
        for (Action action : actions) {
            action.initUnit();
        }
    }

    public void initTurn() throws GameActionException {
        Debug.reset(rc.getRoundNum()); // Reset clock to benchmark
        Debug.print(0, "Start turn => " + rc.getType() + " at " + _Info.robotLoc);
        Interest.resetDirectionScores();
        _Info.update();
        Communication.initTurn();
        Communication.readMessages();
        }

    public void playTurn() throws GameActionException {
        // ----------------- Calculate interests -----------------
        Debug.print(1, "");
        Debug.print(1, "Calculate interests.");
        if (_Info.unitType.isRobotType()) {
            playBunnyTurn();
        } else {
            playTowerTurn();
        }
    }

    public void playBunnyTurn() throws GameActionException {
        for (Interest interest : interests) {
            Debug.print(2, interest.name + " ...", interest.debugInterest);
            interest.updateDirectionScores();
        }
        Interest.maskIllegalMoves();
        Interest.calcBestDirAndScore();

        // ----------------- Calculate actions -----------------
        Debug.print(1, "");
        Debug.print(1, "Calculate actions.");
        Action bestAction = null;
        int bestTotalScore = 0;
        int scoreWithDir;
        for (Action action : actions) {
            Debug.print(2, action.name + " ...", action.debugAction);
            action.calcScore();
            if (action.score > 0) {
            action.setPossibleDirs(action.targetLoc);
            scoreWithDir = action.calcScoreWithDir(Interest.directionScores);
            if (scoreWithDir > bestTotalScore) {
                bestTotalScore = scoreWithDir;
                bestAction = action;
            }
            }
        }
        if (bestAction != null){
            Interest.calcBestDirWithAction(bestAction);
        }
        // ---------- Play best action and move ----------
        if (Interest.bestDirScore >= bestTotalScore) { // It is better to move and skip the action.
            Debug.print(1, "Action skipped or no legal actions");
            if (Debug.debug){
            rc.setIndicatorString("No action.");
            }
            if (Interest.bestDir != Direction.CENTER) {
            rc.move(Interest.bestDir);
            }
        } else {
            Debug.print(1, "Playing action: " + bestAction.name);
            Debug.setActionIndicatorString(bestAction);
            if (bestAction.possibleDirs[8]) { // If the robot can play the action before moving, play it first
            bestAction.play();
            if (Interest.bestDir != Direction.CENTER) {
                if (rc.getPaint() > 0){ // Edge case where the robot uses its last paint, disabling its movement
                    rc.move(Interest.bestDir);
                }
            }
            } else {
            if (Interest.bestDir != Direction.CENTER) {
                rc.move(Interest.bestDir);
                bestAction.play(); // In case our action was dependent on movement
            }
            }
        }
        }

    public void playTowerTurn() throws GameActionException {
        Debug.print(1, "");
        Debug.print(1, "Calculate actions.");
        Action bestAction = null;
        int bestTotalScore = 0;

        for (Action action : actions) {
            Debug.print(2, action.name + " ...", action.debugAction);
            action.calcScore();
            if (action.score > 0) {
            if (action.score > bestTotalScore) {
                bestTotalScore = action.score;
                bestAction = action;
            }
            }
        }
        if (bestAction != null) {
            Debug.print(1, "Playing action: " + bestAction.name);
            Debug.setActionIndicatorString(bestAction);
            bestAction.play();
        } else {
            Debug.print(1, "No action to play");
        }
    }

        public void endTurn(){
        // Comms.write();
        Debug.print(0, "End turn.");
    }
}
