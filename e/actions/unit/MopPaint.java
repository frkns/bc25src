package e.actions.unit;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.RobotController;
import e.Robot;
import e.actions.Action;
import e.utils.Constants;
import e.utils.Debug;

public class MopPaint extends Action {
    public RobotController rc;


    public MopPaint(){
        rc = Robot.rc;
        name = "MopPaint";
    }

    public void initUnit(){
        Debug.print(1, Debug.INIT + name, debugAction);
    }

    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos(2);
        for(MapInfo tile : nearbyTiles) {
            if(tile.getPaint().isEnemy()) {
                targetLoc = tile.getMapLocation();
                score = Constants.MopPaintScore;
                return;
            }
        }

        if(targetLoc == null) {
            score = 0;
        }
    }


    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

        if(targetLoc != null) {
            if(rc.canAttack(targetLoc)) {
                rc.attack(targetLoc);
                targetLoc = null;
            }
        }

    }

    // Add helper functions here

}
