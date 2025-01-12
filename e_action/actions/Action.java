package e_action.actions;

import e_action.Robot;
import e_action.knowledge._Info;

import battlecode.common.*;

public abstract class Action {
    public RobotController rc;
    public String name = "ABSTRACT Act.";
    public int score = 0; 
    public MapLocation targetLoc;
    public boolean[] possibleDirs = new boolean[9];
    public boolean debugAction = false;

    public Action(){
        rc = Robot.rc;
    }

    public abstract void initUnit() throws GameActionException;
    public abstract void calcScore() throws GameActionException;
    public abstract void play() throws GameActionException;
    // Possible dirs include every move that keeps the robot in range of their action's target location.
    public void setPossibleDirs(MapLocation targetLoc) {
        if (_Info.actionRadiusSquared >= _Info.robotLoc.distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.CENTER.ordinal()] = true;
        }
        if (_Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.NORTH).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.NORTH.ordinal()] = true;
        }
        if (_Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.NORTHEAST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.NORTHEAST.ordinal()] = true;
        }
        if (_Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.EAST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.EAST.ordinal()] = true;
        }
        if (_Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.SOUTHEAST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.SOUTHEAST.ordinal()] = true;
        }
        if (_Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.SOUTH).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.SOUTH.ordinal()] = true;
        }
        if (_Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.SOUTHWEST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.SOUTHWEST.ordinal()] = true;
        }
        if (_Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.WEST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.WEST.ordinal()] = true;
        }
        if (_Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.NORTHWEST).distanceSquaredTo(targetLoc)) {
            possibleDirs[Direction.NORTHWEST.ordinal()] = true;
        }
    }
    // Calculate the best move direction where the action can still be played
    public int calcScoreWithDir(int[] directionScores) {
        int bestScore = 0;
        if (possibleDirs[0]) {
            int totalScore = directionScores[0] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        if (possibleDirs[1]) {
            int totalScore = directionScores[1] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        if (possibleDirs[2]) {
            int totalScore = directionScores[2] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        if (possibleDirs[3]) {
            int totalScore = directionScores[3] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        if (possibleDirs[4]) {
            int totalScore = directionScores[4] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        if (possibleDirs[5]) {
            int totalScore = directionScores[5] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        if (possibleDirs[6]) {
            int totalScore = directionScores[6] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        if (possibleDirs[7]) {
            int totalScore = directionScores[7] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        if (possibleDirs[8]) {
            int totalScore = directionScores[8] + score;
            if (totalScore > bestScore) bestScore = score;
        }
        return bestScore;
    }
}
