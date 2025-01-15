package newtst1;

import battlecode.common.*;

public class Towers extends RobotPlayer {

    static boolean spawnedFirstMopper = false;

    public static void run() throws GameActionException {
        if (rc.getRoundNum() <= 1) {
            System.out.println("Siege phase " + siegePhase);
            System.out.println("Mopper phase " + mopperPhase);
        }

        int tries = 8;
        Direction dir = directions[rng.nextInt(8)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (dir == Direction.NORTH || dir == Direction.SOUTH || dir == Direction.EAST || dir == Direction.WEST)
            nextLoc = nextLoc.add(dir);

        while (rc.canSenseRobotAtLocation(nextLoc) && tries-- > 0) {
            dir = directions[rng.nextInt(8)];
            nextLoc = rc.getLocation().add(dir);
            if (dir == Direction.NORTH || dir == Direction.SOUTH || dir == Direction.EAST || dir == Direction.WEST)
                nextLoc = nextLoc.add(dir);
        }
        if (tries == 0) {
            System.out.println("Tower: no empty spaces left in 8 main tiles!");
        }

        if (turnsAlive > 5 && !spawnedFirstMopper) {
            if (rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
                rc.buildRobot(UnitType.MOPPER, nextLoc);
                numSpawnedUnits++;
                spawnedFirstMopper = true;
            }
        }

        // if (numSpawnedUnits == 0 && rc.getRoundNum() < 10 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
        //     rc.buildRobot(UnitType.SOLDIER,nextLoc);
        //     numSpawnedUnits++;
        // }
        if (numSpawnedUnits < 2 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            numSpawnedUnits++;
        }

        if (rc.getRoundNum() >= mopperPhase && numSpawnedUnits < 9) {
            if (rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
                rc.buildRobot(UnitType.MOPPER, nextLoc);
                numSpawnedUnits++;
            }
        }

        rc.attack(null);

        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot  : robots) {
            if (robot.getTeam() != rc.getTeam()) {
                if (rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                }
            }
        }

        // try to transfer paint to nearby friendly robots if we have action cooldown left
        RobotInfo[] superNearbyRobots = rc.senseNearbyRobots(2);
        for (RobotInfo robot  : superNearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                MapLocation robotLoc = robot.getLocation();
                int robotPaint = robot.getPaintAmount();
                int towerPaint = rc.getPaint();
                int transferAmt = Math.min(towerPaint, robot.getType().paintCapacity - robotPaint);
                if (rc.canTransferPaint(robotLoc, transferAmt)) {
                    rc.transferPaint(robotLoc, transferAmt);
                }
            }
        }

    }

}
