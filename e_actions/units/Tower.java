package e_actions.units;

import e_actions.Robot;
import e_actions.actions.tower.*;

import battlecode.common.*;


public class Tower extends Robot {
    public Tower(RobotController rc) {
        super(rc);

        actions.add(new SpawnUnits());
    }
}


