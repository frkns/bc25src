package e_action;

import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

import java.util.ArrayDeque;
import java.util.Random;

public abstract class Robot{

    public static RobotController rc;
    public static ArrayDeque<Action> actions = new ArrayDeque<>();
    public statit ArrayDque


    // -------------- Useful constants --------------
    public static final Random rng = new Random(0);
    public static final Direction[] directions = {
            Direction.CENTER,
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };


    // -------------- Constants that vary by game --------------
    public static int MAP_WIDTH;
    public static int MAP_HEIGHT;
    public static int MAP_AREA;

    // -------------- Variables set during unit initialization ----------------
    public static MapLocation spawnTowerLocation;
    public static Direction spawnDirection;

    // -------------- Variables that vary by turn
    // Game state info
    public static int phase;
    public static int chips;
    public static int chipsRate;
    // Comms info
    // Internal infos

    // External infos
    public static RobotInfo[] nearbyAllies;
    public static RobotInfo[] nearbyEnemies;
    public static MapLocation[] nearbyRuins;
    public static MapLocation nearestPaintTower = null;
    public static MapInfo[] nearbyTiles;

    // -------------- Methods --------------
    public Robot(RobotController r){
        rc = r;
        // messageUnit = new MessageUnit(this);
        Debug.init();
        Pathfinder.init(rc);
    }

    // Only runs on a unit's first turn
    public void init() throws GameActionException{
        Debug.print(0, "Create unit => " + rc.getType() + " at " + rc.getLocation());
        for(Action action: actions){
            action.initUnit();
        }
    }

    public void initTurn() throws GameActionException {
        MAP_WIDTH = rc.getMapWidth();
        MAP_HEIGHT = rc.getMapHeight();
        MAP_AREA = MAP_WIDTH * MAP_HEIGHT;

        phase = Phase.getPhase(rc.getRoundNum(), MAP_AREA);
        chips = rc.getChips();
        chipsRate = ChipProductionRate.calculate();

        nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
        nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent()); //Bytecode improvement possible
        nearbyTiles = rc.senseNearbyMapInfos();
        if (rc.getType().isRobotType()) {
            nearbyRuins = rc.senseNearbyRuins(-1);
        }

        //update nearestPaintTower (assumes the last paintTower we passed by is closest)
        for (RobotInfo robot : nearbyAllies) {
            if (robot.getType().isTowerType() && (Utils.getTowerType(robot.getType()) == Utils.towerType.PAINT_TOWER)) {
                    nearestPaintTower = robot.getLocation();
            }
        }
    }

    public void playTurn() throws GameActionException {
        Debug.reset(rc.getRoundNum()); // Reset clock to benchmark

        Debug.print(0, "Start turn => " + rc.getType() + " at " + rc.getLocation());
        Debug.print(1, "Init.");
        initTurn();

        Debug.print(1, "");
        Debug.print(1, "Calculate actions.");
        Action bestActionAction = null;
        Action bestMoveAction = null;
        Action bestComboAction = null;

        int bestActionScore = 0;
        int bestMoveScore = 0;
        int bestComboScore = 0;

        for(Action action: actions){
            Debug.print(2, action.name + " ...", action.debugAction);
            action.calcScore();
            int score = action.getScore();
            switch (action.cooldown_reqs){
                case 1:
                    if(score > bestActionScore){
                        bestActionScore = score;
                        bestActionAction = action;
                    }
                    break;
                case 2:
                    if(score > bestMoveScore){
                        bestMoveScore = score;
                        bestMoveAction = action;
                    }
                    break;
                case 3:
                    if(score > bestComboScore){
                        bestComboScore = score;
                        bestComboAction = action;
                    }
                    break;
            }

            Debug.print(2,  "SCORE : " + score, action.debugAction);
            Debug.print(2,  "", action.debugAction);
        }

        Debug.print(1, "");
        if(bestActionScore > 0 || bestMoveScore > 0 || bestComboScore > 0){
            if (bestComboScore > bestActionScore + bestMoveScore){
                Debug.print(1, "Playing combo action: " + bestComboAction.name + " with directionScores " + bestComboScore);
                bestComboAction.play();
                Debug.setActionIndicatorString(bestComboAction, bestComboScore);
            } else {
                if (bestActionScore > 0) {
                    Debug.print(1, "Playing action: " + bestActionAction.name + " with directionScores " + bestActionScore);
                    bestActionAction.play();
                }
                if (bestMoveScore > 0) {
                    Debug.print(1, "Playing move: " + bestMoveAction.name + " with directionScores " + bestMoveScore);
                    bestMoveAction.play();
                }
                Debug.setActionIndicatorString(bestActionAction, bestMoveAction, bestActionScore, bestMoveScore);
            }
        } else {
            Debug.print(1, "No action to play");
        }
    }

    public void endTurn(){
        Debug.print(0, "End turn.");
    }
}