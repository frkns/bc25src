package e_action.actions.tower;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

public class SpawnUnits extends Action {
    public RobotController rc;

    public SpawnUnits(){
        rc = Robot.rc;
        name = "SPAWN UNITS";
        score = 0;
        Debug.print(3, "Instantiating " + name);
    }

    static UnitType[] productionList = {UnitType.SOLDIER, UnitType.MOPPER, UnitType.SPLASHER};
    static int productionTypeCounter = 0;
    static MapLocation spawnLoc;
    static UnitType Unit;

    public void init() throws GameActionException {
        Debug.print(3, "Init " + name);

        Direction dir = Robot.directions[Robot.rng.nextInt(Robot.directions.length)]; // TODO spawn them in the direction they should go
        if (dir.getDeltaX() != 0 && dir.getDeltaY() != 0) {
            spawnLoc = rc.getLocation().add(dir); // If diagonal we can only spawn 1 tile away
        } else {
            spawnLoc = rc.getLocation().add(dir).add(dir);
        }
        Unit = productionList[productionTypeCounter % productionList.length];

        if (rc.canBuildRobot(Unit, spawnLoc)) {
            score = ActionConstants.SpawnUnitsScore;
        }
    }

    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, "Playing " + name);
        rc.buildRobot(Unit, spawnLoc);
        productionTypeCounter++;
    }
}
