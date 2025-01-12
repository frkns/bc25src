package templateBot.Actions;

import battlecode.common.RobotController;
import templateBot.Robot;

public abstract class Action {
    public Robot robot;
    public RobotController rc;

    public final String name = "Abstract Action";

    public Action(Robot r){
        robot = r;
        rc = Robot.rc;
    }

    public abstract int getScore();
    public abstract void play();
}
