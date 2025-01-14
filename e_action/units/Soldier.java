package e_action.units;

import e_action.Robot;
import e_action.actions.unit.*;
import e_action.interests.unit.*;

import battlecode.common.*;



public class Soldier extends Robot {
    public Soldier(RobotController rc) throws GameActionException {
        super(rc);

        interests.add(new FindSrpCenter());
        interests.add(new Explore());
        //interests.add(new Ruins());
        actions.add(new CompleteSrp());
//        actions.add(new CompleteTowerPattern());

        Robot.initUnit();
    }
}
