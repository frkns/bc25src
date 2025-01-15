package e_action.interests.unit;

import e_action.Robot;
import e_action.interests.Interest;
import e_action.knowledge._Info;
import e_action.utils.*;

import battlecode.common.*;
import scala.collection.immutable.Stream;

public class FindEnemyPaint extends Interest {
    public RobotController rc;


    public FindEnemyPaint(){
        rc = Robot.rc;
        name = "FIND ENEMY PAINT";
        debugInterest = false;
        Debug.print(3, Debug.INIT + name, debugInterest);
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {

        for(MapInfo tile : rc.senseNearbyMapInfos(2)) {
            if(tile.getPaint().isEnemy()){
                adjustDirectionScore(Direction.CENTER,Constants.FindEnemyPaint);
                return;
            }
        }

        for(MapInfo tile : _Info.nearbyTiles) {
            if(tile.getPaint().isEnemy()) {
                int distance = rc.getLocation().distanceSquaredTo(tile.getMapLocation());
                adjustDirectionScore(rc.getLocation().directionTo(tile.getMapLocation()),Constants.FindEnemyPaint/Math.max(1,distance)/3);
            }
        }
    }
}
