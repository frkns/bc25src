package e_action.utils;

import battlecode.common.RobotController;
import battlecode.common.UnitType;
import e_action.Robot;

import java.util.ArrayList;

//Helper function for the BuildTowerPattern class that decides which tower to build
public class SelectTower {
    public static RobotController rc = Robot.rc;

    public static UnitType selectTower() {
        int phase = Phase.getPhase(rc.getRoundNum(),Robot.MAP_AREA);

        switch(phase) {
            case 1:
                if(rc.getChips() > 2500) {
                    return UnitType.LEVEL_ONE_PAINT_TOWER;
                } else {
                    return UnitType.LEVEL_ONE_MONEY_TOWER;
                }
            case 2:
                if(rc.getChips() < 100) {
                    return UnitType.LEVEL_ONE_MONEY_TOWER;
                } else {
                    return UnitType.LEVEL_ONE_PAINT_TOWER;
                }
            case 3:
                if(rc.getChips() < 500) {
                    return UnitType.LEVEL_ONE_MONEY_TOWER;
                } else {
                    return UnitType.LEVEL_ONE_DEFENSE_TOWER;
                }
        }
        return null;
    }
}
