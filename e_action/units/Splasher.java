package e_action.units;

import e_action.Robot;
import e_action.actions.unit.*;

import battlecode.common.*;


public class Splasher extends Robot {
    public Splasher(RobotController rc){
        super(rc);

        actions.add(new Explore());
    }
}
