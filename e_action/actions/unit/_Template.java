package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.Debug;

import battlecode.common.*;

public class _Template extends Action {
    public RobotController rc;

    public _Template(){
        rc = Robot.rc;
        name = "REPLACE WITH FUNC NAME";
        score = 0;
        Debug.print(3, Debug.INIT + name);
    }

    // Initialize variables specific to the function here
    // Use Robot.variable_name to access the variables (Comms, nearbyMapInfos...) in Robot file
    // e.g. Robot.directions
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name);
        // Calculate score and store any variable useful to the play() function
    }

    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name);
        // Code here
    }
}
