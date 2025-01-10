package bastien.units;

import bastien.Robot;
import bastien.actions.unit.*;
import bastien.utils.*;

import battlecode.common.*;

public class Mopper extends Robot {
    public Mopper(RobotController rc){
        super(rc);

        actions.add(new MopperAttack());
    }
}
