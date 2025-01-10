package bastien.utils;

import bastien.Robot;
import battlecode.common.Clock;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class Debug {
    public static boolean debug = true;

    private static final String[] indents = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t", "\t\t\t\t\t"};
    private static int BYTECODE_PER_TURNS = GameConstants.ROBOT_BYTECODE_LIMIT;

    private static final int[] lastBytecode = {0, 0, 0, 0, 0, 0};
    private static final int[] lastRound = {0, 0, 0, 0, 0, 0};

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
      * Prints the bytecode used to calculate score for each action and execute action
      * Example: https://discord.com/channels/1316447035242709032/1323051422819815486/1327037155956228146 
      */
    public static void print(int level, String text){
        if(!debug) return;

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
}
