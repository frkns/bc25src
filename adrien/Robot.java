package adrien;

import battlecode.common.*;
import adrien.Actions.Action;
import adrien.Interests.Interest;
import adrien.utils.DebugUnit;
import adrien.utils.MessageUnit;

import java.util.ArrayDeque;
import java.util.Random;

public abstract class Robot{
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

    public static final int INTEREST_CONSISTANCY = 2;
    public static final int INTEREST_EXPLORE = 1;
    public static final int INTEREST_ON_PAINT = 1;
    public static final int INTEREST_MARK = 20;
    public static final int INTEREST_RUNE = 10;

    public static final int ACTION_DEFEND = 30; // Towers
    public static final int ACTION_ATTACK_TOWER = 25; // Soldier

    public static final int ACTION_BUILD_TOWER = 30;
    public static final int ACTION_PAINT_MARK = 20;
    public static final int ACTION_SET_TOWER_PATTERN = 15;

    public static final int ACTION_MOPSWING = 15;

    public static final int ACTION_PAINT_UNDER_ENEMIES = 11;
    public static final int ACTION_PAINT_UNDER_ALLIES = 10;
    public static final int ACTION_PAINT_EMPTY = 1;

    public static Random rng = new Random();

    public static int action_range = 0;

    public static RobotController rc;
    static MessageUnit messageUnit;
    public static ArrayDeque<Interest> interests = new ArrayDeque<>();
    public static ArrayDeque<Action> actions = new ArrayDeque<>();

    public static RobotInfo[] allies;
    public static RobotInfo[] enemies;
    public static MapLocation[] ruins;
    public static MapInfo[] marks;
    public static MapLocation nearestEmptyRuins;
    public static MapInfo nearestIncorrectMark;

    public static ArrayDeque<MapLocation> ruinsVisited = new ArrayDeque<>();

    public static int attackCost = 0;

    public Robot(RobotController controlller){
        rc = controlller;
        messageUnit = new MessageUnit(this);

        action_range = rc.getType().actionRadiusSquared;
    }

    public void init(){
        DebugUnit.init(rc);
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

                // ------------------- Cheking if I can mark the ruin as visited -------------------
                // I have just explore the ruin, im far and someone is already watch the ruins. I can leave
                if(distance <= 4 && distance > 2){
                    if(rc.senseNearbyRobots(loc, 2, rc.getTeam()).length >= 1){
                        // We mark rune as visited if we have another robot staing near to it to claim the tower
                        ruinsVisited.add(loc);
                    }
                } else if (distance < 2) {

                    // We are two or more to keep the ruins, i can leave if im not the lover ID
                    for(RobotInfo info: rc.senseNearbyRobots(loc, 2, rc.getTeam())){
                        if(info.getID() > rc.getID()){
                            ruinsVisited.add(loc);
                        }
                        continue;
                    }
                }
                // ---------------------------------------------------------------------------------


                if(!ruinsVisited.contains(loc) && distance < distanceNeirestRuins){
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

        if(nearestIncorrectMark != null) {
            DebugUnit.print(4, "nearestIncorrectMark : " + nearestIncorrectMark.getMapLocation() + " " + nearestIncorrectMark.getMark());
        }else{
            DebugUnit.print(4, "nearestIncorrectMark : EMPTY");

        }
    }

    public void play() throws GameActionException {
        DebugUnit.reset(rc.getRoundNum()); // Reset clock to benchmark

        DebugUnit.print(0, "Start turn. " + rc.getType() + " at " + rc.getLocation());
        DebugUnit.print(1, "Init.");
        initTurn();

        DebugUnit.print(1, "Init interest.");
        for(Interest interest: interests){
            interest.initTurn();
        }

        DebugUnit.print(1, "Neighbours score.");
        // Calculating neighbours score
        int[] neighbours = new int[8];
        for(int i=0; i<8; i++){
            Direction dir = directions[i];
            if(rc.canMove(dir)){
                DebugUnit.print(2, "Direction : " + dir);
                MapLocation location = rc.getLocation().add(dir);

                for(Interest interest: interests){
                    int score = interest.getScore(location);
                    if(score != 0) {
                        DebugUnit.print(3, interest.name + " : " + score);
                    }
                    neighbours[i] += score;
                }

            }else{
                DebugUnit.print(2, "Cant move to : " + dir);
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
            rc.setIndicatorString(bestAction.name + " - " + bestScore);
            bestAction.play();
        }else{
            DebugUnit.print(1, "No action to play");
        }

        // Suicide : To fix
        if(false && rc.getPaint() < attackCost && nearestEmptyRuins == null){
            DebugUnit.print(0, "Not Enought paint. Disintegrate.");
            rc.disintegrate();
        }

        DebugUnit.print(0, "End turn.");
    }
    public void end(){}
}