package e.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e.Robot;
import e.actions.unit.*;
import e.interests.unit.*;


public class Soldier extends Robot {
    public Soldier(RobotController rc) throws GameActionException {
        super(rc);

        interests.add(new Explore());

        initUnit();
    }
}
