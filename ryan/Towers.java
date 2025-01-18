package ryan;

import battlecode.common.*;


public class Towers extends RobotPlayer {
    //region Configuration
    static boolean spawnedFirstMopper = false;
    static int nonGreedyPhase = (int)(mx * 2);  // allow other units to complete ruins / upgrade towers if money capped
    static int firstMopper = (int)(mx * 2);
    static int splasherPhase = (int)(mx * 1.5);  // Start spawning splashers earlier than moppers
    //endregion

    public static void run() throws GameActionException {
        debugging();
        handleUnitSpawning();
        performCombatActions();
        handlePaintTransfer();
    }

    private static void debugging() throws GameActionException {
        for (int i = 0; i < 4; i++) {
            rc.setIndicatorDot(quadrantCenters[i], 255, 255, 255);
        }
        if (rc.getRoundNum() <= 1 && rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
            System.out.println("Number of towers " + rc.getNumberTowers());
            System.out.println("Siege phase " + siegePhase);
            System.out.println("Mopper phase " + mopperPhase);
            System.out.println("Mopper phase " + splasherPhase);
        }
    }

    //region Unit Spawning
    private static void handleUnitSpawning() throws GameActionException {
        MapLocation spawnLoc = findSpawnLocation();
        if (spawnLoc == null) return;

        UnitType unitToSpawn = chooseUnitType();

        if (checkResourcesAndSpawn(unitToSpawn, spawnLoc)) {
            numSpawnedUnits++;
        }
    }

    private static MapLocation findSpawnLocation() throws GameActionException {
        int tries = 8;
        Direction dir = directions[rng.nextInt(8)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // Double distance for cardinal directions
        if (dir == Direction.NORTH || dir == Direction.SOUTH || dir == Direction.EAST || dir == Direction.WEST)
            nextLoc = nextLoc.add(dir);

        // Keep trying new directions if location is occupied
        while (rc.canSenseRobotAtLocation(nextLoc) && tries-- > 0) {
            dir = directions[rng.nextInt(8)];
            nextLoc = rc.getLocation().add(dir);
            if (dir == Direction.NORTH || dir == Direction.SOUTH || dir == Direction.EAST || dir == Direction.WEST)
                nextLoc = nextLoc.add(dir);
        }
        if (tries == 0) {
            System.out.println("Tower: no empty spaces left in 8 main tiles!");
            return null;
        }
        return nextLoc;
    }

    private static UnitType chooseUnitType() {
        int r = rng.nextInt(100);
        rc.setIndicatorString("RNG: " + r);

        int splasherSpawnPercent = 0;
        int mopperSpawnPercent = 0;
        if (rc.getRoundNum() >= splasherPhase) {
            splasherSpawnPercent = 25;
        }
        if (rc.getRoundNum() >= mopperPhase) {
            mopperSpawnPercent = 20;
        }

        // Special case: First mopper spawn
        if (turnsAlive > 5 && rc.getRoundNum() > firstMopper && !spawnedFirstMopper && rc.getMoney() > reserveChips) {
            return UnitType.MOPPER;
        }

        // Normal unit selection
        if (r < splasherSpawnPercent) {
            return UnitType.SPLASHER;
        } else if (r < splasherSpawnPercent + mopperSpawnPercent) {
            return UnitType.MOPPER;
        }
        return UnitType.SOLDIER;
    }

    private static boolean checkResourcesAndSpawn(UnitType spawn, MapLocation loc) throws GameActionException {
        boolean hasResources = (rc.getRoundNum() < nonGreedyPhase || rc.getMoney() > 2000)
            && rc.getMoney() - spawn.moneyCost >= reserveChips
            && (rc.getPaint() - spawn.paintCost >= reservePaint 
               || rc.getRoundNum() < reservePaintPhase 
               || rc.getType().getBaseType() != UnitType.LEVEL_ONE_PAINT_TOWER);

        if (hasResources && rc.canBuildRobot(spawn, loc)) {
            rc.buildRobot(spawn, loc);
            if (spawn == UnitType.MOPPER && !spawnedFirstMopper) {
                spawnedFirstMopper = true;
            }
            return true;
        }
        return false;
    }
    //endregion

    /**
     * Always perform AOE attack
     * Attacks enemy units within range.
     */
    private static void performCombatActions() throws GameActionException {
        rc.attack(null);
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot  : robots) {
            if (robot.getTeam() != rc.getTeam()) {
                if (rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                    return;
                }
            }
        }
    }

    /**
     * Transfers paint to nearby friendly units up to their capacity.
     */
    private static void handlePaintTransfer() throws GameActionException {
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
