package bastien.actions.tower;

import bastien.Robot;
import bastien.actions.Action;
import bastien.utils.*;

import battlecode.common.*;

public class SpawnUnits extends Action {
    public RobotController rc;

    public SpawnUnits(){
        rc = Robot.rc;
        name = "ATTACK";

        DebugUnit.print(3, DebugUnit.INSTANTIATE + name);
    }

    public void init(){
        DebugUnit.print(3, DebugUnit.INIT + name);
        // Do stuff here
    }

    public int getScore(){
        DebugUnit.print(3, DebugUnit.RETURN_SCORE + name);
        // 0 if nothing to do.
        return 2;
    }

    public void play(){
        DebugUnit.print(3, DebugUnit.PLAY + name);
    }
}
