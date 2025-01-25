package kenny_version_lmx;

import battlecode.common.*;


public class Moppers extends RobotPlayer {

    // Actions
    static MapLocation target;
    static MapLocation bestAttackTarget = null;
    static int bestAttackScore = 0;
    static Direction bestSwingMove = null;
    static Direction bestSwingDir = null;
    static int bestSwingScore = 0;

    // Swings utils
    static char[] scores; // Score for each (moveDirection, attackDirection)
    static Direction[] DIRECTIONS = Direction.DIRECTION_ORDER;
    static Direction[] DIRECTIONS_ATTACK = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    // how long of not being able to reach target till we change it?
    static int targetChangeWaitTime = mx;
    static int lastTargetChangeRound = 0;
    static MapLocation nearestEnemyPaintOnRuin;

    // should we stand in place this turn? resets to false at the end of every turn
    static boolean mopperStand = false; // unused at the moment

    // experimental
    // static int stopQuadrantModifierPhase = mx * 2; // pending deletion?
    /* */

    public static void run() throws GameActionException {
        Debug.println("Running.");
        rc.setIndicatorString("");


        // Update
        Debug.println("\tUpdating :");
        ImpureUtils.tryUpgradeNearbyTowers();
        ImpureUtils.updateNearbyMask(true);
        ImpureUtils.updateNearestEnemyTower();
        ImpureUtils.updateNearestEnemyPaint();
        ImpureUtils.updateNearestEnemyPaintOnRuin();
        ImpureUtils.updateNearestEnemyRobot();



        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            // selecting a random target location on the map has an inherent bias towards
            // the center if e.g. we are in a corner
            // this is more of a problem on big maps
            // try to combat this but also instead sometimes selecting a location in our
            // current quadrant
            /*
             * if (rc.getRoundNum() % 2 == 0 && rc.getRoundNum() <
             * stopQuadrantModifierPhase)
             * target = Utils.randomLocationInQuadrant(Utils.currentQuadrant());
             * else
             */
            {
                target = Utils.randomLocationInQuadrant(rng.nextInt(4));
                Debug.println("\t\texplore target is now " + target);
            }
            lastTargetChangeRound = rc.getRoundNum();
        }

        // Update refill
        if (nearestPaintTower != null) {
            ImpureUtils.withdrawPaintIfPossible(nearestPaintTower);
        }
        isRefilling = rc.getPaint() < 40;
        if (isRefilling && nearestPaintTower != null) {
            Pathfinder.move(nearestPaintTower);
        }

        // mop vs mop swing relative scoring logic
        // let's suppose that removing enemy paint from a tile has a value of ~10 paint

        // mop (10 action cooldown):
        // * enemy robot mop = -10 enemy and +5 mopper, net = +15 = 15
        // * enemy robot on enemy paint = +15 + 10 = 25
        // * enemy paint = 0 + 10 = 10
        // mop swing (20 action cooldown):
        // * can hit up to 6 and remove 5 from each for a max of +30, but has double
        // action cooldown

        // also, we should take into account the removing of enemy paint from a ruin
        // scores is in paint multiplied by 10 for easy calcs


        // update attack
        ImpureUtils.updateNearbyMask(true);
        _UpdateAttackAction();
        Debug.println("\t\tScore Attack : " + bestAttackScore + " on " + bestAttackTarget);

        _UpdateSwingAction();
        Debug.println("\t\tScore Swing  : " + bestSwingScore + " Moving on " + bestSwingMove + " attack on " + bestSwingDir);


        Debug.println("\tPlaying ");
        if (bestSwingDir != null && bestSwingMove != null && bestSwingScore >= bestAttackScore) {
            Debug.println("\t\tSwinging - Moving " + bestSwingMove.name() + " Swing " + bestSwingDir.name());
            rc.setIndicatorString("Swinging - Moving " + bestSwingMove.name() + " Swing " + bestSwingDir.name());
            rc.move(bestSwingMove);
            rc.mopSwing(bestSwingDir);
            _Move(); // BestSwingMove can be DIRECTION_CENTER

        } else {

            _Move();
            _UpdateAttackAction();
            Debug.println("\t\tUpdated Attack: " + bestAttackScore + " on " + bestAttackTarget);

            if (bestAttackTarget != null) {
                Debug.println("\t\tAttack on " + bestAttackTarget);
                rc.setIndicatorString("Attack");
                rc.attack(bestAttackTarget);
            }
        }


        // try to transfer paint if we are filled
        if (rc.getPaint() > 90) {

            bestAttackScore = 0;
            MapLocation bestLoc = null;
            int bestTransferAmt = 0;

            RobotInfo[] adjRobots = rc.senseNearbyRobots(2);
            for (RobotInfo robot : adjRobots) {
                if (robot.getTeam() == rc.getTeam()) {
                    int score = 0;
                    int transferAmt = Math.min(50, robot.getType().paintCapacity - robot.getPaintAmount());
                    score += transferAmt;
                    if (robot.getType() == UnitType.SPLASHER) {
                        score += 15;
                    }
                    if (score > bestAttackScore) {
                        bestAttackScore = score;
                        bestLoc = robot.getLocation();
                        bestTransferAmt = transferAmt;
                    }
                }
            }

            if (bestAttackScore > 0) {
                Debug.println("\tTransfer " + bestTransferAmt + " paint " + bestLoc);

                if (rc.canTransferPaint(bestLoc, bestTransferAmt)) {
                    rc.transferPaint(bestLoc, bestTransferAmt);
                }
            }
        }

        Debug.println("End of turn.");
        Debug.println("");
        Debug.println("");

    }

    //------------------------------------------------------------------------------//
    // Internal Functions
    //------------------------------------------------------------------------------//
    static void _Move() throws GameActionException {
        if (rc.isMovementReady()) {
            MapLocation towerLoc = Utils.chooseTowerTarget();
            if (towerLoc != null && rc.getNumberTowers() > 8 && rc.getID() % 10 < 2 && rc.isActionReady()) {
                Debug.println("\t\tMove tower target " + towerLoc);
                Pathfinder.move(towerLoc);

            } else {
                Debug.println("\t\tMove explore target " + target);
                HeuristicPath.mopperMove(target);

            }
        }
    }

    static void _UpdateSwingAction() throws GameActionException {
        bestSwingScore = 0;
        bestSwingDir = null;
        bestSwingMove = null;
        scores = "\u0000".repeat(49).toCharArray();

        if(!rc.isActionReady()){
            return;
        }

        // Update score
        for (RobotInfo enemy : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
            Debug.println("\t\tEnemie detected at " + enemy.getLocation());
            _addSwingScore(enemy.location);
        }

        // Get the max value
        for (int iDirectionMovement = 0; iDirectionMovement < 9; iDirectionMovement++) {
            if (!rc.canMove(DIRECTIONS[iDirectionMovement])) {
                continue;
            }

            MapLocation loc = rc.getLocation().add(DIRECTIONS[iDirectionMovement]);
            MapInfo info = rc.senseMapInfo(loc);

            // Calculate paint penalty for the movement
            int coef = 10; // Number of paint lost if nearby ally units
            int baseScore = 0;
            switch (info.getPaint()) {
                case PaintType.ALLY_PRIMARY:
                case PaintType.ALLY_SECONDARY:
                    // Don't lost paint
                    break;

                case PaintType.EMPTY:
                    baseScore -= 10;
                    break;

                case PaintType.ENEMY_PRIMARY:
                case PaintType.ENEMY_SECONDARY:
                    baseScore -= 20;
                    coef = 20;
            }
            ;
            baseScore += rc.senseNearbyRobots(loc, 2, rc.getTeam()).length * coef;

            for (int iDirectionAttack = 0; iDirectionAttack < 5; iDirectionAttack++) {
                int id = iDirectionMovement + iDirectionAttack * 10;
                int score = scores[id];

                if(score == 0){
                    break;
                }

                score += baseScore;

                if (score > bestSwingScore) {
                    bestSwingScore = score;
                    bestSwingMove = DIRECTIONS[iDirectionMovement];
                    bestSwingDir = DIRECTIONS_ATTACK[iDirectionAttack];
                }
            }
        }
    }

    static void _UpdateAttackAction() throws GameActionException {
        bestAttackTarget = null;
        bestAttackScore = 0; // = number paint lost * 10

        if(!rc.isActionReady()){
            return;
        }

        MapInfo[] veryNearbyTiles = rc.senseNearbyMapInfos(2);
        for (MapInfo tile : veryNearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();

            if (!rc.canAttack(tileLoc))
                continue;

            int score = 0;
            boolean hasEnemyPaint = tile.getPaint().isEnemy();

            if (rc.canSenseRobotAtLocation(tileLoc)) {
                RobotInfo robot = rc.senseRobotAtLocation(tileLoc);
                if (robot.getTeam() != rc.getTeam()) {
                    if (robot.getPaintAmount() > 0 && rc.getPaint() < 100) {
                        score += 150;  // this tile has an enemy robot with paint
                        // = -100 for enemy, + 50 for mopper
                    }
                }
            }

            if (hasEnemyPaint) {
                score += 100; // this tile has enemy paint there
                if (tileLoc.equals(nearestEnemyPaintOnRuin)) {
                    score += 130; // this tile is also part of a ruin
                }
            }

            if (score > bestAttackScore) {
                bestAttackScore = score;
                bestAttackTarget = tileLoc;
            }
        }
    }

    //------------------------------------------------------------------------------//
    // Unwrapped update score
    //------------------------------------------------------------------------------//
    // Score is 50 (= paint remove * 10) + small score if the attack is near enemie to keep them at range

    public static void _addSwingScore(MapLocation loc) {
        MapLocation myLoc = RobotPlayer.rc.getLocation();
        int shift = (loc.x - myLoc.x) + (loc.y - myLoc.y) * 1000;
        switch (shift) {
            case -2003:
                scores[38] += 58;
                break;
            case -1003:
                scores[31] += 58;
                scores[38] += 57;
                break;
            case -3:
                scores[31] += 57;
                scores[32] += 58;
                scores[38] += 56;
                break;
            case 997:
                scores[31] += 56;
                scores[32] += 57;
                break;
            case 1997:
                scores[32] += 56;
                break;
            case -3002:
                scores[28] += 58;
                break;
            case -2002:
                scores[21] += 58;
                scores[37] += 58;
                scores[28] += 57;
                scores[38] += 57;
                break;
            case -1002:
                scores[30] += 58;
                scores[21] += 57;
                scores[31] += 57;
                scores[22] += 58;
                scores[37] += 57;
                scores[38] += 56;
                break;
            case -2:
                scores[30] += 57;
                scores[31] += 56;
                scores[22] += 57;
                scores[32] += 57;
                scores[33] += 58;
                scores[37] += 56;
                scores[8] += 55;
                scores[38] += 55;
                break;
            case 998:
                scores[30] += 56;
                scores[1] += 55;
                scores[31] += 55;
                scores[32] += 56;
                scores[33] += 57;
                scores[8] += 54;
                break;
            case 1998:
                scores[1] += 54;
                scores[2] += 55;
                scores[32] += 55;
                scores[33] += 56;
                break;
            case 2998:
                scores[2] += 54;
                break;
            case -3001:
                scores[27] += 58;
                scores[28] += 57;
                break;
            case -2001:
                scores[20] += 58;
                scores[21] += 57;
                scores[36] += 58;
                scores[27] += 57;
                scores[37] += 57;
                scores[28] += 56;
                break;
            case -1001:
                scores[20] += 57;
                scores[30] += 57;
                scores[21] += 56;
                scores[22] += 57;
                scores[23] += 58;
                scores[35] += 58;
                scores[36] += 57;
                scores[37] += 56;
                break;
            case -1:
                scores[30] += 56;
                scores[22] += 56;
                scores[23] += 57;
                scores[33] += 57;
                scores[34] += 58;
                scores[35] += 57;
                scores[36] += 56;
                scores[7] += 55;
                scores[37] += 55;
                scores[8] += 54;
                break;
            case 999:
                scores[0] += 55;
                scores[30] += 55;
                scores[1] += 54;
                scores[33] += 56;
                scores[34] += 57;
                scores[35] += 56;
                scores[7] += 54;
                scores[8] += 53;
                break;
            case 1999:
                scores[0] += 54;
                scores[1] += 53;
                scores[2] += 54;
                scores[3] += 55;
                scores[33] += 55;
                scores[34] += 56;
                break;
            case 2999:
                scores[2] += 53;
                scores[3] += 54;
                break;
            case -3000:
                scores[26] += 58;
                scores[27] += 57;
                scores[28] += 56;
                break;
            case -2000:
                scores[20] += 57;
                scores[21] += 56;
                scores[25] += 58;
                scores[26] += 57;
                scores[36] += 57;
                scores[27] += 56;
                scores[18] += 55;
                scores[28] += 55;
                break;
            case -1000:
                scores[20] += 56;
                scores[11] += 55;
                scores[21] += 55;
                scores[22] += 56;
                scores[23] += 57;
                scores[24] += 58;
                scores[25] += 57;
                scores[35] += 57;
                scores[36] += 56;
                scores[18] += 54;
                break;
            case 0:
                scores[11] += 54;
                scores[12] += 55;
                scores[22] += 55;
                scores[23] += 56;
                scores[24] += 57;
                scores[34] += 57;
                scores[35] += 56;
                scores[6] += 55;
                scores[36] += 55;
                scores[7] += 54;
                scores[8] += 53;
                scores[18] += 53;
                break;
            case 1000:
                scores[0] += 54;
                scores[1] += 53;
                scores[11] += 53;
                scores[12] += 54;
                scores[34] += 56;
                scores[5] += 55;
                scores[35] += 55;
                scores[6] += 54;
                scores[7] += 53;
                scores[8] += 52;
                break;
            case 2000:
                scores[0] += 53;
                scores[1] += 52;
                scores[2] += 53;
                scores[12] += 53;
                scores[3] += 54;
                scores[4] += 55;
                scores[34] += 55;
                scores[5] += 54;
                break;
            case 3000:
                scores[2] += 52;
                scores[3] += 53;
                scores[4] += 54;
                break;
            case -2999:
                scores[26] += 57;
                scores[27] += 56;
                break;
            case -1999:
                scores[20] += 56;
                scores[25] += 57;
                scores[26] += 56;
                scores[17] += 55;
                scores[27] += 55;
                scores[18] += 54;
                break;
            case -999:
                scores[10] += 55;
                scores[20] += 55;
                scores[11] += 54;
                scores[23] += 56;
                scores[24] += 57;
                scores[25] += 56;
                scores[17] += 54;
                scores[18] += 53;
                break;
            case 1:
                scores[10] += 54;
                scores[11] += 53;
                scores[12] += 54;
                scores[13] += 55;
                scores[23] += 55;
                scores[24] += 56;
                scores[6] += 54;
                scores[7] += 53;
                scores[17] += 53;
                scores[18] += 52;
                break;
            case 1001:
                scores[0] += 53;
                scores[10] += 53;
                scores[11] += 52;
                scores[12] += 53;
                scores[13] += 54;
                scores[5] += 54;
                scores[6] += 53;
                scores[7] += 52;
                break;
            case 2001:
                scores[0] += 52;
                scores[12] += 52;
                scores[3] += 53;
                scores[13] += 53;
                scores[4] += 54;
                scores[5] += 53;
                break;
            case 3001:
                scores[3] += 52;
                scores[4] += 53;
                break;
            case -2998:
                scores[26] += 56;
                break;
            case -1998:
                scores[25] += 56;
                scores[16] += 55;
                scores[26] += 55;
                scores[17] += 54;
                break;
            case -998:
                scores[10] += 54;
                scores[24] += 56;
                scores[15] += 55;
                scores[25] += 55;
                scores[16] += 54;
                scores[17] += 53;
                break;
            case 2:
                scores[10] += 53;
                scores[13] += 54;
                scores[14] += 55;
                scores[24] += 55;
                scores[15] += 54;
                scores[6] += 53;
                scores[16] += 53;
                scores[17] += 52;
                break;
            case 1002:
                scores[10] += 52;
                scores[13] += 53;
                scores[14] += 54;
                scores[5] += 53;
                scores[15] += 53;
                scores[6] += 52;
                break;
            case 2002:
                scores[13] += 52;
                scores[4] += 53;
                scores[14] += 53;
                scores[5] += 52;
                break;
            case 3002:
                scores[4] += 52;
                break;
            case -1997:
                scores[16] += 54;
                break;
            case -997:
                scores[15] += 54;
                scores[16] += 53;
                break;
            case 3:
                scores[14] += 54;
                scores[15] += 53;
                scores[16] += 52;
                break;
            case 1003:
                scores[14] += 53;
                scores[15] += 52;
                break;
            case 2003:
                scores[14] += 52;
                break;
        }
    }
}


