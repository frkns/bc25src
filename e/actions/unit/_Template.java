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
    }

    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

    }

    
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

    }
}
