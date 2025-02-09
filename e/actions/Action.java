package e.actions;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import e.Robot;
import e.interests.Interest;
import e.knowledge._Info;

public abstract class Action {
    public RobotController rc;
    public String name = "ABSTRACT Act.";
    public int score = 0; 
    public MapLocation targetLoc;
    public boolean[] possibleDirs = new boolean[9];
    public boolean debugAction = true;

    public abstract void initUnit() throws GameActionException;
    public abstract void calcScore() throws GameActionException;
    public abstract void play() throws GameActionException;

    // Possible dirs include every move that keeps the robot in range of their action's target location.

    public void setPossibleDirs(MapLocation targetLoc) {
        // possibleDirs[direction] = true if target is still reachable after moving in this direction
        // TODO Check center first. If center is in range, we will play the action first so no need to consider other directions
        // TODO Heuristics to minimize the number of checks (if we check 4 locations, can we guarantee the others are by default reachable?)
        possibleDirs[Direction.NORTH.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.NORTH).distanceSquaredTo(targetLoc);
        possibleDirs[Direction.NORTHEAST.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.NORTHEAST).distanceSquaredTo(targetLoc);
        possibleDirs[Direction.EAST.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.EAST).distanceSquaredTo(targetLoc);
        possibleDirs[Direction.SOUTHEAST.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.SOUTHEAST).distanceSquaredTo(targetLoc);
        possibleDirs[Direction.SOUTH.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.SOUTH).distanceSquaredTo(targetLoc);
        possibleDirs[Direction.SOUTHWEST.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.SOUTHWEST).distanceSquaredTo(targetLoc);
        possibleDirs[Direction.WEST.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.WEST).distanceSquaredTo(targetLoc);
        possibleDirs[Direction.NORTHWEST.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.add(Direction.NORTHWEST).distanceSquaredTo(targetLoc);
        possibleDirs[Direction.CENTER.ordinal()] = _Info.actionRadiusSquared >= _Info.robotLoc.distanceSquaredTo(targetLoc);
    }

    // TODO Replace with a hash map
    public int calcScoreWithDir(int[] directionScores) {
        int bestScore = 0;
        if (possibleDirs[8]) { // If action can be played without moving
            int totalScore = Interest.bestDirScore + score;
            if (totalScore > bestScore) bestScore  = totalScore;
            return bestScore;
        }
        if (possibleDirs[0]) {
            int totalScore = directionScores[0] + score;
            if (totalScore > bestScore) bestScore  = totalScore;
        }
        if (possibleDirs[1]) {
            int totalScore = directionScores[1] + score;
            if (totalScore > bestScore) bestScore  = totalScore;
        }
        if (possibleDirs[2]) {
            int totalScore = directionScores[2] + score;
            if (totalScore > bestScore) bestScore  = totalScore;
        }
        if (possibleDirs[3]) {
            int totalScore = directionScores[3] + score;
            if (totalScore > bestScore) bestScore  = totalScore;
        }
        if (possibleDirs[4]) {
            int totalScore = directionScores[4] + score;
            if (totalScore > bestScore) bestScore  = totalScore;
        }
        if (possibleDirs[5]) {
            int totalScore = directionScores[5] + score;
            if (totalScore > bestScore) bestScore  = totalScore;
        }
        if (possibleDirs[6]) {
            int totalScore = directionScores[6] + score;
            if (totalScore > bestScore) bestScore  = totalScore;
        }
        if (possibleDirs[7]) {
            int totalScore = directionScores[7] + score;
            if (totalScore > bestScore) bestScore  = totalScore;
        }
        return bestScore;
    }
}
