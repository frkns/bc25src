package e_action.utils;

import battlecode.common.RobotController;
import battlecode.common.UnitType;
import e_action.Robot;

import java.util.ArrayList;

// Helper function for the BuildTowerPattern class that decides which tower to build
// Adjust chip rate cutoffs to minimize excess chips in mid-late game

public class SelectTower {
    public static RobotController rc = Robot.rc;

    public static UnitType getTower() {
        int mapArea = Robot.MAP_AREA;
        int chipsRate = Robot.chipsRate;
        int round = rc.getRoundNum();

        if(mapArea < 1000) {
            if(Robot.chipsRate < 60 ) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            }
        } else if (mapArea < 2000) {
            if(Robot.chipsRate < 100 ) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            }
        } else if (mapArea < 3000) {
            if(Robot.chipsRate < 100 ) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else if(round < 300){
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else {
                return UnitType.LEVEL_ONE_DEFENSE_TOWER;
            }
        } else {
            if(Robot.chipsRate < 160 ) {
                return UnitType.LEVEL_ONE_PAINT_TOWER;
            } else if (round < 300) {
                return UnitType.LEVEL_ONE_MONEY_TOWER;
            } else {
                return UnitType.LEVEL_ONE_DEFENSE_TOWER;
            }
        }
    }
}
