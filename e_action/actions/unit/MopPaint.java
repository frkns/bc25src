package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.knowledge._Info;
import e_action.utils.*;

import battlecode.common.*;

public class MopPaint extends Action {
    public RobotController rc;

    // Initialize any variables needed for the action here

    public MopPaint(){
        rc = Robot.rc;
        name = "Mop Paint";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }

    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);
        // Initialize any variable needed when a unit first spawns in
    }

    // Use GatherInfo.varname to access the variables
    // Included are: GatherInfo.nearbyAllies, GatherInfo.nearbyRuins...
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        // 1. Calculate and set the score variable (defined in Action.java)
        // 2. Store important variables
        // 3. Set targetLoc (defined in Action.java)

        for(MapInfo tile : rc.senseNearbyMapInfos(2)) {
            if(tile.getPaint().isEnemy()) {
                targetLoc = tile.getMapLocation();
                score = Constants.MopEnemyPaint;
                rc.setIndicatorString(tile.getMapLocation()+"");
                if(tile.getMapLocation().equals(rc.getLocation())) {
                    return;
                }
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
