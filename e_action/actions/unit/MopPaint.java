package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.knowledge._Info;
import e_action.utils.*;

import battlecode.common.*;

public class MopPaint extends Action {
    public RobotController rc;


    public MopPaint(){
        rc = Robot.rc;
        name = "Mop Paint";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }

    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);
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
