package e_actions.actions.unit;

import e_actions.Robot;
import e_actions.actions.Action;
import e_actions.utils.Debug;
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
