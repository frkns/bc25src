// https://github.com/maxwelljones14/BattleCode2023/blob/main/src/MPWorking/Debug.java
package kenny;

import battlecode.common.*;

class Debug extends RobotPlayer {
    static RobotController rc;

    static final boolean VERBOSE = true;
    static final boolean INFO = true;
    static final boolean PATHFINDING = true;
    static final boolean INDICATORS = true;

    static void init(RobotController r) throws GameActionException {
        rc = r;
        sb = new StringBuilder();
    }

    static String bytecodeDebug = new String();

    static StringBuilder sb;

    static void flush() {
        rc.setIndicatorString(sb.toString());
        sb = new StringBuilder();
        bytecodeDebug = new String();
    }

    static void printString(boolean cond, String s) {
        if (VERBOSE && cond) {
            sb.append(s);
            sb.append(", ");
        }
    }

    static void printString(String s) {
        Debug.printString(Debug.INFO, s);
    }

    static void failFast(GameActionException ex) {
        if (Utils.DEBUG_FAIL_FAST) {
            throw new IllegalStateException(ex);
        }
    }

    static void failFast(String message) {
        if (Utils.DEBUG_FAIL_FAST) {
            throw new IllegalStateException(message);
        }
    }

    static void betterAssert(boolean cond, String msg) {
        if (!cond) {
            failFast(msg);
        }
    }

    static void println(boolean cond, String s) {
        if (VERBOSE && cond) {
            System.out.println(s);
        }
    }

    static void println(boolean cond, String s, int id) {
        if (VERBOSE && cond && (id < 0 || rc.getID() == id)) {
            System.out.println(s);
        }
    }

    static void println(String s) {
        Debug.println(Debug.INFO, s);
    }

    static void println(String s, int id) {
        Debug.println(Debug.INFO, s, id);
    }

    static void print(boolean cond, String s) {
        if (VERBOSE && cond) {
            System.out.print(s);
        }
    }

    static void setIndicatorDot(boolean cond, MapLocation loc, int r, int g, int b) throws GameActionException {
        if (VERBOSE && INDICATORS && cond && loc != null) {
            rc.setIndicatorDot(loc, r, g, b);
        }
    }

    static void setIndicatorLine(boolean cond, MapLocation startLoc, MapLocation endLoc, int r, int g, int b) throws GameActionException {
        if (VERBOSE && INDICATORS && cond && startLoc != null && endLoc != null) {
            rc.setIndicatorLine(startLoc, endLoc, r, g, b);
        }
    }

}
