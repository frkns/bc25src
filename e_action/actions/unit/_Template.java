package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

public class _Template extends Action {
    public RobotController rc;

    // Initialize any variables needed for the action here
    

    public _Template(){
        rc = Robot.rc;
        name = "REPLACE WITH FUNC NAME";
        type = null; // REMEMBER TO SET THE COOLDOWN REQUIREMENTS! No requirements = 0, Action = 1, Action + Move = 2
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
        // 1. Calculate and assign score variable
        // 2. Store important variables
        // 3. Set targetLoc
    }

    public int getScore(){
        return score;
    }

    // TODO <-- Use TODO to make notes on potential future improvements
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        // Code here
    }

    // Add helper functions here

}
