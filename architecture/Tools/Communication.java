package architecture.Tools;

import architecture.RobotPlayer;
import battlecode.common.*;


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

    /**
     * Initializes the Communication system with a RobotController and creates a reverse ID lookup array.
     * @param r The RobotController instance to use
     */
    public static void init(RobotController r){
        rc = r;
        reverseID = "\0".repeat(20000).toCharArray();
    }

    /**
     * Converts an integer position to a MapLocation.
     * @param i The integer representing a position (x + y * mapWidth)
     * @return MapLocation corresponding to the integer position
     */
    public static MapLocation intToLoc(int i){
        return new MapLocation(i % mapWidth, i / mapWidth);
    }

    /**
     * Converts a MapLocation to an integer position.
     * @param loc The MapLocation to convert
     * @return Integer representation of the location
     */
    public static int locToInt(MapLocation loc){
        return loc.x + loc.y * mapWidth;
    }

    /**
     * Retrieves the location of a robot using its unit ID from the nearby robots array.
     * @param unitID The ID of the robot to locate
     * @return MapLocation of the robot, or null if not found
     */
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

    /**
     * Initializes the reverse ID lookup array at the start of each turn.
     * Maps robot IDs to their index in the nearbyRobots array for quick lookup.
     */
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

    /**
     * Sends a class-type message to a specific unit.
     * @param unitID The ID of the target robot
     * @param type The class type to send
     * @throws GameActionException If sending the message fails
     */
    public static void sendClassMessage(int unitID, int type) throws GameActionException{
        MapLocation loc = getLocation(unitID);
        if(loc != null && rc.canSendMessage(loc)){
            rc.sendMessage(loc, HEADER_CLASS + type);
        }
    }

    /**
     * Sends a location-based message to a specific unit with additional information.
     * @param unitID The ID of the target robot
     * @param info A 16-bit integer containing additional information
     * @param target The target MapLocation to send
     * @throws GameActionException If sending the message fails
     */
    public static void sendLocationMessage(int unitID, int info, MapLocation target) throws GameActionException{
        MapLocation loc = getLocation(unitID);
        if(loc != null && rc.canSendMessage(loc)){
            rc.sendMessage(loc, HEADER_LOCATION + (info << 12) + locToInt(target));
            Debug.println(Debug.COMMS, "Sent location message to ID:" + unitID + " info:" + info + " target:" + target);
        }else{
            Debug.println(Debug.COMMS, "Failed to send to ID:" + unitID + " - Target not found or can't send");
        }
    }

    /**
     * Reads and processes all messages from the previous round.
     * Delegates to specific handlers based on message header type.
     */
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
                    Debug.println(Debug.COMMS, "Can't Read" + mes);
            }
        }
    }

    /**
     * Processes class-type messages.
     * @param message The message containing class information
     */
    public static void readMessageClass(Message message){
        int classType = message.getBytes() & MASK_CLASS;
        Debug.println(Debug.COMMS, "Received class message: " + classType);
    }

    /**
     * Processes location-type messages.
     * @param message The message containing location and info data
     */
    public static void readMessageLocation(Message message){
        int info = (message.getBytes() & MASK_LOCATION_INFO) >> 12;
        MapLocation target = intToLoc(message.getBytes() & MASK_LOCATION);
    }

}
