package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import battlecode.common.*;

public class ActionAttackSwing extends RobotPlayer {
    static Direction[] DIRECTIONS = Direction.DIRECTION_ORDER;
    static Direction[] DIRECTIONS_ATTACK = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    static char[] scores;
    static Direction bestDirMovement;
    static Direction bestDirAttack;
    static int maxTarget;
    static RobotController rc;

    //------------------------------------------------------------------------------//
    // Update function
    //------------------------------------------------------------------------------//
    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case RobotPlayer.Action.ACTION_ATTACK_SWING:
            case RobotPlayer.Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        scores = "\u0000".repeat(49).toCharArray();
        rc = RobotPlayer.rc;
        bestDirMovement = null;
        bestDirAttack = null;
        maxTarget = 0;

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        // Check if enemy
        if (enemies.length == 0) {
            Debug.println("\tX - ACTION_ATTACK_SWING  : No enemy");
            action = RobotPlayer.Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Update score
        for (RobotInfo enemy : enemies) {
            Debug.println("\t\t\tUpdating enemy at : " + enemy.location);
            addMoperScore(enemy.location);
        }

        // Update max
        for (int iDirectionMovement = 0; iDirectionMovement < 9; iDirectionMovement++) {
            if (!rc.canMove(DIRECTIONS[iDirectionMovement])) {
                continue;
            }

            MapLocation loc = rc.getLocation().add(DIRECTIONS[iDirectionMovement]);
            MapInfo info = rc.senseMapInfo(loc);

            int coef = 1;
            int baseScore = 0;
            switch (info.getPaint()){
                case PaintType.ALLY_PRIMARY:
                case PaintType.ALLY_SECONDARY:
                    baseScore += 20;
                    break;

                case PaintType.EMPTY:
                    baseScore += 10;
                    break;

                case PaintType.ENEMY_PRIMARY:
                case PaintType.ENEMY_SECONDARY:
                    coef = 2;
            };
            // Not 80 because one of the robot is current bot.
            baseScore += 90 - (rc.senseNearbyRobots(loc, 2, rc.getTeam()).length * coef);

            for (int iDirectionAttack = 0; iDirectionAttack < 5; iDirectionAttack++) {
                int id = iDirectionMovement + iDirectionAttack * 10;
                int score = scores[id] + baseScore;

                if (score > maxTarget) {
                    // Don't check for mop swing, if we have score != 0, it means we can.
                    maxTarget = score;
                    bestDirMovement = DIRECTIONS[iDirectionMovement];
                    bestDirAttack = DIRECTIONS_ATTACK[iDirectionAttack];
                }
            }
        }

        // Check for target
        if (bestDirMovement == null || bestDirAttack == null) {
            Debug.println("\tX - ACTION_ATTACK_SWING  : No best Direction");
            action = RobotPlayer.Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        action = Action.ACTION_ATTACK_SWING;
        Debug.println("\t0 - ACTION_ATTACK_SWING  : Playing! ");
        Debug.println("\t\tMove " + bestDirMovement.name() + " and swing " + bestDirAttack.name());

        rc.move(bestDirMovement);

        // Check if action ready
        if (rc.isActionReady()) {
            rc.mopSwing(bestDirAttack);
        }else{
            Debug.println("\tW - ACTION_ATTACK_SWING  : not action ready, keep moving in direction of enemy.");
        }
    }
    public static void addMoperScore(MapLocation loc){
        MapLocation myLoc = RobotPlayer.rc.getLocation();
        int shift = (loc.x - myLoc.x) + (loc.y - myLoc.y) * 1000;
        switch(shift){
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
