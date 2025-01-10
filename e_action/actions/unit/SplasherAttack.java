package e_action.actions.unit;

import battlecode.common.RobotController;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.Debug;

public class SplasherAttack extends Action {
    public RobotController rc;

    public SplasherAttack(){
        rc = Robot.rc;
        name = "SPLASHER ATTACK";

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
