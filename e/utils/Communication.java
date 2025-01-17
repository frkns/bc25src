package e.utils;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import e.knowledge._Info;


public class Communication {
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
        return new MapLocation(i % _Info.MAP_WIDTH, i / _Info.MAP_WIDTH);
    }

    public static int locToInt(MapLocation loc){
        return loc.x + loc.y * _Info.MAP_WIDTH;
    }

    public static MapLocation getLocation(int unitID){
        char index = reverseID[unitID];
        if(index < _Info.nearbyAllies.length){
            if(_Info.nearbyAllies[index].getID() == unitID){
                return _Info.nearbyAllies[index].getLocation();
            }
        }
        return null;
    }

    // ---------- Messages ----------
    public static void initTurn(){
        // Maybe only for towers that need to communicate to units ?
        // reverseID[robotID] = index of robotID in nearbyAllies.
        for (char i = 0; i < _Info.nearbyAllies.length; i++) {
            reverseID[_Info.nearbyAllies[i].getID()] = i;
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
            Debug.print(0, "Can't send location message. Did we have a path of paint between sender and receiver ?");
        }
    }

    public static void readMessages(){
        // todo read message from actual turn in addition to last turn
        for (Message mes : rc.readMessages(_Info.round - 1)) {
            int header = mes.getBytes() & HEADER_MASK;
            switch(header){
                case HEADER_CLASS:
                    readMessageClass(mes);
                    break;
                case HEADER_LOCATION:
                    readMessageLocation(mes);
                    break;
                default:
                    Debug.print(0, "Unable to read message " + mes);
            }
        }
    }

    public static void readMessageClass(Message message){
        int classType = message.getBytes() & MASK_CLASS;

        Debug.print(0, "I have received a message telling me the class " + classType);
    }

    public static void readMessageLocation(Message message){
        int info = (message.getBytes() & MASK_LOCATION_INFO) >> 12;
        MapLocation target = intToLoc(message.getBytes() & MASK_LOCATION);

        Debug.print(0, "I have received a message telling me location " + target + " and " + info);
    }

}
