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

    static Direction directionToSpawn = Direction.NORTH;

    static void runTower(RobotController rc) throws GameActionException {
        if (PHASE == 1) {
            MapLocation nextLoc = rc.getLocation().add(directionToSpawn);
            if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
                directionToSpawn = directionToSpawn.rotateRight();
            }
        } else
            spawnUnits(rc);

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                // our team
            } else {
                // their team
                if (rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                }
            }
        }
    }

    static void spawnUnits(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)]; // TODO spawn them in the direction they should go
        MapLocation nextLoc = rc.getLocation().add(dir);

        UnitType Unit = productionList[productionTypeCounter % productionList.length];
        if (rc.canBuildRobot(Unit, nextLoc)) {
            rc.buildRobot(Unit, nextLoc);
            productionTypeCounter++;
        }
    }
}
