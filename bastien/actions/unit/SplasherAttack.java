package bastien.actions.unit;

import bastien.Robot;
import bastien.actions.Action;
import bastien.utils.*;

import battlecode.common.*;

public class SplasherAttack extends Action {
    public RobotController rc;

    public SplasherAttack(){
        rc = Robot.rc;
        name = "SSPLASHER ATTACK";
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
