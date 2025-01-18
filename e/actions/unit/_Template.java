package e.actions.unit;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e.Robot;
import e.actions.Action;
import e.utils.Debug;

public class _Template extends Action {
    public RobotController rc;

    // Initialize any variables needed for the action here
    

    public _Template(){
        rc = Robot.rc;
        name = "_Template";
    }

    public void initUnit(){
        Debug.print(1, Debug.INIT + name, debugAction);
        // Initialize any variable needed when a unit first spawns in
    }

    // Use GatherInfo.varname to access the variables
    // Included are: GatherInfo.nearbyAllies, GatherInfo.nearbyRuins...
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        // 1. Calculate and set the score variable (defined in Action.java)
        // 2. Store important variables
        // 3. Set targetLoc (defined in Action.java)
    }

    
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        // Code here
    }

    // Add helper functions here

}
