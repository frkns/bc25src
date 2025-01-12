package smallrewrite;

import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {
    static int turnCount = 0;
    public static int[] moneyHistory = new int[5];
    public static MapLocation[] locationHistory = new MapLocation[8];
    static final Random rng = new Random();

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


    public static int phase2;
    public static int phase3;
    public static int nextBot = 1;

    public static MapLocation CENTER;

    static MapLocation spawnTowerLocation;
    static UnitType spawnTowerType;

    static RobotController rc;
    static int roundNum;

    static RobotInfo[] nearbyRobots;

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        nearbyRobots = rc.senseNearbyRobots();

        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                if (robot.getType().isTowerType()) {
                    if (spawnTowerLocation == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc.getLocation().distanceSquaredTo(spawnTowerLocation)) {
                        spawnTowerLocation = robot.getLocation();
                        spawnTowerType = robot.getType().getBaseType();
                    }
                }
            }
        }
        // phase2 = (int)((((rc.getMapHeight()+rc.getMapWidth())/2) * 2));
        // phase3 = (int)((((rc.getMapHeight()+rc.getMapWidth())/2) * 5.775));
        // System.out.println("Phase 2: " + phase2);
        // System.out.println("Phase 3: " + phase3);

        CENTER = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);

        while (true) {
            roundNum = rc.getRoundNum();
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            try {
                switch (rc.getType()) {
                    case SOLDIER: runSoldier(); break;
                    case MOPPER: runMopper(); break;
                    case SPLASHER: runSplasher(); // Consider upgrading examplefuncsplayer to use splashers!
                    default: runTower(); break;
                }

                // update stuff
                moneyHistory[rc.getRoundNum() % moneyHistory.length] = rc.getMoney();
                locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();


            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                if (roundNum != rc.getRoundNum())
                    System.out.println("~~~ Went over bytecode limit!! ~~~");
                Clock.yield();
            }
        }
    }

    public static void runTower() throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);


        RobotInfo[] robots = rc.senseNearbyRobots();


        for (RobotInfo robot  : robots ) {
            if (robot.getTeam() == rc.getTeam()) {
                // our team
            }
            else {
                // their team
                if(rc.canAttack(robot.getLocation())) {
                    rc.attack(robot.getLocation());
                }
            }
        }
        int r = rng.nextInt(100);

        if (rc.getRoundNum() < 150) {
            if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                rc.buildRobot(UnitType.SOLDIER, nextLoc);
            }
        } else {
            if (r < 70) {
                if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
                    rc.buildRobot(UnitType.SOLDIER, nextLoc);
                }
            } else {
                if (rc.canBuildRobot(UnitType.MOPPER, nextLoc)) {
                    rc.buildRobot(UnitType.MOPPER, nextLoc);
                }
            }
        }
    }


    public static void runSoldier() throws GameActionException {
        Phase1.run();
    }

    public static void runMopper() throws GameActionException{
        Moppers.run();
    }

    public static void runSplasher() throws GameActionException{
        Splashers.run();
    }

}
