package adrien.Actions;

import battlecode.common.RobotController;
import adrien.Robot;

public abstract class Action {
    public RobotController rc;
    public String name = "ABSTRACT    ";

    public Action(){
        rc = Robot.rc;
    }

    public abstract int getScore();
    public abstract void play();
}
