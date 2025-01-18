package ryan;

import battlecode.common.*;
import ryan.DebugIndicators;


public class Communication extends RobotPlayer {
    // ---------- Attributes ----------
    static RobotController rc;
    static char[] reverseID;

    static final int HEADER_MASK = 0b1111 << 28; // Coded on 4 upper bits
    static final int HEADER_CLASS = 1 << 28;
    static final int HEADER_LOCATION = 2 << 28;

    /** Message Class :
     *   4 bits for encoding header
     *  28 bits for encoding class
     */

    static final int MASK_CLASS = (1 << 28) - 1;

    /** Message Location :
     *   4 bits for encoding header
     *  16 bits for information int
     *  12 bits for location = log2(60*60)
     */

    static final int MASK_LOCATION = (1 << 12) - 1;
    static final int MASK_LOCATION_INFO = 0b1111111111111111 << 12;

    // ---------- Instantiates and utils ----------
    public static void init(RobotController r){
        rc = r;
        reverseID = "\0".repeat(20000).toCharArray();
    }

    public static MapLocation intToLoc(int i){
        return new MapLocation(i % mapWidth, i / mapWidth);
    }

    public static int locToInt(MapLocation loc){
        return loc.x + loc.y * mapWidth;
    }

    public static MapLocation getLocation(int unitID){
        char index = reverseID[unitID];
        if(index < nearbyRobots.length){
            if(nearbyRobots[index].getID() == unitID){
                return nearbyRobots[index].getLocation();
            }
        }
        return null;
    }

    // ---------- Messages ----------
    public static void initTurn(){
        // Maybe only for towers that need to communicate to units ?
        // reverseID[robotID] = index of robotID in nearbyAllies.
        char i = 0;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getTeam() == rc.getTeam()) {
                reverseID[robot.getID()] = i++;
            }
        }
    }


    public static void sendClassMessage(int unitID, int type) throws GameActionException{
        MapLocation loc = getLocation(unitID);
        if(loc != null && rc.canSendMessage(loc)){
            rc.sendMessage(loc, HEADER_CLASS + type);
        }
    }

    /**
     * Send a location 'target' to robot 'unitID' with an integer of 16 bits 'info'.
     * */
    public static void sendLocationMessage(int unitID, int info, MapLocation target) throws GameActionException{
        MapLocation loc = getLocation(unitID);
        if(loc != null && rc.canSendMessage(loc)){
            rc.sendMessage(loc, HEADER_LOCATION + (info << 12) + locToInt(target));
        }else{
            DebugIndicators.printString("Can'tsend" + unitID);
        }
    }

    public static void readMessages(){
        // todo read message from actual turn in addition to last turn
        for (Message mes : rc.readMessages(roundNum - 1)) {
            int header = mes.getBytes() & HEADER_MASK;
            switch(header){
                case HEADER_CLASS:
                    readMessageClass(mes);
                    break;
                case HEADER_LOCATION:
                    readMessageLocation(mes);
                    break;
                default:
                    DebugIndicators.printString("Can'tRead" + mes);
            }
        }
    }

    public static void readMessageClass(Message message){
        int classType = message.getBytes() & MASK_CLASS;

        DebugIndicators.printString("Class" + classType);
    }

    public static void readMessageLocation(Message message){
        int info = (message.getBytes() & MASK_LOCATION_INFO) >> 12;
        MapLocation target = intToLoc(message.getBytes() & MASK_LOCATION);

        DebugIndicators.printString("Location" + target + "Info:" + info);
    }

}
