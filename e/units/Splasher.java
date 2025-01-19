package e.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e.Robot;
import e.interests.unit.Explore;


public class Splasher extends Robot {
    public Splasher(RobotController rc) throws GameActionException {
        super(rc);


        initUnit();
    }
}
