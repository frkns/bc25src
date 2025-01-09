package adrien.units;

import battlecode.common.*;
import adrien.Actions.ActionDefend;
import adrien.Robot;
import adrien.utils.DebugUnit;

public class Tower extends Robot {

    public Tower(RobotController controlller) {
        super(controlller);

        actions.add(new ActionDefend());
        DebugUnit.debug = false;

        attackCost = 0;
    }

    void Summon() {
        Direction dir = directions[rng.nextInt(directions.length)];
        int robotType = rng.nextInt(100);
        MapLocation nextLoc = rc.getLocation().add(dir).add(dir);
        try {
            if (robotType < 70) {
                if(rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                    rc.buildRobot(UnitType.SOLDIER, nextLoc);
                    DebugUnit.print(2, "Summon SOLDIER on " + nextLoc);
                }
            } else {
                if (rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
                    rc.buildRobot(UnitType.MOPPER, nextLoc);
                    DebugUnit.print(2, "Summon MOPPER on " + nextLoc);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void Upgrade(){
        if(rc.canUpgradeTower(rc.getLocation())){
            try {
                rc.upgradeTower(rc.getLocation());
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initTurn() throws GameActionException {
        super.initTurn();
        rc.attack(null); // Radius attack

        if(rc.getChips() > 1400 || rc.getRoundNum() < 3){
            if(Robot.rng.nextInt(100) <= 2){
                Upgrade();
            }else{
               Summon();
            }
        }
    }
}
