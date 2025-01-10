package bastien.actions.tower;

import bastien.Robot;
import bastien.actions.Action;
import bastien.utils.*;

import battlecode.common.*;

public class SpawnUnits extends Action {
    public RobotController rc;

    public SpawnUnits(){
        rc = Robot.rc;
        name = "SPAWN UNITS";

        DebugUnit.print(3, "Instantiating " + name);
    }

    public void init(){
        DebugUnit.print(3, "Init " + name);
        // Do stuff here
    }

    public int getScore(){
        // 0 if nothing to do.
        return ActionConstants.SpawnUnitsScore;
    }

    public void play(){
        DebugUnit.print(3, "Playing " + name);
    }
}
