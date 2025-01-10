package c_actiontemp.Actions;

import battlecode.common.*;
import c_actiontemp.Robot;

public class ActionSetTowerPattern extends Action{
    UnitType buildType;

    public ActionSetTowerPattern() {
        super();
        name = "Patern tower";
    }

    @Override
    public int getScore() {
        buildType = null;

        if(Robot.nearestEmptyRuins == null){
            return 0;
        }

        Direction dir = Robot.nearestEmptyRuins.directionTo(rc.getLocation()); // todos: Any direction
        MapLocation shouldBeMarked = Robot.nearestEmptyRuins.add(dir);
        try {
            if(rc.senseMapInfo(shouldBeMarked).getMark() != PaintType.EMPTY){
                return 0;
            }

            if(rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, Robot.nearestEmptyRuins)){
                return 10;
                // System.out.println("Trying to build a tower at " + targetLoc);
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void play() {
        if(Robot.nearestEmptyRuins == null){
            return;
        }

        try {
            rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, Robot.nearestEmptyRuins);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
