package templateBot.utils;

import battlecode.common.Clock;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class DebugUnit {
    private static final String tab = "\t";
    private static final String[] indents = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t", "\t\t\t\t\t"};
    private static int LIMITS = GameConstants.ROBOT_BYTECODE_LIMIT;

    private static final int[] lastBytecode = {0, 0, 0, 0, 0, 0};
    private static final int[] lastRound = {0, 0, 0, 0, 0, 0};

    public static boolean debug = true;
    private static RobotController robot;

    public static void init(RobotController rc){
        robot = rc;
        switch (rc.getType()){
            case SOLDIER: break;
            case MOPPER: break;
            case SPLASHER: break; // Consider upgrading examplefuncsplayer to use splashers!
            default:
                LIMITS = GameConstants.TOWER_BYTECODE_LIMIT;
                break;
        }

        char[] str = Integer.toString(rc.getID(), 26).toCharArray();
        for (int i = 0; i < str.length; i++) {
            str[i] += (char) (str[i] > '9' ? 10 : 49);
        }
    }

    public static final void print(int level, String text){
        if(!debug){
            return;
        }

        // Exemple : [Bot Id] round | clock | comparaison | \t * indent text

        int cost = (robot.getRoundNum() - lastRound[level]) * LIMITS + Clock.getBytecodeNum() - lastBytecode[level];
        lastRound[level] = robot.getRoundNum();
        lastBytecode[level] = Clock.getBytecodeNum();

        for (int i = level + 1; i < 6; i++) {
            lastRound[i] = lastRound[level];
            lastBytecode[i] = lastBytecode[level];
        }

        if (cost > 500) {
            System.out.println("%5d | + %5d | ".formatted(Clock.getBytecodeNum(), cost) + indents[level] + text);
        }else{
            System.out.println("%5d |         | ".formatted(Clock.getBytecodeNum()) + indents[level] + text);
        }
    }
}
