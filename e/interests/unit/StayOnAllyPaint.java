package e.interests.unit;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.RobotController;
import e.Robot;
import e.interests.Interest;
import e.utils.Constants;
import e.utils.Debug;

public class StayOnAllyPaint extends Interest {
    public RobotController rc;


    public StayOnAllyPaint(){
        rc = Robot.rc;
        name = "StayOnAllyPaint";
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);

        for(MapInfo tile : rc.senseNearbyMapInfos(2)) {
            Direction direction = tile.getMapLocation().directionTo(tile.getMapLocation());
            if(tile.getPaint().isEnemy()){
                adjustDirectionScore(direction,-1 * Constants.StayOnAlliedPaintScore);
            } else if(tile.getPaint().isAlly()) {
                adjustDirectionScore(direction, Constants.StayOnAlliedPaintScore);
            }
        }

    }
}
