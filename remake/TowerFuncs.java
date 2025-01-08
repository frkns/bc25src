package remake;

import battlecode.common.*;

import java.util.Random;

class TowerFuncs extends RobotPlayer{
    static final Random rng = new Random(0);

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static UnitType[] productionList = new UnitType[]{UnitType.SOLDIER, UnitType.SOLDIER, UnitType.SPLASHER};
    static int productionTypeCounter = 0;

    static void runTower(RobotController rc) throws GameActionException {
        spawnUnits(rc);
    }

    static void spawnUnits(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)]; // TODO spawn them in the direction they should go
        MapLocation nextLoc = rc.getLocation().add(dir);

        UnitType Unit = productionList[productionTypeCounter];
        if (rc.canBuildRobot(Unit, nextLoc)) {
            rc.buildRobot(Unit, nextLoc);
            productionTypeCounter++;
            if (productionTypeCounter >= productionList.length) {
                productionTypeCounter = 0;
            }
        }
    }
}
