package e.actions.tower;

import battlecode.common.*;
import e.Robot;
import e.actions.Action;
import e.knowledge._Info;
import e.knowledge.Constants;
import e.utils.Debug;

public class SpawnUnits extends Action {
    public RobotController rc;

    public static UnitType[] productionList = {UnitType.SOLDIER};
    public static int productionTypeCounter = 0;
    public static UnitType unit;
    public static int spawnedUnits = 0;

    public SpawnUnits(){
        rc = Robot.rc;
        name = "SpawnUnits";
    }

    public void initUnit(){
        Debug.print(1, Debug.INIT + name, debugAction);

    }

    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

        Direction dir = _Info.directions[_Info.rng.nextInt(_Info.directions.length)];
        targetLoc = rc.getLocation().add(dir);
        unit = productionList[productionTypeCounter % productionList.length];

        if (rc.canBuildRobot(unit, targetLoc) && shouldSpawn()) {
            score = Constants.SpawnUnitsScore;
        } else {
            score = 0;
        }
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

        rc.buildRobot(unit, targetLoc);

        spawnedUnits++;
        productionTypeCounter++;
    }

    public boolean shouldSpawn() {
        return true;
    }


}
