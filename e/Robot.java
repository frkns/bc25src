package e;

import battlecode.common.*;
import e.actions.*;
import e.interests.*;
import e.knowledge.*;
import e.utils.*;


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
        if (_Info.unitType.isRobotType()) 
            playBunnyTurn();
        playTowerTurn();
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
        
        // Find the best action by comparing scores that include directional preferences
        for (Action action : actions) {
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
        
        if (bestAction != null) 
            Interest.calcBestDirWithAction(bestAction);
        
        // ---------- Execute movement and actions ----------
        if (Interest.bestDirScore >= bestTotalScore) {
            // Scenario 1: Moving without action is better than any action
            Debug.print(2, "");
            Debug.print(2, "Action skipped or no legal actions");
            rc.setIndicatorString("No action.");
            if (Interest.bestDir != Direction.CENTER) 
                rc.move(Interest.bestDir);
        } else {
            // Scenario 2: We have a good action to perform
            Debug.print(2, "");
            Debug.print(2, "Playing actions: ");
            rc.setIndicatorString("Playing " + bestAction.name);
            if (bestAction.possibleDirs[8]) {
                // Case 2a: Action can be performed from current position
                // Execute action first, then move if possible
                bestAction.play();
                if (Interest.bestDir != Direction.CENTER && rc.getPaint() > 0) 
                    rc.move(Interest.bestDir);
            } else {
                // Case 2b: Action needs to be performed from a different position
                // Move first, then execute action
                if (Interest.bestDir != Direction.CENTER) 
                    rc.move(Interest.bestDir);
                bestAction.play();
            }
        }
    }

    public void playTowerTurn() throws GameActionException {
        // Towers are stationary, so they only need to consider actions
        Debug.print(2, "");
        Debug.print(2, "Calculate actions:");
        Action bestAction = null;
        int bestTotalScore = 0;

        // Find the highest scoring action
        for (Action action : actions) {
            action.calcScore();
            if (action.score > 0 && action.score > bestTotalScore) {
                bestTotalScore = action.score;
                bestAction = action;
            }
        }
        
        // Execute the best action if one exists
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
        Debug.print(0, "End turn.");
    }
}
