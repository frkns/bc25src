package ref_best_tmp;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.UnitType;


public class Comms extends RobotPlayer {
    public static void reportToTower(MapLocation towerLoc) throws GameActionException {
        // assume canSendMessage
        int outgoingMsg = 0;

        if (nearestEnemyTower == null) {
            // outgoingMsg |= 0xFFF << (21 - 1);  // sentinel value
        } else {
            outgoingMsg |= Comms.locToInt(nearestEnemyTower) << (21 - 1);
            outgoingMsg |= (nearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 1 : 0) << (32 - 13);
        }
        if (sndNearestEnemyTower == null) {
            // outgoingMsg |= 0xFFF << (21 - 1);  // sentinel value
        } else {
            outgoingMsg |= Comms.locToInt(sndNearestEnemyTower) << (21 - 14);
            outgoingMsg |= (sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 1 : 0) << (32 - 26);
        }

        rc.sendMessage(towerLoc, outgoingMsg);
    }

    public static void readAndUpdateTowerTargets(int round) throws GameActionException {
        Message[] msgs = rc.readMessages(round);
        for (Message msg : msgs) {
            int bits = msg.getBytes();

            if (((bits >> (32 - 27)) & 1) == 1) {
                rc.setIndicatorLine(new MapLocation(mapWidth-1, mapHeight-1), rc.getLocation(), 255, 0, 255);
                System.out.println("-> killed by tower #" + rc.getID());
                rc.disintegrate();  // tower sent a termination signal
            }

            int fst = (bits >> (21 - 1)) & 0xFFF;
            int snd = (bits >> (21 - 14)) & 0xFFF;
            boolean fstType = ((bits >> (32 - 13)) & 1) == 1;
            boolean sndType = ((bits >> (32 - 26)) & 1) == 1;

            MapLocation fstLoc;
            MapLocation sndLoc;
            fstLoc = fst == 0 ? null : Comms.intToLoc(fst);
            sndLoc = snd == 0 ? null : Comms.intToLoc(snd);


            if (fstLoc != null && !fstLoc.equals(fstTowerTarget) && !fstLoc.equals(sndTowerTarget)) {
                visFstTowerTarget = false;
                fstTowerTarget = fstLoc;
                fstTowerTargetIsDefense = fstType;
            }
            if (sndLoc != null && !sndLoc.equals(fstTowerTarget) && !sndLoc.equals(sndTowerTarget)) {
                visSndTowerTarget = false;
                sndTowerTarget = sndLoc;
                sndTowerTargetIsDefense = sndType;
            }

            break;
        }
    }

    public static MapLocation intToLoc(int i) {
        return new MapLocation(i % mapWidth, i / mapWidth);
    }


    public static int locToInt(MapLocation loc) {
        return loc.x + loc.y * mapWidth;
    }
}