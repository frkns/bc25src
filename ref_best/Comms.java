package ref_best;

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
            int fst = (bits >> (21 - 1)) & 0xFFF;
            int snd = (bits >> (21 - 14)) & 0xFFF;
            boolean fstType = ((bits >> (32 - 13)) & 1) == 1;
            boolean sndType = ((bits >> (32 - 26)) & 1) == 1;

            MapLocation fstLoc;
            MapLocation sndLoc;
            fstLoc = fst == 0 ? null : Comms.intToLoc(fst);
            sndLoc = snd == 0 ? null : Comms.intToLoc(snd);


            if (fstLoc != null && fstLoc != fstTowerTarget && fstLoc != sndTowerTarget) {
                visFstTowerTarget = false;
                fstTowerTarget = fstLoc;
                fstTowerTargetIsDefense = fstType;
            }
            if (sndLoc != null && sndLoc != fstTowerTarget && sndLoc != sndTowerTarget) {
                visSndTowerTarget = false;
                sndTowerTarget = sndLoc;
                sndTowerTargetIsDefense = sndType;
            }
        }
    }

    /**
     * Converts an integer position to a MapLocation.
     *
     * @param i The integer representing a position (x + y * mapWidth)
     * @return MapLocation corresponding to the integer position
     */
    public static MapLocation intToLoc(int i) {
        return new MapLocation(i % mapWidth, i / mapWidth);
    }

    /**
     * Converts a MapLocation to an integer position.
     *
     * @param loc The MapLocation to convert
     * @return Integer representation of the location
     */
    public static int locToInt(MapLocation loc) {
        return loc.x + loc.y * mapWidth;
    }
}