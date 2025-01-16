package e.actions.unit;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import e.Robot;
import e.actions.Action;
import e.knowledge._Info;
import e.utils.Debug;

public class RefillPaint extends Action {
    public RobotController rc;


    public RefillPaint(){
        rc = Robot.rc;
        name = "RefillPaint";
    }

    public void initUnit(){
        Debug.print(1, Debug.INIT + name, debugAction);
        // Initialize any variable needed when a unit first spawns in
    }

    
    // Must return TARGET LOCATION
    // Must return TARGET LOCATION
    // Must return TARGET LOCATION
    // Must return TARGET LOCATION
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

        for (RobotInfo robot : _Info.nearbyAllies) {
            if(robot.getType().isTowerType()) {
                //
            }
    }
    }

    // TODO <-- Use TODO to make notes on potential future improvements
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

        // Code here
    }

    // Add helper functions here

}
