package e_action.units;

import e_action.Robot;
import e_action.actions.unit.*;
import e_action.interests.unit.*;

import battlecode.common.*;


public class Splasher extends Robot {
    public Splasher(RobotController rc) throws GameActionException {
        super(rc);

        interests.add(new Explore());

        Robot.initUnit();
    }
}
