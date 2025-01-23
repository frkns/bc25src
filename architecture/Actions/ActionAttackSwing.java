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
        if(enemies.length == 0){
            Debug.println("\tX - ACTION_ATTACK_SWING  : No enemy");
            action = RobotPlayer.Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Update score
        for(RobotInfo enemy: enemies){
            addMoperScore(enemy.location);
        }

        // Update max
        for(int iDirectionMovement=0; iDirectionMovement<9; iDirectionMovement++){
            if(!rc.canMove(DIRECTIONS[iDirectionMovement])){
                continue;
            }

            for(int iDirectionAttack=0; iDirectionAttack<50; iDirectionAttack += 10){
                if(scores[iDirectionMovement + iDirectionAttack] > maxTarget && rc.canMove(DIRECTIONS_ATTACK[iDirectionAttack])){
                    maxTarget = scores[iDirectionMovement + iDirectionAttack];
                    bestDirMovement = DIRECTIONS[iDirectionMovement];
                    bestDirAttack = DIRECTIONS_ATTACK[iDirectionAttack];
                }
            }
        }

        // Check for target
        if(bestDirMovement == null || bestDirAttack == null){
            Debug.println("\tX - ACTION_ATTACK_SWING  : No best Direction");
            action = RobotPlayer.Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        action = Action.ACTION_ATTACK_SWING;
        Debug.println("\t0 - ACTION_ATTACK_SWING  : Playing! ");
        Debug.println("\t\tMove " + bestDirMovement.name() + " and swing " + bestDirAttack.name());

        rc.move(bestDirMovement);
        rc.mopSwing(bestDirAttack);
    }


    public static void addMoperScore(MapLocation loc) {
        MapLocation myLoc = rc.getLocation();
        int shift = (loc.x - myLoc.y) + (loc.y - myLoc.y) * 100;
        switch (shift) {
            case -123:
                scores[38]++;
                break;
            case -63:
                scores[31]++;
                scores[38]++;
                break;
            case -3:
                scores[31]++;
                scores[32]++;
                scores[38]++;
                break;
            case 57:
                scores[31]++;
                scores[32]++;
                break;
            case 117:
                scores[32]++;
                break;
            case -182:
                scores[28]++;
                break;
            case -122:
                scores[21]++;
                scores[37]++;
                scores[28]++;
                scores[38]++;
                break;
            case -62:
                scores[30]++;
                scores[21]++;
                scores[31]++;
                scores[22]++;
                scores[37]++;
                scores[38]++;
                break;
            case -2:
                scores[30]++;
                scores[31]++;
                scores[22]++;
                scores[32]++;
                scores[33]++;
                scores[37]++;
                scores[8]++;
                scores[38]++;
                break;
            case 58:
                scores[30]++;
                scores[1]++;
                scores[31]++;
                scores[32]++;
                scores[33]++;
                scores[8]++;
                break;
            case 118:
                scores[1]++;
                scores[2]++;
                scores[32]++;
                scores[33]++;
                break;
            case 178:
                scores[2]++;
                break;
            case -181:
                scores[27]++;
                scores[28]++;
                break;
            case -121:
                scores[20]++;
                scores[21]++;
                scores[36]++;
                scores[27]++;
                scores[37]++;
                scores[28]++;
                break;
            case -61:
                scores[20]++;
                scores[30]++;
                scores[21]++;
                scores[22]++;
                scores[23]++;
                scores[35]++;
                scores[36]++;
                scores[37]++;
                break;
            case -1:
                scores[30]++;
                scores[22]++;
                scores[23]++;
                scores[33]++;
                scores[34]++;
                scores[35]++;
                scores[36]++;
                scores[7]++;
                scores[37]++;
                scores[8]++;
                break;
            case 59:
                scores[0]++;
                scores[30]++;
                scores[1]++;
                scores[33]++;
                scores[34]++;
                scores[35]++;
                scores[7]++;
                scores[8]++;
                break;
            case 119:
                scores[0]++;
                scores[1]++;
                scores[2]++;
                scores[3]++;
                scores[33]++;
                scores[34]++;
                break;
            case 179:
                scores[2]++;
                scores[3]++;
                break;
            case -180:
                scores[26]++;
                scores[27]++;
                scores[28]++;
                break;
            case -120:
                scores[20]++;
                scores[21]++;
                scores[25]++;
                scores[26]++;
                scores[36]++;
                scores[27]++;
                scores[18]++;
                scores[28]++;
                break;
            case -60:
                scores[20]++;
                scores[11]++;
                scores[21]++;
                scores[22]++;
                scores[23]++;
                scores[24]++;
                scores[25]++;
                scores[35]++;
                scores[36]++;
                scores[18]++;
                break;
            case 0:
                scores[11]++;
                scores[12]++;
                scores[22]++;
                scores[23]++;
                scores[24]++;
                scores[34]++;
                scores[35]++;
                scores[6]++;
                scores[36]++;
                scores[7]++;
                scores[8]++;
                scores[18]++;
                break;
            case 60:
                scores[0]++;
                scores[1]++;
                scores[11]++;
                scores[12]++;
                scores[34]++;
                scores[5]++;
                scores[35]++;
                scores[6]++;
                scores[7]++;
                scores[8]++;
                break;
            case 120:
                scores[0]++;
                scores[1]++;
                scores[2]++;
                scores[12]++;
                scores[3]++;
                scores[4]++;
                scores[34]++;
                scores[5]++;
                break;
            case 180:
                scores[2]++;
                scores[3]++;
                scores[4]++;
                break;
            case -179:
                scores[26]++;
                scores[27]++;
                break;
            case -119:
                scores[20]++;
                scores[25]++;
                scores[26]++;
                scores[17]++;
                scores[27]++;
                scores[18]++;
                break;
            case -59:
                scores[10]++;
                scores[20]++;
                scores[11]++;
                scores[23]++;
                scores[24]++;
                scores[25]++;
                scores[17]++;
                scores[18]++;
                break;
            case 1:
                scores[10]++;
                scores[11]++;
                scores[12]++;
                scores[13]++;
                scores[23]++;
                scores[24]++;
                scores[6]++;
                scores[7]++;
                scores[17]++;
                scores[18]++;
                break;
            case 61:
                scores[0]++;
                scores[10]++;
                scores[11]++;
                scores[12]++;
                scores[13]++;
                scores[5]++;
                scores[6]++;
                scores[7]++;
                break;
            case 121:
                scores[0]++;
                scores[12]++;
                scores[3]++;
                scores[13]++;
                scores[4]++;
                scores[5]++;
                break;
            case 181:
                scores[3]++;
                scores[4]++;
                break;
            case -178:
                scores[26]++;
                break;
            case -118:
                scores[25]++;
                scores[16]++;
                scores[26]++;
                scores[17]++;
                break;
            case -58:
                scores[10]++;
                scores[24]++;
                scores[15]++;
                scores[25]++;
                scores[16]++;
                scores[17]++;
                break;
            case 2:
                scores[10]++;
                scores[13]++;
                scores[14]++;
                scores[24]++;
                scores[15]++;
                scores[6]++;
                scores[16]++;
                scores[17]++;
                break;
            case 62:
                scores[10]++;
                scores[13]++;
                scores[14]++;
                scores[5]++;
                scores[15]++;
                scores[6]++;
                break;
            case 122:
                scores[13]++;
                scores[4]++;
                scores[14]++;
                scores[5]++;
                break;
            case 182:
                scores[4]++;
                break;
            case -117:
                scores[16]++;
                break;
            case -57:
                scores[15]++;
                scores[16]++;
                break;
            case 3:
                scores[14]++;
                scores[15]++;
                scores[16]++;
                break;
            case 63:
                scores[14]++;
                scores[15]++;
                break;
            case 123:
                scores[14]++;
                break;
        }
    }

}
