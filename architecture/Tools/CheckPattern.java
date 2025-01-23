package architecture.Tools;

import architecture.RobotPlayer;
import battlecode.common.*;

public class CheckPattern {
    public static PatternReport analyseTowerPatern(MapLocation center, UnitType tower) throws GameActionException {
        return analysePattern(center, RobotPlayer.rc.getTowerPattern(tower), false);
    }

    public static PatternReport analyseSRP(MapLocation center) throws GameActionException{
        return analysePattern(center, RobotPlayer.rc.getResourcePattern(), true);
    }

    public static PatternReport analysePattern(MapLocation center, boolean[][] pattern, boolean checkCenter) throws GameActionException {
        int numWrong = 0;
        int numUnknown = 0;
        MapLocation nearestWrong = null;
        MapLocation nearestWrongEnemie = null;
        RobotController rc = RobotPlayer.rc;
        int x_corner = center.x - 2;
        int y_corner = center.y - 2;

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2 && checkCenter == false) {
                    continue;
                }

                MapLocation loc = new MapLocation(x_corner + i, y_corner + j);

                if (!rc.canSenseLocation(loc)){
                    numUnknown++;
                    continue;
                }

                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {

                    numWrong++;
                    if (nearestWrongEnemie == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrongEnemie)) {
                        nearestWrongEnemie = loc;
                    }
                }

                if (paint == PaintType.EMPTY
                        || (paint == PaintType.ALLY_SECONDARY && !pattern[i][j])
                        || (paint == PaintType.ALLY_PRIMARY && pattern[i][j])) {

                    numWrong++;
                    if (nearestWrong == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrong)) {
                        nearestWrong = loc;
                    }
                }
            }
        }

        return new PatternReport(numWrong, numUnknown, nearestWrong, nearestWrongEnemie);
    }
}
