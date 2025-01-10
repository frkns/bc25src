package e_actions.units;

import e_actions.Robot;
import e_actions.actions.unit.*;

import battlecode.common.*;


public class Soldier extends Robot {
    public Soldier(RobotController rc){
        super(rc);

        actions.add(new SoldierAttack());
    }
}
