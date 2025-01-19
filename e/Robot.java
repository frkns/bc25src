package e;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e.actions.Action;
import e.interests.Interest;
import e.knowledge._Info;
import e.utils.Communication;
import e.utils.Debug;
import e.utils.Pathfinder;
import e.utils.Utils;
import battlecode.common.Clock;

import java.util.ArrayDeque;


public class Robot {

    public static RobotController rc;
    public static ArrayDeque<Action> actions = new ArrayDeque<>();
    public static ArrayDeque<Interest> interests = new ArrayDeque<>();

    public Robot(RobotController r) throws GameActionException {
        rc = r;
        Debug.init();
        _Info.init();
        Pathfinder.init();
        Communication.init();
    }

    // Call after setting Actions and Interests
    public static void initUnit() throws GameActionException {
        Debug.print(0, "");
        Debug.print(0, "");
        Debug.print(0, "Create unit => " + rc.getType() + " at " + rc.getLocation());
        for (Interest interest : interests) {
            interest.initUnit();
        }
        for (Action action : actions) {
            action.initUnit();
        }
    }

    public void initTurn() throws GameActionException {
        Debug.reset();
        Debug.print(0, "");
        Debug.print(0, "");
        Debug.print(0, "Start turn => " + rc.getType() + " at " + _Info.robotLoc);
        Interest.resetDirectionScores();
        _Info.update();
        Communication.initTurn();
        Communication.readMessages();
        }

    public void playTurn() throws GameActionException {
        if (_Info.unitType.isRobotType()) {
            playBunnyTurn();
        } else {
            playTowerTurn();
        }
    }

    public void playBunnyTurn() throws GameActionException {
        // ----------------- Calculate interests -----------------
        Debug.print(2, "");
        Debug.print(2, "Calculate interests.");
        for (Interest interest : interests) {
            interest.updateDirectionScores();
        }
        Interest.maskIllegalMoves();
        Interest.calcBestDirAndScore();

        // ----------------- Calculate actions -----------------
        Debug.print(2, "");
        Debug.print(2, "Calculate actions.");
        Action bestAction = null;
        int bestTotalScore = 0;
        int scoreWithDir;
        for (Action action : actions) {
            // Debug calculation line
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
            Debug.print(2, "");
            Debug.print(2, "Action skipped or no legal actions");
            rc.setIndicatorString("No action.");
            if (Interest.bestDir != Direction.CENTER) {
            rc.move(Interest.bestDir);
            }
        } else {
            Debug.print(2, "");
            Debug.print(2, "Playing actions: ");
            rc.setIndicatorString("Playing " + bestAction.name);
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
        Debug.print(2, "");
        Debug.print(2, "Calculate actions:");
        Action bestAction = null;
        int bestTotalScore = 0;

        for (Action action : actions) {
            action.calcScore();
            if (action.score > 0) {
            if (action.score > bestTotalScore) {
                bestTotalScore = action.score;
                bestAction = action;
            }
            }
        }
        if (bestAction != null) {
            Debug.print(2, "");
            Debug.print(2, "Playing actions: ");
            rc.setIndicatorString("Playing " + bestAction.name);
            bestAction.play();
        } else {
            Debug.print(2, "");
            Debug.print(2, "No legal action play");
        }
    }

    public void endTurn() throws GameActionException {
        // Comms.write();
        Debug.print(0, "End turn.");
    }
}
