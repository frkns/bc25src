package ref_best;

import battlecode.common.*;

public class Towers extends RobotPlayer {

    static boolean spawnedFirstMopper = false;

    static int nonGreedyPhase = (int)(mx * 2);  // not used
    static int firstMopper = (int)(mx);

    static int numSpawnedUnits = 0;

    // force the spawning of a unit if possible, bypassing reserve checks, resets to false end of round
    static boolean forceSpawn = false;

    static MapLocation fstEnemyTower;  // first target
    static boolean fstIsDefense;

    static MapLocation sndEnemyTower;
    static boolean sndIsDefense;

    static int msgUpdateRoundNum = -99;

    public static void readMessages(int round) throws GameActionException {
        Message[] receivedMsgs = rc.readMessages(round);
        for (Message msg : receivedMsgs) {
            int bits = msg.getBytes();

            int fst = (bits >> (21 - 1)) & 0xFFF;
            int snd = (bits >> (21 - 14)) & 0xFFF;
            MapLocation fstLoc = fst == 0 ? null : Comms.intToLoc(fst);
            MapLocation sndLoc = snd == 0 ? null : Comms.intToLoc(snd);
            boolean fstType = ((bits >> (32 - 13)) & 1) == 1;
            boolean sndType = ((bits >> (32 - 26)) & 1) == 1;

            // replace if greater distance, always update if old messages - actually since we're reading past 2 rounds, not required
            if (fstLoc != null)
            if (rc.getRoundNum() >= msgUpdateRoundNum + 11 || fstEnemyTower == null /*|| rc.getLocation().distanceSquaredTo(fstLoc) > rc.getLocation().distanceSquaredTo(fstEnemyTower) */&& fstLoc != sndEnemyTower) {
                fstEnemyTower = fstLoc;
                fstIsDefense = fstType;
                // msgUpdateRoundNum = rc.getRoundNum();
            }
            if (sndLoc != null)
            if (rc.getRoundNum() >= msgUpdateRoundNum + 11 || sndEnemyTower == null /*|| rc.getLocation().distanceSquaredTo(sndLoc) > rc.getLocation().distanceSquaredTo(sndEnemyTower) */&& sndLoc != fstEnemyTower) {
                sndEnemyTower = sndLoc;
                sndIsDefense = sndType;
                // msgUpdateRoundNum = rc.getRoundNum();
            }
        }
    }

    public static void sendMessages() throws GameActionException {
        assert(rc.canBroadcastMessage());

        // Comms
        // remake the message to include additional information if necessary
        int outgoingMsg = 0;

        if (fstEnemyTower == null) {
            // outgoingMsg |= 0xFFF << (21 - 1);  // sentinel value, just leave it to be 0
        } else {
            outgoingMsg |= Comms.locToInt(fstEnemyTower) << (21 - 1);
            outgoingMsg |= (fstIsDefense ? 1 : 0) << (32 - 13);
        }
        if (sndEnemyTower == null) {
            // outgoingMsg |= 0xFFF << (21 - 14);  // sentinel value
        } else {
            outgoingMsg |= Comms.locToInt(sndEnemyTower) << (21 - 14);
            outgoingMsg |= (sndIsDefense ? 1 : 0) << (32 - 26);
        }

        rc.broadcastMessage(outgoingMsg);
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                assert(robot.getType().isRobotType());

                MapLocation tileLoc = robot.getLocation();
                if (rc.canSendMessage(tileLoc)) {
                    rc.sendMessage(tileLoc, outgoingMsg);
                }
            }
        }
    }

    public static void run() throws GameActionException {
        readMessages(rc.getRoundNum() - 1);  // read last round's messages
        readMessages(rc.getRoundNum());      // read this round's messages

        // debugging stuff
        if (rc.getRoundNum() <= 1 && rc.getType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
            System.out.println("Number of towers " + rc.getNumberTowers());
            System.out.println("Siege phase " + siegePhase);
            System.out.println("Mopper phase " + mopperPhase);
        }
        if (fstEnemyTower != null) {
            rc.setIndicatorLine(rc.getLocation(), fstEnemyTower, 255, 255, 255);
        }
        if (sndEnemyTower != null) {
            rc.setIndicatorLine(rc.getLocation(), sndEnemyTower, 199, 199, 199);
        }
        /* */

        int nearbySoldiers = 0;  // allies
        int nearbyMoppers = 0;
        int nearbySplashers = 0;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                switch (robot.getType()) {
                    case SOLDIER:
                        nearbySoldiers++;
                        break;
                    case MOPPER:
                        nearbyMoppers++;
                        break;
                    case SPLASHER:
                        nearbySplashers++;
                        break;
                }
            }
        }



        ImpureUtils.updateNearestEnemyPaint();
        ImpureUtils.updateNearestEnemyRobot();

        int r = rng.nextInt(100);
        // rc.setIndicatorString("RNG: " + r);

        UnitType spawn = UnitType.SOLDIER;

        if (rc.getRoundNum() >= mopperPhase) {
            // if (rc.getRoundNum() % 5 == 0) {
            if (r < 60) {
                spawn = UnitType.MOPPER;
            }
        }

        if(rc.getRoundNum() >= splasherPhase) {
            if(r < 40) {
                spawn = UnitType.SPLASHER;
            }
        }

        if (turnsAlive > 2 && rc.getRoundNum() > firstMopper && !spawnedFirstMopper && rc.getMoney() > reserveChips) {
            spawn = UnitType.MOPPER;
        }

        if (nearestEnemyRobot != null && nearbyMoppers < 2) {
            // "clog will mog" reactionary mopper
            rc.setIndicatorString("there is a enemy robot nearby, spawning mopper");
            spawn = UnitType.MOPPER;
            forceSpawn = true;
        }

        // determine which tile to spawn this UnitType
        MapInfo[] nearbyDiamond = rc.senseNearbyMapInfos(4);
        int bestScore = (int) -2e9;
        MapLocation bestLoc = null;

        for (MapInfo tile : nearbyDiamond) {
            MapLocation tileLoc = tile.getMapLocation();
            // rc.setIndicatorDot(tileLoc, 255, 255, 0);
            if (rc.canSenseRobotAtLocation(tileLoc))  // can't spawn here
                continue;
            int score = 0;
            score += Math.min(tileLoc.x * 600, 6*600);  // wall avoidance
            score += Math.min((mapWidth - tileLoc.x) * 600, 7*600);
            score += Math.min(tileLoc.y * 600, 6*600);
            score += Math.min((mapHeight - tileLoc.y) * 600, 7*600);

            if (rc.getLocation().isWithinDistanceSquared(tileLoc, 1)) {
                score -= 500;  // add a cost for spawning closer
            }

            if (spawn == UnitType.MOPPER) {
                if (nearestEnemyPaint != null)
                    score -= Math.min(1, Utils.chessDistance(tileLoc, nearestEnemyPaint)) * 1500; // add a cost for
                                                                                                      // spawning
                                                                                                      // further from
                                                                                                      // enemy paint
                if (nearestEnemyRobot != null)
                    score -= Utils.chessDistance(tileLoc, nearestEnemyRobot) * 4000;    // add a cost for spawning
                                                                                        // further from enemy robot

                if (!tile.getPaint().isAlly()) {  // add cost if it's not our paint
                    score -= 1500;
                }

            }

            // score += rng.nextInt((int)(Math.abs(score) * 0.5 + 10));

            if (score > bestScore) {
                bestScore = score;
                bestLoc = tileLoc;
            }
        }

        MapLocation nextLoc = bestLoc;

        if (nextLoc != null)
        if (forceSpawn || numSpawnedUnits < 1 ||  // don't conserve resources if we haven't spawned one units yet
            true // (rc.getRoundNum() < nonGreedyPhase || rc.getMoney() > 2000)
            && rc.getMoney() - spawn.moneyCost >= reserveChips
            && (rc.getPaint() - spawn.paintCost >= reservePaint || rc.getRoundNum() < reservePaintPhase || rc.getType().getBaseType() != UnitType.LEVEL_ONE_PAINT_TOWER))
            // only reserve paint if we are a paint tower ^
        if (rc.canBuildRobot(spawn, nextLoc)) {
            rc.buildRobot(spawn, nextLoc);
            if (spawn == UnitType.MOPPER)
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
                    // can towers transfer paint?
                    assert(false);  // apparantely not? pending deletion
                    System.out.println("Tower transfered paint");
                    rc.transferPaint(robotLoc, transferAmt);
                }
            }
        }

        sendMessages();

        forceSpawn = false;
    }
}
