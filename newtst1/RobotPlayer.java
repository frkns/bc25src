package newtst1;

import battlecode.common.*;

import java.util.Random;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {
    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    public static int[] moneyHistory = new int[5];
    public static MapLocation[] locationHistory = new MapLocation[8];

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random();

    /** Array containing all the possible movement directions. */
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

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")

    public static int phase2;
    public static int phase3;
    public static int nextBot = 1;

    public static boolean rusher = false;
    public static int birthRound;
    public static boolean rushTowerDestroyed = false;
    public static MapLocation CENTER;

    static MapLocation spawnTowerLocation;
    static UnitType spawnTowerType;

    static RobotController rc;
    static int roundNum;

    public static void run(RobotController r) throws GameActionException {
        rc = r;

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
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
        phase2 = (int)((((rc.getMapHeight()+rc.getMapWidth())/2) * 2));
        phase3 = (int)((((rc.getMapHeight()+rc.getMapWidth())/2) * 5.775));
        System.out.println("Phase 2: " + phase2);
        System.out.println("Phase 3: " + phase3);

        Utils.init(rc);
        AttackBase.init(rc);
        Pathfinder.init(rc);
        birthRound = rc.getRoundNum();
        CENTER = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);

        while (true) {
            roundNum = rc.getRoundNum();
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // if (birthRound <= 4 && rc.getType() == UnitType.SOLDIER) {
                //     rusher = true;
                //     assert(!rc.getType().isTowerType());
                //     AttackBase.run();
                // } else
                switch (rc.getType()) {
                    case SOLDIER: runSoldier(rc); break;
                    case MOPPER: runMopper(rc); break;
                    case SPLASHER: runSplasher(rc); // Consider upgrading examplefuncsplayer to use splashers!
                    default: runTower(rc); break;
                }

                // update stuff
                moneyHistory[rc.getRoundNum() % moneyHistory.length] = rc.getMoney();
                locationHistory[rc.getRoundNum() % locationHistory.length] = rc.getLocation();


            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                if (roundNum != rc.getRoundNum())
                    System.out.println("~~~ Went over bytecode limit!! ~~~");
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for towers.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runTower(RobotController rc) throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        // Pick a direction to build in.
        // if (rc.getRoundNum() <= 4) {
        //     dir = rc.getLocation().directionTo(CENTER);
        //     nextLoc = rc.getLocation().add(dir);
        //     if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)) {
        //         rc.buildRobot(UnitType.SOLDIER, nextLoc);
        //     }
        // }

        // rc.setIndicatorDot(nextLoc, 255, 0, 0);

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

        if(nextBot == -1) {
            if(rc.getRoundNum() < phase2) {
                nextBot = 1;
            } else if(rc.getRoundNum() < phase3) {
                if(r > 30) {
                    nextBot = 1;
                } else {
                    nextBot = 0;
                }
            } else {
                if(r > 55) {
                    nextBot = 1;
                } else if (r > 40){
                    // nextBot = 0;
                    nextBot = 2;
                } else {
                    nextBot = 2;
                }
            }
        }

        if(nextBot == 1) {
            if(rc.canBuildRobot(UnitType.SOLDIER,nextLoc)) {
                rc.buildRobot(UnitType.SOLDIER,nextLoc);
                nextBot = -1;
            }
        }
        if(nextBot == 0) {
            if(rc.canBuildRobot(UnitType.MOPPER,nextLoc)) {
                rc.buildRobot(UnitType.MOPPER,nextLoc);
                nextBot = -1;
            }
        }
        if(nextBot == 2) {
            if(rc.canBuildRobot(UnitType.SPLASHER,nextLoc)) {
                rc.buildRobot(UnitType.SPLASHER,nextLoc);
                nextBot = -1;
            }
        }


        // Pick a random robot type to build.
        /*
        int robotType = rng.nextInt(3);
        if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }
        else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            System.out.println("BUILT A MOPPER");
        }
        else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            // rc.buildRobot(UnitType.SPLASHER, nextLoc);
            // System.out.println("BUILT A SPLASHER");
            rc.setIndicatorString("SPLASHER NOT IMPLEMENTED YET");
        }

        */

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

        // TODO: can we attack other bots?
    }


    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException {

        // emphemeral Sprint 1 code
        // boolean diff = false;
        // for (int i = 1; i < moneyHistory.length; i++) {
        //     if (moneyHistory[i] != moneyHistory[0]) {
        //         diff = true;
        //         break;
        //     }
        // }
        // if ((rc.getRoundNum() > 20 && !diff) || (rusher && rushTowerDestroyed)) {
        //     // start painting everything so we can win the game
        //     Sprint1.runSprint1(rc);
        //     return;
        // }

        if(rc.getRoundNum() < phase2) {
            Phase1.run(rc);
        } else if (rc.getRoundNum() < phase3) {
            Phase2.run(rc);
        } else {
            Phase3.run(rc);
        }

        /*
        // Sense information about all visible nearby tiles.
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        // Search for a nearby ruin to complete.
        MapInfo curRuin = null;
        for (MapInfo tile : nearbyTiles){
            if (tile.hasRuin() && !rc.isLocationOccupied(tile.getMapLocation())){
                curRuin = tile;
            }
        }
        if (curRuin != null){
            rc.setIndicatorString("Ruin nearby");
            MapLocation targetLoc = curRuin.getMapLocation();
            Direction dir = rc.getLocation().directionTo(targetLoc);
            if (rc.canMove(dir))
                rc.move(dir);
            // Mark the pattern we need to draw to build a tower here if we haven't already.
            MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
            if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
                rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
                System.out.println("Trying to build a tower at " + targetLoc);
            }
            // Fill in any spots in the pattern with the appropriate paint.
            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                    if (rc.canAttack(patternTile.getMapLocation()))
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                }
            }
            // Complete the ruin if we can.
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
                rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + targetLoc + "!");
            }
        }

        // Move and attack randomly if no objective.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)){
            rc.move(dir);
        }
        // Try to paint beneath us as we walk to avoid paint penalties.
        // Avoiding wasting paint by re-painting our own tiles.
        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
            rc.attack(rc.getLocation());
        }


         */
    }


    /**
     * Run a single turn for a Mopper.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runMopper(RobotController rc) throws GameActionException{
        Moppers.run(rc);
    }

    public static void runSplasher(RobotController rc) throws GameActionException{
        Splashers.run(rc);
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
