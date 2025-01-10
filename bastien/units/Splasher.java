package bastien.units;

import bastien.Robot;
import bastien.actions.unit.*;
import bastien.utils.*;

import battlecode.common.*;


public class Splasher extends Robot {
    public Splasher(RobotController rc){
        super(rc);

        actions.add(new SplasherAttack());
    }
}
