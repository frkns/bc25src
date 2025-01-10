package c_actiontemp.Actions;

import battlecode.common.GameActionException;
import battlecode.common.UnitType;
import c_actiontemp.Robot;

public class ActionBuildTower extends Action{
    UnitType toBuild;


    public ActionBuildTower() {
        super();
        name = "BuildTower  ";
    }

    @Override
    public int getScore() {
        toBuild = null;

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

        // TODOS: Fix this force money
        toBuild = UnitType.LEVEL_ONE_MONEY_TOWER;


        if (rc.canCompleteTowerPattern(toBuild, Robot.nearestEmptyRuins)) {
            return Robot.ACTION_BUILD_TOWER;
        }

        return 0;
    }

    @Override
    public void play() {
        if(Robot.nearestEmptyRuins == null){
            return;
        }

        if (rc.canCompleteTowerPattern(toBuild, Robot.nearestEmptyRuins)){
            try {
                rc.completeTowerPattern(toBuild, Robot.nearestEmptyRuins);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            // System.out.println("Built a tower at " + targetLoc + "!");
        }
    }
}
