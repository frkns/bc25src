package lmx;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public class Debug {
    public static boolean debug = true;
    private static final String[] indents = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t"};
    private static final int[] lastBytecode = {0, 0, 0, 0, 0};

    private static RobotController rc;

    public static void init() {
        rc = RobotPlayer.rc;
    }

    public static void reset(){
        lastBytecode[0] = 0;
        lastBytecode[1] = 0;
        lastBytecode[2] = 0;
        lastBytecode[3] = 0;
        lastBytecode[4] = 0;
    }

    /**
     * Prints the bytecode used to calculate directionScores for each action and execute action
     * Example: https://discord.com/channels/1316447035242709032/1323051422819815486/1327037155956228146
     */
    public static void print(String text){
        print(0, text);
    }

    public static void print(int level, String text) {
        if(!debug) return;

        int cost = Clock.getBytecodeNum() - lastBytecode[level];
        switch(level){
            case 0:
                lastBytecode[0] = Clock.getBytecodeNum();
            case 1:
                lastBytecode[1] = Clock.getBytecodeNum();
            case 2:
                lastBytecode[2] = Clock.getBytecodeNum();
            case 3:
                lastBytecode[3] = Clock.getBytecodeNum();
            case 4:
                lastBytecode[4] = Clock.getBytecodeNum();
            default:
        }

        if (cost > 500) {
            System.out.println("%5d | + %5d | ".formatted(Clock.getBytecodeNum(), cost) + indents[level] + text);
        } else {
            System.out.println("%5d |         | ".formatted(Clock.getBytecodeNum()) + indents[level] + text);
        }
    }
}
