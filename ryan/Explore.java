package ryan;

import battlecode.common.*;

public class Explore extends RobotPlayer {
    public static int exploreScore;
    public static int exploreTargetDistance = 6; // How many tiles in the best direction do we set the exploreTarget?
    public static int centerBonus = 3; // Adds a bonus for the best direction being toward the center
    public static int emptyTileBonus = 5; // Adds a bonus for each empty tile in target direction
    
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
        if (!robotLoc.equals(mapCenter)) {
            directionScores[robotLoc.directionTo(mapCenter).ordinal()] += centerBonus;
            directionScores[robotLoc.directionTo(mapCenter).rotateLeft().ordinal()] += centerBonus - 1;
            directionScores[robotLoc.directionTo(mapCenter).rotateRight().ordinal()] += centerBonus - 1;
            directionScores[robotLoc.directionTo(mapCenter).rotateLeft().rotateLeft().ordinal()] += centerBonus - 2;
            directionScores[robotLoc.directionTo(mapCenter).rotateRight().rotateRight().ordinal()] += centerBonus - 2;
        }

        // Check 2 tiles away
        checkLoc = robotLoc.translate(2, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.EAST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(-2, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.WEST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(0, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.NORTH.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(0, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.SOUTH.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(2, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.NORTHEAST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(-2, 2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.NORTHWEST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(2, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.SOUTHEAST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(-2, -2); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.SOUTHWEST.ordinal()] += emptyTileBonus;

        // Check 3 tiles away
        checkLoc = robotLoc.translate(3, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.EAST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(-3, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.WEST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(0, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.NORTH.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(0, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.SOUTH.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(3, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.NORTHEAST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(-3, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.NORTHWEST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(3, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.SOUTHEAST.ordinal()] += emptyTileBonus;
        checkLoc = robotLoc.translate(-3, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY && rc.senseMapInfo(checkLoc).isPassable()) directionScores[Direction.SOUTHWEST.ordinal()] += emptyTileBonus;

        Direction bestDir = directions[0];
        exploreScore = directionScores[0];
        for (int i = 7; i >= 0; i--) {
            if (directionScores[i] > exploreScore) {
                exploreScore = directionScores[i];
                bestDir = directions[i];
            }
        }

        return robotLoc.translate(bestDir.dx * exploreTargetDistance, bestDir.dy * exploreTargetDistance);
    }
}