package adrien.Interests;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import adrien.Robot;

public abstract class Interest {
    public RobotController rc;
    public String name = "Interest   ";

    public Interest(){
        rc = Robot.rc;
    }
    public void initTurn(){}
    public abstract int getScore(MapLocation loc);
}
