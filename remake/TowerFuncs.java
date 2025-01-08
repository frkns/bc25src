package remake;

import battlecode.common.*;

import java.util.Random;

public class TowerFuncs {
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

    static UnitType[] ProductionList = new UnitType[]{UnitType.SOLDIER, UnitType.SOLDIER, UnitType.SPLASHER};
    static int ProductionTypeCounter = 0;

    public static void runTower(RobotController rc) throws GameActionException {
        spawnUnits(rc);
    }

    private static void spawnUnits(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)]; // TODO spawn them in the direction they should go
        MapLocation nextLoc = rc.getLocation().add(dir);

        UnitType Unit = ProductionList[ProductionTypeCounter];
        if (rc.canBuildRobot(Unit, nextLoc)) {
            rc.buildRobot(Unit, nextLoc);
            ProductionTypeCounter++;
            if (ProductionTypeCounter >= ProductionList.length) {
                ProductionTypeCounter = 0;
            }
        }
    }
}
