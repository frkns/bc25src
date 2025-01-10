package e_action.units;

import e_action.Robot;
import e_action.actions.unit.*;

import battlecode.common.*;


public class Soldier extends Robot {
    public Soldier(RobotController rc){
        super(rc);

        actions.add(new Explore());
        actions.add(new _Template());
    }
}
