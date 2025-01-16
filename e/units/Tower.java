package e.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e.Robot;
import e.actions.tower.SpawnUnits;


public class Tower extends Robot {
    public Tower(RobotController rc) throws GameActionException {
        super(rc);

        actions.add(new SpawnUnits());

        initUnit();
    }
}


