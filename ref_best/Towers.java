package ref_best;

import battlecode.common.*;

public class Towers extends RobotPlayer {

    static boolean spawnedFirstMopper = false;
    static int nonGreedyPhase = Math.max(mapWidth, mapHeight) * 2;  // allow other units to complete ruins if money capped
    static int firstMopper = Math.max(mapWidth, mapHeight) * 2;
    static int reverseChips = 2000;

    public static void run() throws GameActionException {
        for (int i = 0; i < 4; i++) {
            rc.setIndicatorDot(quadrantCenters[i], 255, 255, 255);
        }

        if (rc.getRoundNum() <= 1 && rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
            System.out.println("Number of towers " + rc.getNumberTowers());
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

        if (turnsAlive > 5 && rc.getRoundNum() > firstMopper && !spawnedFirstMopper && rc.getMoney() > reverseChips) {
            if (rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
                System.out.println("spawned first mopper");
                rc.buildRobot(UnitType.MOPPER, nextLoc);
                numSpawnedUnits++;
                spawnedFirstMopper = true;
            }
        }

        int r = rng.nextInt(100);
        rc.setIndicatorString("RNG: " + r);

        UnitType spawn = UnitType.SOLDIER;

        if (rc.getRoundNum() >= mopperPhase) {
            // if (rc.getRoundNum() % 5 == 0) {
            if (r < 20) {
                spawn = UnitType.MOPPER;
            }
        }

        if ((rc.getRoundNum() < nonGreedyPhase || rc.getMoney() > 2000) && rc.getMoney() > reverseChips)
        if (rc.canBuildRobot(spawn, nextLoc)) {
            rc.buildRobot(spawn, nextLoc);
            numSpawnedUnits++;
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
