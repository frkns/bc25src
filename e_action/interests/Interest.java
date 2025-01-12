package e_action.interests;

import e_action.Robot;
import e_action.actions.Action;
import e_action.knowledge._Info;

import battlecode.common.*;


public abstract class Interest {
    public RobotController rc;
    public String name = "ABSTRACT Int.";
    public boolean debugInterest = true;

    public static int [] directionScores = new int[9]; // TODO use cheap hash map instead

    public Interest(){
        rc = Robot.rc;
    }

    public abstract void initUnit() throws GameActionException;
    public abstract void updateDirectionScores() throws GameActionException;
    public static void resetDirectionScores() throws GameActionException {
        directionScores = new int[9];
    }
    public static void adjustDirectionScore(Direction dir, int score){
        directionScores[dir.ordinal()] += score;
    }
    public static void maskIllegalMoves(){
        if (!Robot.rc.canMove(_Info.directions[0])) directionScores[0] = 0;
        if (!Robot.rc.canMove(_Info.directions[1])) directionScores[1] = 0;
        if (!Robot.rc.canMove(_Info.directions[2])) directionScores[2] = 0;
        if (!Robot.rc.canMove(_Info.directions[3])) directionScores[3] = 0;
        if (!Robot.rc.canMove(_Info.directions[4])) directionScores[4] = 0;
        if (!Robot.rc.canMove(_Info.directions[5])) directionScores[5] = 0;
        if (!Robot.rc.canMove(_Info.directions[6])) directionScores[6] = 0;
        if (!Robot.rc.canMove(_Info.directions[7])) directionScores[7] = 0;
        if (!Robot.rc.canMove(_Info.directions[8])) directionScores[8] = 0;
    }

    public static Direction calcBestDir (Action action){
        int score;
        int bestScore = 0;
        Direction bestDir = null;
        if (action != null) {
            if (action.possibleDirs[0]) {
                score = directionScores[0] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[0])) {
                    bestScore = score;
                    bestDir = _Info.directions[0];
                }
            }
            if (action.possibleDirs[1]) {
                score = directionScores[1] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[1])) {
                    bestScore = score;
                    bestDir = _Info.directions[1];
                }
            }
            if (action.possibleDirs[2]) {
                score = directionScores[2] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[2])) {
                    bestScore = score;
                    bestDir = _Info.directions[2];
                }
            }
            if (action.possibleDirs[3]) {
                score = directionScores[3] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[3])) {
                    bestScore = score;
                    bestDir = _Info.directions[3];
                }
            }
            if (action.possibleDirs[4]) {
                score = directionScores[4] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[4])) {
                    bestScore = score;
                    bestDir = _Info.directions[4];
                }
            }
            if (action.possibleDirs[5]) {
                score = directionScores[5] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[5])) {
                    bestScore = score;
                    bestDir = _Info.directions[5];
                }
            }
            if (action.possibleDirs[6]) {
                score = directionScores[6] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[6])) {
                    bestScore = score;
                    bestDir = _Info.directions[6];
                }
            }
            if (action.possibleDirs[7]) {
                score = directionScores[7] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[7])) {
                    bestScore = score;
                    bestDir = _Info.directions[7];
                }
            }
            if (action.possibleDirs[8]) {
                score = directionScores[8] + action.score;
                if (score > bestScore && Robot.rc.canMove(_Info.directions[8])) {
                    bestScore = score;
                    bestDir = _Info.directions[8];
                }
            }
        } else {
            score = directionScores[0];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[0];
            }
            score = directionScores[1];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[1];
            }
            score = directionScores[2];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[2];
            }
            score = directionScores[3];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[3];
            }
            score = directionScores[4];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[4];
            }
            score = directionScores[5];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[5];
            }
            score = directionScores[6];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[6];
            }
            score = directionScores[7];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[7];
            }
            score = directionScores[8];
            if (score > bestScore) {
                bestScore = score;
                bestDir = _Info.directions[8];
            }
        }
        return bestDir;
    }
}
