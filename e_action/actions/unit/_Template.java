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
        // 1. Calculate scores AND store any variables useful to the play() function
        // 2. If the action is illegal, set scores to 0
        // 3. Fixed scores and parameters that contribute to final scores go in Utils/Constants
        
        // score = Constants._TemplateScore; 
        
        // Return action requirements
        // None = 0, Action = 1, Action + Move = 2
        
        // cooldown_reqs = 2;
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
