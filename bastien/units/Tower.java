package bastien.units;

import battlecode.common.*;
import bastien.Robot;
import bastien.utils.DebugUnit;

public class Tower extends Robot {

    public Tower(RobotController controller) {
        super(controller);

        DebugUnit.debug = false;
        attackCost = 0;
    }

    void summon() {
        Direction dir = directions[rng.nextInt(directions.length)];
        int robotType = rng.nextInt(100);
        MapLocation nextLoc = rc.getLocation().add(dir).add(dir);
        try {
            if (robotType < 50 || rc.getRoundNum() < 20) {
                if(rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                    rc.buildRobot(UnitType.SOLDIER, nextLoc);
                    DebugUnit.print(2, "Summon SOLDIER on " + nextLoc);
                }
            } else if(robotType < 80) {
                if (rc.canBuildRobot(UnitType.SPLASHER, nextLoc)) {
                    rc.buildRobot(UnitType.SPLASHER, nextLoc);
                    DebugUnit.print(2, "Summon SPLASHER on " + nextLoc);
                }
            }else{
                if (rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
                    rc.buildRobot(UnitType.MOPPER, nextLoc);
                    DebugUnit.print(2, "Summon MOPPER on " + nextLoc);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initTurn() throws GameActionException {
        super.initTurn();

        summon();
        // upgrade() // To complete
    }
}
