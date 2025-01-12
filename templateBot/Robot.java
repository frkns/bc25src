package templateBot;

import battlecode.common.*;
import templateBot.Actions.Action;
import templateBot.Interests.Interest;
import templateBot.utils.DebugUnit;
import templateBot.utils.MessageUnit;

import java.util.ArrayDeque;

public abstract class Robot{
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static RobotController rc;
    static MessageUnit messageUnit;
    public static ArrayDeque<Interest> interests = new ArrayDeque<>();
    public static ArrayDeque<Action> actions = new ArrayDeque<>();

    public static RobotInfo[] allies;
    public static RobotInfo[] enemies;


    public Robot(RobotController controlller){
        rc = controlller;
        messageUnit = new MessageUnit(this);
    }

    public void init(){
        DebugUnit.init(rc);
    }

    public void initTurn() throws GameActionException {
        allies = rc.senseNearbyRobots(-1, rc.getTeam());
        enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
    }

    public void play() throws GameActionException {
        DebugUnit.print(0, "Start turn.");
        DebugUnit.print(1, "Init.");
        initTurn();

        DebugUnit.print(1, "Neighbours score.");
        // Calculating neighbours score
        int[] neighbours = new int[8];
        for(int i=0; i<8; i++){
            Direction dir = directions[i];
            if(rc.canMove(dir)){
                MapLocation location = rc.getLocation().add(dir);

                for(Interest interest: interests){
                    int score = interest.getScore(location);
                    DebugUnit.print(2, interest.name + " : " + score);
                    neighbours[i] += score;
                }

            }else{
                neighbours[i] = 0;
            }
        }

        // Moving to best location
        Direction dir = Direction.CENTER;
        int bestScore = 0;
        for(int i=0; i < 8; i++){
            if(neighbours[i] > bestScore){
                bestScore = neighbours[i];
                dir = directions[i];
            }
        }

        // Moving to location
        if(rc.canMove(dir)){
            rc.move(dir);
            DebugUnit.print(1, "Moving to          : " + rc.getLocation());
        }else{
            DebugUnit.print(1, "Dont move. Stay at : " + rc.getLocation());
        }

        DebugUnit.print(1, "Actions score.");
        Action bestAction = null;
        bestScore = 0;
        for(Action action: actions){
            int score = action.getScore();
            DebugUnit.print(2, action.name + " : " + score);
            if(score > bestScore){
                bestScore = score;
                bestAction = action;
            }
        }

        if(bestScore > 0){
            DebugUnit.print(1, "Playing action     : " + bestAction.name);
            bestAction.play();
        }else{
            DebugUnit.print(1, "No action to play");
        }
    }
    public void end(){}
}