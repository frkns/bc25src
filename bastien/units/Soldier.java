package bastien.units;

import bastien.Robot;
import bastien.actions.unit.*;
import bastien.utils.*;

import battlecode.common.*;


public class Soldier extends Robot {
    public Soldier(RobotController rc){
        super(rc);

        actions.add(new SoldierAttack());
    }
}
