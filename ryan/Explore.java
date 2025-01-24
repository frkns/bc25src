package ryan;

import battlecode.common.*;

public class Explore extends RobotPlayer {
    public static boolean nearBoundary(int tilesFromEdge) throws GameActionException {
        MapLocation loc = rc.getLocation();
        return loc.x < tilesFromEdge || loc.x >= rc.getMapWidth() - tilesFromEdge || 
               loc.y < tilesFromEdge || loc.y >= rc.getMapHeight() - tilesFromEdge;
    }
    public static MapLocation getExploreTarget() throws GameActionException {
        int[] directionScores = new int[8];

        MapLocation checkLoc;
        MapLocation robotLoc = rc.getLocation();
        // Add bias towards center
        directionScores[robotLoc.directionTo(mapCenter).ordinal()] += 3;
        directionScores[robotLoc.directionTo(mapCenter).rotateLeft().ordinal()] += 2;
        directionScores[robotLoc.directionTo(mapCenter).rotateRight().ordinal()] += 2;
        directionScores[robotLoc.directionTo(mapCenter).rotateLeft().rotateLeft().ordinal()] += 1;
        directionScores[robotLoc.directionTo(mapCenter).rotateRight().rotateRight().ordinal()] += 1;

        // Check 2 tiles away
        checkLoc = robotLoc.translate(2, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.EAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-2, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.WEST.ordinal()] += 5;
        checkLoc = robotLoc.translate(0, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTH.ordinal()] += 5;
        checkLoc = robotLoc.translate(0, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTH.ordinal()] += 5;
        checkLoc = robotLoc.translate(2, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHEAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-2, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHWEST.ordinal()] += 5;
        checkLoc = robotLoc.translate(2, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHEAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-2, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHWEST.ordinal()] += 5;

        // Check 3 tiles away
        checkLoc = robotLoc.translate(3, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.EAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-3, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.WEST.ordinal()] += 5;
        checkLoc = robotLoc.translate(0, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTH.ordinal()] += 5;
        checkLoc = robotLoc.translate(0, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTH.ordinal()] += 5;
        checkLoc = robotLoc.translate(3, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHEAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-3, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHWEST.ordinal()] += 5;
        checkLoc = robotLoc.translate(3, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHEAST.ordinal()] += 5;
        checkLoc = robotLoc.translate(-3, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHWEST.ordinal()] += 5;

        Direction bestDir = directions[0];
        int bestScore = directionScores[0];
        for (int i = 7; i >= 0; i--) {
            if (directionScores[i] > bestScore) {
                bestScore = directionScores[i];
                bestDir = directions[i];
            }
        }

        // We translate exactly 12 tiles because it ensures the robot gets a completely new field of vision by the time it reaches the target
        // It is 12 tiles instead of 9 because we stop moving once we SEE the target, not when we reach it. 
        // That way if the target is unreachable, we don't get stuck trying to reach it
        return robotLoc.translate(bestDir.dx * 12, bestDir.dy * 12);
    }
}