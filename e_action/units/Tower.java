package e_action.units;

import e_action.Robot;
import e_action.actions.tower.*;

import battlecode.common.*;


public class Tower extends Robot {
    public Tower(RobotController rc) {
        super(rc);

        actions.add(new SpawnUnits());
    }
}


