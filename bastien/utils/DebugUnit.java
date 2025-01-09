package bastien.utils;

import bastien.Robot;
import battlecode.common.Clock;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class DebugUnit {
    private static final String[] indents = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t", "\t\t\t\t\t"};
    private static int BYTECODE_PER_TURNS = GameConstants.ROBOT_BYTECODE_LIMIT;

    private static final int[] lastBytecode = {0, 0, 0, 0, 0, 0};
    private static final int[] lastRound = {0, 0, 0, 0, 0, 0};

    public static boolean debug = true;
    private static RobotController rc;

    public static void init(){
        rc = Robot.rc;
        if(rc.getType().isTowerType()){
            BYTECODE_PER_TURNS = GameConstants.TOWER_BYTECODE_LIMIT;
        }

        char[] str = Integer.toString(rc.getID(), 26).toCharArray();
        for (int i = 0; i < str.length; i++) {
            str[i] += (char) (str[i] > '9' ? 10 : 49);
        }
    }

    public static void reset(int turn){
        for(int i=0; i<6; i++){
            lastBytecode[i] = 0;
            lastRound[i] = turn;
        }
    }

    public static void print(int level, String text){
        if(!debug){
            return;
        }

        // Exemple : [Bot Id] round | clock | comparaison | \t * indent text

        int cost = (rc.getRoundNum() - lastRound[level]) * BYTECODE_PER_TURNS + Clock.getBytecodeNum() - lastBytecode[level];
        lastRound[level] = rc.getRoundNum();
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
