package e_action.utils;

import battlecode.common.*;
import e_action.Robot;
import e_action.knowledge._Info;

public class SRP {
    public static RobotController rc = Robot.rc;

    public static boolean centerIsValid(MapLocation center) throws GameActionException {
        for (MapInfo tile : _Info.nearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();
            // If tile is a part of the SRP
            if (tileLoc.x >= center.x - 2 && tileLoc.x <= center.x + 2 &&
                    tileLoc.y >= center.y - 2 && tileLoc.y <= center.y + 2) {
                if (tile.hasRuin() || tile.isWall() ||
                        tile.getPaint() == PaintType.ENEMY_SECONDARY ||
                        tile.getPaint() == PaintType.ENEMY_PRIMARY) {
                    return false;
                }
            }
        }
        return true;
    }

    // Returns the first unvalidated center it sees
    public static MapLocation findUnvalidatedCenter() throws GameActionException{
        // Calculate the vertical offset needed to reach the next valid y-coordinate (y % 3 == 2)
        int dy = (2 - _Info.robotLoc.y % 3) % 3;
        // Calculate the shifted x-coordinate based on y position
        int x = _Info.robotLoc.x + ((_Info.robotLoc.y + dy) / 3);
        // Calculate the horizontal offset needed to reach the next valid x-coordinate ((x + y/3) % 4 == 2)
        int dx = (2 - x % 4) % 4;

        // Generate four potential center locations by applying offsets in all combinations
        MapLocation center1 = new MapLocation(_Info.robotLoc.x + dx, _Info.robotLoc.y + dy);
        MapLocation center2 = new MapLocation(_Info.robotLoc.x + dx, _Info.robotLoc.y - dy);
        MapLocation center3 = new MapLocation(_Info.robotLoc.x - dx, _Info.robotLoc.y + dy);
        MapLocation center4 = new MapLocation(_Info.robotLoc.x - dx, _Info.robotLoc.y - dy);

        // Calculate distances to each center, setting to MAX_VALUE if center is illegal or completed
        int d1 = _Info.illegalOrCompletedCenters.contains(center1) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(center1);
        int d2 = _Info.illegalOrCompletedCenters.contains(center2) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(center2);
        int d3 = _Info.illegalOrCompletedCenters.contains(center3) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(center3);
        int d4 = _Info.illegalOrCompletedCenters.contains(center4) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(center4);

        // Return the closest valid center, or null if none are valid
        if (d1 <= d2 && d1 <= d3 && d1 <= d4 && d1 != Integer.MAX_VALUE) return center1;
        if (d2 <= d3 && d2 <= d4 && d2 != Integer.MAX_VALUE) return center2;
        if (d3 <= d4 && d3 != Integer.MAX_VALUE) return center3;
        if (d4 != Integer.MAX_VALUE) return center4;
        return null;
    }

}
