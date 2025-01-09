package bastien;

import bastien.Actions.Action;
import battlecode.common.*;
import bastien.utils.DebugUnit;
import bastien.utils.MessageUnit;

import java.util.ArrayDeque;
import java.util.Random;

public abstract class Robot{
    // -------------- Scores --------------
    public static int ACTION_PAINT = 30;
    public static int ACTION_EXPLORE = 15;

    // -------------- Attributs --------------
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static Random rng = new Random();
    public static int action_range = 0;

    public static RobotController rc;
    static MessageUnit messageUnit;
    public static ArrayDeque<Action> actions = new ArrayDeque<>();


    // -------------- Map infos you could need --------------
    public static RobotInfo[] allies;
    public static RobotInfo[] enemies;
    public static MapLocation[] ruins;
    public static MapInfo[] marks;
    public static MapLocation nearestEmptyRuins;
    public static MapInfo nearestIncorrectMark;
    public static int attackCost = 0;

    // -------------- Methodes --------------
    public Robot(RobotController controlller){
        rc = controlller;
        messageUnit = new MessageUnit(this);
        action_range = rc.getType().actionRadiusSquared;
        DebugUnit.init();
    }

    public void initTurn() throws GameActionException {
        allies = rc.senseNearbyRobots(-1, rc.getTeam());
        enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        ruins = rc.senseNearbyRuins(-1);
        marks = rc.senseNearbyMapInfos(-1);

        nearestEmptyRuins = null;
        int distanceNeirestRuins = 999;
        for(MapLocation loc: ruins){
            if(!rc.canSenseRobotAtLocation(loc)){ // No tower at the location, empty ruins

                int distance = rc.getLocation().distanceSquaredTo(loc);
                if(distance < distanceNeirestRuins){
                    nearestEmptyRuins = loc;
                    distanceNeirestRuins = distance;
                }
            }
        }

        nearestIncorrectMark = null;
        int distanceNeirestIncorrectMark = 99;
        for(MapInfo info: marks){
            // Color != Mark and Mark is not empty
            if(!info.getMark().equals(PaintType.EMPTY) && !info.getPaint().equals(info.getMark())){

                // Paint is not enemy or type is mopper
                if(!info.getPaint().isEnemy()|| rc.getType().equals(UnitType.MOPPER)) {

                    // Paint is nearest
                    int distance = rc.getLocation().distanceSquaredTo(info.getMapLocation());
                    if(distance < distanceNeirestIncorrectMark) {
                        nearestIncorrectMark = info;
                        distanceNeirestIncorrectMark = distance;
                    }
                }
            }
        }
    }

    public void playTurn() throws GameActionException {
        DebugUnit.reset(rc.getRoundNum()); // Reset clock to benchmark

        DebugUnit.print(0, "Start turn. " + rc.getType() + " at " + rc.getLocation());
        DebugUnit.print(1, "Init.");
        initTurn();

        DebugUnit.print(1, "");
        DebugUnit.print(1, "Calculate actions.");
        Action bestAction = null;
        int bestScore = 0;

        for(Action action: actions){
            DebugUnit.print(2, action.name + "...");
            action.init();
            int score = action.getScore();
            if(score > bestScore){
                bestScore = score;
                bestAction = action;
            }
            DebugUnit.print(2,  "SCORE : " + score);
            DebugUnit.print(2,  "");
        }

        if(bestScore > 0){
            DebugUnit.print(1, "");
            DebugUnit.print(1, "Playing action     : " + bestAction.name + " with score " + bestScore);
            rc.setIndicatorString(bestAction.name + " - " + bestScore);
            bestAction.play();
        }else{
            DebugUnit.print(1, "");
            DebugUnit.print(1, "No action to play");
        }
    }

    public void endTurn(){
        DebugUnit.print(0, "End turn.");
    }
}