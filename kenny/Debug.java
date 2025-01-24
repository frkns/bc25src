// https://github.com/maxwelljones14/BattleCode2023/blob/main/src/MPWorking/Debug.java
package kenny;

import battlecode.common.*;

/**
 * Utilizes the robot's indicator string to display debug information
 *
 * The printString(cond, s) method:
 * - Accumulates messages to be displayed at the end of the turn
 * - Params:
 * -     cond --> condition to print the message,
 * -     s    --> message to print
 *
 * The flush() method:
 * - Sets the robot's indicator string with accumulated messages from Debug.printString()
 * - Clears the string builder for next turn
 * - Resets bytecode debugging
 * - Must be called at the end of each turn to display debug information
 */
public class Debug extends RobotPlayer {
    public static final boolean VERBOSE = true;  // Set to true to enable debug messages
    public static final boolean FAIL_FAST = false;
    public static final boolean INDICATORS = true;
    public static final boolean INFO = true;
    public static final boolean TOWER = true;
    public static final boolean COMMS = true;


    public static String bytecodeDebug = new String();

    private static StringBuilder sb;

    public static void init() {
        sb = new StringBuilder();
    }

    public static void flush() {
        rc.setIndicatorString(sb.toString());
        sb = new StringBuilder();
        bytecodeDebug = new String();
    }

    public static void printString(boolean cond, String s) {
        if (VERBOSE && cond) {
            sb.append(s);
            sb.append(", ");
        }
    }

    public static void printString(String s) {
        Debug.printString(Debug.INFO, s);
    }

    public static void failFast(GameActionException ex) {
        if (Debug.FAIL_FAST) {
            throw new IllegalStateException(ex);
        }
    }

    public static void failFast(String message) {
        if (Debug.FAIL_FAST) {
            throw new IllegalStateException(message);
        }
    }

    public static void betterAssert(boolean cond, String msg) {
        if (!cond) {
            failFast(msg);
        }
    }

    public static void println(boolean cond, String s) {
        if (VERBOSE && cond) {
            System.out.println(s);
        }
    }

    public static void println(boolean cond, String s, int id) {
        if (VERBOSE && cond && (id < 0 || rc.getID() == id)) {
            System.out.println(s);
        }
    }

    public static void println(String s) {
        Debug.println(Debug.INFO, s);
    }

    public static void println(String s, int id) {
        Debug.println(Debug.INFO, s, id);
    }

    public static void print(boolean cond, String s) {
        if (VERBOSE && cond) {
            System.out.print(s);
        }
    }

    public static void setIndicatorDot(boolean cond, MapLocation loc, int r, int g, int b) throws GameActionException{
        if (VERBOSE && INDICATORS && cond && loc != null) {
            rc.setIndicatorDot(loc, r, g, b);
        }
    }

    public static void setIndicatorLine(boolean cond, MapLocation startLoc, MapLocation endLoc, int r, int g, int b) throws GameActionException {
        if (VERBOSE && INDICATORS && cond && startLoc != null && endLoc != null) {
            rc.setIndicatorLine(startLoc, endLoc, r, g, b);
        }
    }

    public static void setIndicatorDot(MapLocation loc, int r, int g, int b) throws GameActionException {
        setIndicatorDot(INDICATORS, loc, r, g, b);
    }

    public static void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int r, int g, int b) throws GameActionException {
        setIndicatorLine(INDICATORS, startLoc, endLoc, r, g, b);
    }
}
