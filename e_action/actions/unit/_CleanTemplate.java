package e_action.actions.unit;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.Debug;

public class _CleanTemplate extends Action {
    public RobotController rc;

    public _CleanTemplate(){
        rc = Robot.rc;
        name = "REPLACE WITH FUNC NAME";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }


    // Function specific variables


    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);

    }

    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

    }

    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

    }

    // Helper functions

}
