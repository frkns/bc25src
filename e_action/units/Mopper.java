package e_action.units;

import e_action.Robot;
import e_action.actions.unit.*;

import battlecode.common.*;


public class Mopper extends Robot {
    public Mopper(RobotController rc){
        super(rc);

        actions.add(new Explore());
        actions.add(new MopperAttack());
    }
}
