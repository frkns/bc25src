package e_actions.actions.tower;

import e_actions.Robot;
import e_actions.actions.Action;
import e_actions.utils.*;

import battlecode.common.*;

public class SpawnUnits extends Action {
    public RobotController rc;

    public SpawnUnits(){
        rc = Robot.rc;
        name = "SPAWN UNITS";

        Debug.print(3, "Instantiating " + name);
    }

    public void init(){
        Debug.print(3, "Init " + name);
        // Do stuff here
    }

    public int getScore(){
        // 0 if nothing to do.
        return ActionConstants.SpawnUnitsScore;
    }

    public void play(){
        Debug.print(3, "Playing " + name);
    }
}
