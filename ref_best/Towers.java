package ref_best;

import battlecode.common.*;


public class Towers extends RobotPlayer {
    //region Configuration
    static boolean spawnedFirstMopper = false;

    //endregion

    public static void run() throws GameActionException {
        debugging();
        Communication.readMessages();
        handleUnitSpawning();
        performCombatActions();
        handlePaintTransfer();
        sendMessages();
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


    private static void handleUnitSpawning() throws GameActionException {
        MapLocation spawnLoc = findSpawnLocation();
        if (spawnLoc == null) return;

        UnitType unitToSpawn = chooseUnitType();

        if (checkResourcesAndSpawn(unitToSpawn, spawnLoc)) {
            numSpawnedUnits++;
        }
    }

    private static MapLocation findSpawnLocation() throws GameActionException {
        // bytecode efficient way of locating where to spawn
        MapLocation loc = rc.getLocation();
        int pos = rng.nextInt(8);
        boolean[] blocked = new boolean[8];
        MapLocation nextLoc = null;
        /*
        0 North
        1 Northeast
        2 East
        3 Southeast
        4 South
        5 Southwest
        6 West
        7 Northwest
         */

        // spawns units away from walls
        // looks ugly but trust me it works
        if(loc.y <= 6) {blocked[4] = true;} else {blocked[4] = false;}
        if(loc.y >= mapHeight - 7) {blocked[0] = true;} else {blocked[0] = false;}
        if(loc.x <= 6) {blocked[6] = true;} else {blocked[6] = false;}
        if(loc.x >= mapWidth - 7) {blocked[2] = true;} else {blocked[2] = false;}
        if(blocked[0] && blocked[2]) {blocked[1] = true;} else {blocked[1] = false;}
        if(blocked[2] && blocked[4]) {blocked[3] = true;} else {blocked[3] = false;}
        if(blocked[4] && blocked[6]) {blocked[5] = true;} else {blocked[5] = false;}
        if(blocked[6] && blocked[0]) {blocked[7] = true;} else {blocked[7] = false;}


        //returns a valid location
        int count = 0;
        while(count < 8) {
            if(pos >= 8) {
                pos = 0;
            }
            if(!blocked[pos]) {
                Direction direction = directions[pos];
                nextLoc = rc.getLocation().add(direction);
                if(direction == Direction.NORTH || direction == Direction.SOUTH || direction == Direction.EAST || direction == Direction.WEST) {
                    nextLoc = nextLoc.add(direction);
                }
                if(!rc.isLocationOccupied(nextLoc)) {
                    break;
                }
            }

            pos++;
            count++;
        }
        if(nextLoc == null) {
            return null;
        }
        rc.setIndicatorString("Spawn Loc: " + nextLoc);
        return nextLoc;
    }

    private static UnitType chooseUnitType() {
        int r = rng.nextInt(100);
        //rc.setIndicatorString("RNG: " + r);

        int splasherSpawnPercent = 0;
        int mopperSpawnPercent = 0;
        if (rc.getRoundNum() >= splasherPhase) {
            splasherSpawnPercent = 20;
        }
        if (rc.getRoundNum() >= mopperPhase) {
            if (rc.getRoundNum() >= siegePhase){
                mopperSpawnPercent = 40;
            } else {
                mopperSpawnPercent = 20;
            }
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

    private static void sendMessages() throws GameActionException {
        if (targetEnemyTower != null) {
            for (RobotInfo robot : nearbyRobots) {
                if (robot.getTeam() == rc.getTeam() && robot.getType().isRobotType()) {
                    Communication.sendLocationMessage(robot.getID(), 0, targetEnemyTower);
                }
            }
            rc.setIndicatorLine(rc.getLocation(), targetEnemyTower, 255, 0, 0);
            if (rc.canBroadcastMessage()) {
                rc.broadcastMessage(Communication.HEADER_LOCATION + (0 << 12) + Communication.locToInt(targetEnemyTower));
            }
    }
    }
}
