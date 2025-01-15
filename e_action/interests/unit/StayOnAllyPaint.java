package e_action.interests.unit;

import e_action.Robot;
import e_action.interests.Interest;
import e_action.knowledge._Info;
import e_action.utils.*;

import battlecode.common.*;

public class StayOnAllyPaint extends Interest {
    public RobotController rc;


    public StayOnAllyPaint(){
        rc = Robot.rc;
        name = "FUNC NAME HERE";
        debugInterest = false;
        Debug.print(3, Debug.INIT + name, debugInterest);
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);
        int distance = 0;

        for(MapInfo tile : rc.senseNearbyMapInfos(2)) {
            Direction direction = tile.getMapLocation().directionTo(tile.getMapLocation());
            if(tile.getPaint().isEnemy()){
                adjustDirectionScore(direction,-1 * Constants.AvoidEnemyPaint);
                return;
            }
            if(tile.getPaint().isAlly()) {
                adjustDirectionScore(direction,Constants.AvoidEnemyPaint);
            }
        }
        int x = rc.getLocation().x;
        int y = rc.getLocation().y;

        if(rc.senseMapInfo(new MapLocation(x,y+4)).getPaint() == PaintType.EMPTY)  {
            adjustDirectionScore(Direction.NORTH,Constants.AvoidEnemyPaint);
        }
        if(rc.senseMapInfo(new MapLocation(x+3,y+3)).getPaint() == PaintType.EMPTY)  {
            adjustDirectionScore(Direction.NORTHEAST,Constants.AvoidEnemyPaint);
        }

        if (rc.senseMapInfo(new MapLocation(x, y + 4)).getPaint() == PaintType.EMPTY) {
            adjustDirectionScore(Direction.NORTH, Constants.AvoidEnemyPaint);
        }

        if (rc.senseMapInfo(new MapLocation(x + 3, y + 3)).getPaint() == PaintType.EMPTY) {
            adjustDirectionScore(Direction.NORTHEAST, Constants.AvoidEnemyPaint);
        }

        if (rc.senseMapInfo(new MapLocation(x + 4, y)).getPaint() == PaintType.EMPTY) {
            adjustDirectionScore(Direction.EAST, Constants.AvoidEnemyPaint);
        }

        if (rc.senseMapInfo(new MapLocation(x + 3, y - 3)).getPaint() == PaintType.EMPTY) {
            adjustDirectionScore(Direction.SOUTHEAST, Constants.AvoidEnemyPaint);
        }

        if (rc.senseMapInfo(new MapLocation(x, y - 4)).getPaint() == PaintType.EMPTY) {
            adjustDirectionScore(Direction.SOUTH, Constants.AvoidEnemyPaint);
        }

        if (rc.senseMapInfo(new MapLocation(x - 3, y - 3)).getPaint() == PaintType.EMPTY) {
            adjustDirectionScore(Direction.SOUTHWEST, Constants.AvoidEnemyPaint);
        }

        if (rc.senseMapInfo(new MapLocation(x - 4, y)).getPaint() == PaintType.EMPTY) {
            adjustDirectionScore(Direction.WEST, Constants.AvoidEnemyPaint);
        }

        if (rc.senseMapInfo(new MapLocation(x - 3, y + 3)).getPaint() == PaintType.EMPTY) {
            adjustDirectionScore(Direction.NORTHWEST, Constants.AvoidEnemyPaint);
        }


    }
}
