package e.utils;

import battlecode.common.Clock;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import e.Robot;
import e.actions.Action;
import e.knowledge._Info;

public class Debug {
    public static boolean debug = true;
    private static Integer id; // Add this field for ID filtering

    public static String INIT = "Initializing: ";
    public static String UPDATE_DIR_SCORES = "";
    public static String CALCSCORE = "";
    public static String PLAY = "Playing ";


    private static final String[] indents = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t", "\t\t\t\t\t"};
    private static int BYTECODE_PER_TURNS = GameConstants.ROBOT_BYTECODE_LIMIT;
    private static final int[] lastBytecode = {0, 0, 0, 0, 0, 0};
    private static final int[] lastRound = {0, 0, 0, 0, 0, 0};

    private static final String [] directionAbbreviations = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "C"};

    private static RobotController rc;
    private static int lastByteCodeClockDebugged;

    public static void init() {
        rc = Robot.rc;
        // Adjust bytecode limit for tower type robots
        if(rc.getType().isTowerType()) {
            BYTECODE_PER_TURNS = GameConstants.TOWER_BYTECODE_LIMIT;
        }
    }

    public static void reset(int turn){
        for(int i=0; i<6; i++){
            lastBytecode[i] = 0;
            lastRound[i] = turn;
        }
        lastByteCodeClockDebugged = 0;
    }

    /**
      * Prints the bytecode used to calculate directionScores for each action and execute action
      * Example: https://discord.com/channels/1316447035242709032/1323051422819815486/1327037155956228146 
      */
    
    // Default without debugAction
    public static void print(int level, String text) {
        if(!debug) return;
        if(id != null && _Info.id != id) return;

        int cost = (rc.getRoundNum() - lastRound[level]) * BYTECODE_PER_TURNS + Clock.getBytecodeNum() - lastBytecode[level];
        lastRound[level] = rc.getRoundNum();
        lastBytecode[level] = Clock.getBytecodeNum();

        for (int i = level + 1; i < 6; i++) {
            lastRound[i] = lastRound[level];
            lastBytecode[i] = lastBytecode[level];
        }

        if (Clock.getBytecodeNum() - lastByteCodeClockDebugged > 500) {
            lastByteCodeClockDebugged = Clock.getBytecodeNum();

            System.out.println("%5d | + %5d | ".formatted(Clock.getBytecodeNum(), cost) + indents[level] + text);
        } else {
            System.out.println("%5d |         | ".formatted(Clock.getBytecodeNum()) + indents[level] + text);
        }
    }
    
    public static void print(int level, String text, boolean debugAction) {
        if(!debug || !debugAction) return;
        if(id != null && _Info.id != id) return;

        int cost = (rc.getRoundNum() - lastRound[level]) * BYTECODE_PER_TURNS + Clock.getBytecodeNum() - lastBytecode[level];
        lastRound[level] = rc.getRoundNum();
        lastBytecode[level] = Clock.getBytecodeNum();

        for (int i = level + 1; i < 6; i++) {
            lastRound[i] = lastRound[level];
            lastBytecode[i] = lastBytecode[level];
        }

        if (Clock.getBytecodeNum() - lastByteCodeClockDebugged > 500) {
            lastByteCodeClockDebugged = Clock.getBytecodeNum();

            System.out.println("%5d | + %5d | ".formatted(Clock.getBytecodeNum(), cost) + indents[level] + text);
        } else {
            System.out.println("%5d |         | ".formatted(Clock.getBytecodeNum()) + indents[level] + text);
        }
    }

    public static void setActionIndicatorString(Action action){
        if(!debug) return;
        if (action.targetLoc != null) {
            rc.setIndicatorString(action.name + " - " + action.targetLoc.toString());
        } else {
            rc.setIndicatorString(action.name);
        }
    }

    public static void printDirectionScores(int level, int[] directionScores, boolean debugInterest) {
        if(!debug || !debugInterest) return;
        if(id != null && _Info.id != id) return;
        StringBuilder sb = new StringBuilder("Direction scores: [");
        for (int i = 0; i < 9; i++) {
            sb.append(directionAbbreviations[i]).append(": ").append(directionScores[i]);
            if (i < 8) sb.append(", ");
        }
        sb.append("]");
        System.out.println("%5d |         | ".formatted(Clock.getBytecodeNum()) + indents[level] + sb.toString());
    }
}
