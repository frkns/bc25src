package kenny;

import battlecode.common.*;

public class Towers extends RobotPlayer {

    static boolean spawnedFirstMopper = false;
    static int nonGreedyPhase = (int)(mx * 2);  // allow other units to complete ruins / upgrade towers if money capped
    static int firstMopper = (int)(mx * 2);


    public static void run() throws GameActionException {
        // debugging stuff
        for (int i = 0; i < 4; i++) {
            rc.setIndicatorDot(quadrantCenters[i], 255, 255, 255);
        }
        if (rc.getRoundNum() <= 1 && rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
            System.out.println("Number of towers " + rc.getNumberTowers());
            System.out.println("Siege phase " + siegePhase);
            System.out.println("Mopper phase " + mopperPhase);
        }
        /* */

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


        int r = rng.nextInt(100);
        rc.setIndicatorString("RNG: " + r);

        UnitType spawn = UnitType.SOLDIER;

        if (rc.getRoundNum() >= mopperPhase) {
            // if (rc.getRoundNum() % 5 == 0) {
            if (r < 20) {
                spawn = UnitType.MOPPER;
            }
        }

        boolean spawnIsFirstMopper = false;
        if (turnsAlive > 5 && rc.getRoundNum() > firstMopper && !spawnedFirstMopper && rc.getMoney() > reserveChips) {
            spawn = UnitType.MOPPER;
            spawnIsFirstMopper = true;
        }

        if ((rc.getRoundNum() < nonGreedyPhase || rc.getMoney() > 2000)
            && rc.getMoney() - spawn.moneyCost >= reserveChips
            && (rc.getPaint() - spawn.paintCost >= reservePaint || rc.getRoundNum() < reservePaintPhase || rc.getType().getBaseType() != UnitType.LEVEL_ONE_PAINT_TOWER))
            // only reserve paint if we are a paint tower ^
        if (rc.canBuildRobot(spawn, nextLoc)) {
            rc.buildRobot(spawn, nextLoc);
            if (spawnIsFirstMopper)
                spawnedFirstMopper = true;
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
