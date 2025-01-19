package e.utils;

import battlecode.common.Clock;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import e.actions.Action;
import e.knowledge._Info;

public class Debug{
    // Debug settings
    public static boolean DEBUG = true;
    public static boolean INFO = true;
    public static Integer id; // Add this field for ID filtering
    // Debug constants
    public static String INIT = "Initializing: ";
    public static String UPDATE_DIR_SCORES = "";
    public static String CALCSCORE = "";
    public static String PLAY = "Playing ";
    // Variables for DEBUG class functionality
    public static final String [] directionAbbreviations = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "C"};
    public static final String[] indents = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t"};
    public static int[] lastBytecode = new int[5];
    
    public static RobotController rc;

    public static void init() {
        rc = _Info.rc;
    }

    public static void reset(){
        lastBytecode = new int[5];
    }

    public static void print(String text){
        print(0, text);
    }

    public static void print(int level, String text){
        Debug.print(level, text, Debug.INFO);
    }
    public static void print(int level, String text, boolean cond) {
        if(!DEBUG || !cond || (id != null && _Info.id != id)) return;

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

    public static void printDirectionScores(int level, int[] directionScores, boolean cond) {
        if(!DEBUG || !cond || (id != null && _Info.id != id)) return;

        StringBuilder sb = new StringBuilder("Direction scores: [");
        for (int i = 0; i < 9; i++) {
            sb.append(directionAbbreviations[i]).append(": ").append(directionScores[i]);
            if (i < 8) sb.append(", ");
        }
        sb.append("]");
        System.out.println("%5d |         | ".formatted(Clock.getBytecodeNum()) + indents[level] + sb.toString());
    }
}