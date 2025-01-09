package bastien.Actions;

import battlecode.common.RobotController;
import bastien.Robot;

public abstract class Action {
    public RobotController rc;
    public String name = "ABSTRACT Act.";

    public Action(){
        rc = Robot.rc;
    }

    public abstract void init();
    public abstract int getScore();
    public abstract void play();
}
