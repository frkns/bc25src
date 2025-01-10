package kenny;

import battlecode.common.*;

import java.util.Random;


public class RobotPlayer {

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
    static int turnCount = 0;

    static boolean paintEverywhere = false;

    static int phase = 1;

    public static void run(RobotController rc) throws GameActionException {

        // phase2 = (int)((((rc.getMapHeight()+rc.getMapWidth())/2) * 1.7) - 20.5);
        // phase3 = (int)((((rc.getMapHeight()+rc.getMapWidth())/2) * 4.475) - 60.5);
        phase2 = 50;
        phase3 = 444;

        Tower.init(rc);
        Soldier.init(rc);
        Utils.init(rc);
        Pathfinder.init(rc);
        Debug.init(rc);

        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            if (rc.getRoundNum() < phase2) {
                phase = 1;
            } else if (rc.getRoundNum() < phase3) {
                phase = 2;
            } else {
                phase = 3;
            }
            if (rc.getRoundNum() > 300) {
                paintEverywhere = true;
            }

            try {
                switch (rc.getType()) {
                    case SOLDIER: Soldier.runSoldier(); break;
                    case MOPPER: runMopper(rc); break;
                    case SPLASHER: Splasher.run(rc); // Consider upgrading examplefuncsplayer to use splashers!
                    default: Tower.runTower(); break;
                    }
                }
             catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }
    }




    public static void runSoldier(RobotController rc) throws GameActionException{
    }

    public static void runMopper(RobotController rc) throws GameActionException{
    }

    public static void runSplasher(RobotController rc) throws GameActionException{
    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for possible future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.
            if (rc.getRoundNum() % 20 == 0){
                for (RobotInfo ally : allyRobots){
                    if (rc.canSendMessage(ally.location, enemyRobots.length)){
                        rc.sendMessage(ally.location, enemyRobots.length);
                    }
                }
            }
        }
    }
}
