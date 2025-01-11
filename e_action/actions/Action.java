package e_action.actions;

import e_action.Robot;
import e_action.Info;

import battlecode.common.*;

public abstract class Action {
    public RobotController rc;
    public String name = "ABSTRACT Act.";
    public int score = 0; 
    public int type; // None = 0, Action = 1, Action + Move = 2
    public MapLocation targetLoc;
    public boolean debugAction = false;

    public Action(){
        rc = Robot.rc;
    }

    public abstract void initUnit() throws GameActionException;
    public abstract void calcScore() throws GameActionException;
    public abstract int getScore();
    public abstract void play() throws GameActionException;
    public static boolean[] setPossibleDirs(MapLocation targetLoc) {
        boolean[] possibleDirs = new boolean[9];
        if (actionRadiusSquared >= Info.robotLoc.distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.CENTER.ordinal()] = true;
        }
        if (actionRadiusSquared >= Info.robotLoc.add(Direction.NORTH).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.NORTH.ordinal()] = true;
        }
        if (actionRadiusSquared >= Info.robotLoc.add(Direction.NORTHEAST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.NORTHEAST.ordinal()] = true;
        }
        if (actionRadiusSquared >= Info.robotLoc.add(Direction.EAST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.EAST.ordinal()] = true;
        }
        if (actionRadiusSquared >= Info.robotLoc.add(Direction.SOUTHEAST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.SOUTHEAST.ordinal()] = true;
        }
        if (actionRadiusSquared >= Info.robotLoc.add(Direction.SOUTH).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.SOUTH.ordinal()] = true;
        }
        if (actionRadiusSquared >= Info.robotLoc.add(Direction.SOUTHWEST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.SOUTHWEST.ordinal()] = true;
        }
        if (actionRadiusSquared >= Info.robotLoc.add(Direction.WEST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.WEST.ordinal()] = true;
        }
        if (actionRadiusSquared >= Info.robotLoc.add(Direction.NORTHWEST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.NORTHWEST.ordinal()] = true;
        }
        return possibleDirs;
    }
    public static int getComboScore(boolean[] possibleDirs, int[] directionScores, int comboScore) {
        int bestScore = 0;
        if (possibleDirs[0]) {
            int score = directionScores[0] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        if (possibleDirs[1]) {
            int score = directionScores[1] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        if (possibleDirs[2]) {
            int score = directionScores[2] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        if (possibleDirs[3]) {
            int score = directionScores[3] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        if (possibleDirs[4]) {
            int score = directionScores[4] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        if (possibleDirs[5]) {
            int score = directionScores[5] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        if (possibleDirs[6]) {
            int score = directionScores[6] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        if (possibleDirs[7]) {
            int score = directionScores[7] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        if (possibleDirs[8]) {
            int score = directionScores[8] + comboScore;
            if (score > bestScore) bestScore = score;
        }
        return bestScore;
    }
}
