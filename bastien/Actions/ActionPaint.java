package bastien.Actions;

import bastien.utils.DebugUnit;
import battlecode.common.RobotController;
import bastien.Robot;

public class ActionPaint extends Action {
    public RobotController rc;

    public ActionPaint(){
        rc = Robot.rc;
        name = "ACTION PAINT ";

        DebugUnit.print(3, "Instanciate painting action");
    }

    public void init(){
        DebugUnit.print(3, "Init action paint ...");
        // Do stuff here
    }

    public int getScore(){
        DebugUnit.print(3, "Calculate score ...");
        // 0 if nothing to do.
        return 2;
    }

    public void play(){
        DebugUnit.print(3, "Playing paint ...");
    }
}
