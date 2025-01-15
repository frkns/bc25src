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

        for(MapInfo tile : _Info.nearbyTiles) {
            distance = rc.getLocation().distanceSquaredTo(tile.getMapLocation());
            Direction direction = rc.getLocation().directionTo(tile.getMapLocation());
            int score = Constants.AvoidEnemyPaint / (Math.max(distance,1));
            if(tile.getPaint().isEnemy()) {
                adjustDirectionScore(direction,-1 * score);
            } else if (tile.getPaint().isAlly() && distance <= 2) {
                adjustDirectionScore(direction,Constants.AvoidEnemyPaint/2);
            }
            if(tile.getPaint() == PaintType.EMPTY && distance >20) {
                adjustDirectionScore(direction,Constants.AvoidEnemyPaint*2);
            }
        }
    }
}
