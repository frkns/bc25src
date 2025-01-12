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
        // Comms.read();
    }

    public void playTurn() throws GameActionException {
        // ----------------- Calculate interests -----------------
        Debug.print(1, "");
        Debug.print(1, "Calculate interests.");
        for (Interest interest : interests) {
            Debug.print(2, interest.name + " ...", interest.debugInterest);
            interest.updateDirectionScores();
        }
        Interest.maskIllegalMoves();

        // ----------------- Calculate actions -----------------
        Debug.print(1, "");
        Debug.print(1, "Calculate actions.");
        Action bestAction = null;
        int bestTotalScore = 0;
        // ======================= Bunnies =======================
        // TODO Fix calculation for scoreWithDir to account for action + move
        if (_Info.unitType.isRobotType()) {
            int scoreWithDir;
            for (Action action : actions) {
                Debug.print(2, action.name + " ...", action.debugAction);
                action.calcScore();
                if (action.score > 0) {
                    action.resetPossibleDirs();
                    action.setPossibleDirs(action.targetLoc);
                    scoreWithDir = action.calcScoreWithDir(Interest.directionScores);
                    if (scoreWithDir > bestTotalScore) {
                        bestTotalScore = scoreWithDir;
                        bestAction = action;
                    }
                }
            }
            // ---------- Play best action and move ----------
            if (bestAction != null) {
                Debug.print(1, "Playing action: " + bestAction.name);
                Debug.setActionIndicatorString(bestAction);
                Direction dir = Interest.calcBestDir(bestAction);
                if (bestAction.possibleDirs[8]) { // If the robot can play the action before moving, play it first
                    bestAction.play();
                    if (dir != null) {
                        rc.move(dir);
                    }
                } else {
                    if (dir != null) {
                        rc.move(dir);
                        bestAction.play(); // In case our action was dependent on movement
                    }
                }
            } else {
                Debug.print(1, "No action to play");
                Direction dir = Interest.calcBestDir(null);
                if (dir != null) {
                    rc.move(dir);
                }
            }
        // ==================== Towers ======================
        } else {
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
    }


    public void endTurn(){
        // Comms.write();
        Debug.print(0, "End turn.");
    }
}
