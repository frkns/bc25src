package e_actions.units;

import e_actions.Robot;
import e_actions.actions.unit.*;

import battlecode.common.*;


public class Splasher extends Robot {
    public Splasher(RobotController rc){
        super(rc);

        actions.add(new SplasherAttack());
    }
}
