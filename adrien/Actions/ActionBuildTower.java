package adrien.Actions;

import battlecode.common.GameActionException;
import battlecode.common.UnitType;
import adrien.Robot;

public class ActionBuildTower extends Action{
    UnitType buildType;
    UnitType toBuild;


    public ActionBuildTower() {
        super();
        name = "BuildTower  ";
    }

    @Override
    public int getScore() {
        buildType = null;

        if(Robot.nearestEmptyRuins == null){
            return 0;
        }

        int choice = Robot.rng.nextInt(5); // More money
        switch (choice){
            case 0:
                toBuild = UnitType.LEVEL_ONE_PAINT_TOWER;
                break;
            case 1:
                toBuild = UnitType.LEVEL_ONE_DEFENSE_TOWER;
                break;
            default:
                toBuild = UnitType.LEVEL_ONE_MONEY_TOWER;
                break;
        }

        if (rc.canCompleteTowerPattern(toBuild, Robot.nearestEmptyRuins)) {
            buildType = toBuild;
            return Robot.ACTION_BUILD_TOWER;
        }

        return 0;
    }

    @Override
    public void play() {
        if(Robot.nearestEmptyRuins == null){
            return;
        }

        if (rc.canCompleteTowerPattern(buildType, Robot.nearestEmptyRuins)){
            try {
                rc.completeTowerPattern(buildType, Robot.nearestEmptyRuins);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            // System.out.println("Built a tower at " + targetLoc + "!");
        }
    }
}
