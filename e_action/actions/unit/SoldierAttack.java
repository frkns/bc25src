package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.Debug;
import battlecode.common.RobotController;

public class SoldierAttack extends Action {
    public RobotController rc;

    public SoldierAttack(){
        rc = Robot.rc;
        name = "SOLDIER ATTACK";

        Debug.print(3, "Instantiating " + name);
    }

    public void init(){
        Debug.print(3, "Init " + name);
        // Do stuff here
    }

    public int getScore(){
        // 0 if nothing to do.
        return 2;
    }

    public void play(){
        Debug.print(3, "Playing " + name);
    }
}
