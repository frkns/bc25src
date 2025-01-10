package bastien.actions.unit;

import bastien.Robot;
import bastien.actions.Action;
import bastien.utils.DebugUnit;
import battlecode.common.RobotController;

public class SoldierAttack extends Action {
    public RobotController rc;

    public SoldierAttack(){
        rc = Robot.rc;
        name = "SOLDIER ATTACK";

        DebugUnit.print(3, "Instantiating " + name);
    }

    public void init(){
        DebugUnit.print(3, "Init " + name);
        // Do stuff here
    }

    public int getScore(){
        // 0 if nothing to do.
        return 2;
    }

    public void play(){
        DebugUnit.print(3, "Playing " + name);
    }
}
