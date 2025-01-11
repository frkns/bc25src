package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

public class _Template extends Action {
    public RobotController rc;

    public _Template(){
        rc = Robot.rc;
        name = "REPLACE WITH FUNC NAME";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }


    // Initialize variables specific to the function here
    

    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);
        // Initialize any variable needed when a unit first spawns in
    }

    // Use Robot.varname to access the variables in the Robot file
    // Included are: Robot.nearbyAllies, Robot.nearbyRuins... 
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        // Calculate score AND store any variables useful to the play() function
        // If the action is illegal, set score to 0
        score = Constants._TemplateScore; // Fixed scores and parameters that contribute to final score go in Utils/Constants
        // Return action requirements
        // None = 0, Action = 1, Move = 2, Action + Move = 3
        cooldown_reqs = 3;
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
