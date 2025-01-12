package templateBot.Interests;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import templateBot.Robot;

public abstract class Interest {
    public Robot robot;
    public RobotController rc;
    public final String name = "Abstract Interest";

    public Interest(Robot r){
        robot = r;
        rc = Robot.rc;
    }

    public abstract int getScore(MapLocation loc);
}
