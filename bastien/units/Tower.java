package bastien.units;

import bastien.Robot;
import bastien.actions.tower.*;

import battlecode.common.*;


public class Tower extends Robot {
    public Tower(RobotController rc) {
        super(rc);

        actions.add(new SpawnUnits());
    }
}


