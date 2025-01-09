package bastien.Actions;

import bastien.utils.DebugUnit;
import battlecode.common.RobotController;
import bastien.Robot;

public class ActionExplore extends Action {
    public RobotController rc;

    public ActionExplore(){
        rc = Robot.rc;
        name = "ACTION EXPLORE ";

        DebugUnit.print(3, "Instanciate explore action");
    }

    public void init(){
        DebugUnit.print(3, "Init action explore ...");
        // Do stuff here
    }

    public int getScore(){
        DebugUnit.print(3, "Calculate score ...");
        // 0 if nothing to do.
        return 1;
    }

    public void play(){
        DebugUnit.print(3, "Playing explore ...");
    }
}
