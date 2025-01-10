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
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }


    // Initialize variables specific to the function here


    public void initUnit(){
        Debug.print(3, Debug.INITUNIT + name, debugAction);
        // Initialize any variable needed when a unit first spawns in
    }

    // Use Robot.variable_name to access the variables in Robot file
    // e.g. Robot.directions
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        // Calculate score and store any variable useful to the play() function
        // Remember to set the score 0 if the action is illegal/useless this turn!
    }

    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        // Code here
    }
}
