package e_action.actions.unit;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.Debug;

public class MopSwing extends Action {
    public RobotController rc;

    public MopSwing(){
        rc = Robot.rc;
        name = "MOP SWING";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }




    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugAction);
    }


    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        score = 5;
        cooldown_reqs = 3;
     }

    
    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
    }


}
