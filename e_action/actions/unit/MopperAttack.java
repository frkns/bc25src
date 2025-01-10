package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

public class MopperAttack extends Action {
    public RobotController rc;

    public MopperAttack(){
        rc = Robot.rc;
        name = "MOPPER ATTACK";

        Debug.print(3, Debug.INIT + name);
    }

    public void calcScore(){
        Debug.print(3, Debug.CALCSCORE + name);
        // Do stuff here
    }

    public int getScore(){
        // 0 if nothing to do.
        return 2;
    }

    public void play(){
        Debug.print(3, Debug.PLAY + name);
    }
}
