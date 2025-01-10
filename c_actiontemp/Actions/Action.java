package c_actiontemp.Actions;

import battlecode.common.RobotController;
import c_actiontemp.Robot;

public abstract class Action {
    public RobotController rc;
    public String name = "ABSTRACT    ";

    public Action(){
        rc = Robot.rc;
    }

    public abstract int getScore();
    public abstract void play();
}
