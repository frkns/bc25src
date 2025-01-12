package e_action.knowledge;

import battlecode.common.*;

import java.util.Random;
import e_action.Robot;
import e_action.utils.*;

public class _Info {
    public static RobotController rc = Robot.rc;
    // -------------- Useful constants --------------
    public static final Random rng = new Random(rc.getID());
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER,
    };

    // -------------- Variables that vary by game --------------
    public static int MAP_WIDTH;
    public static int MAP_HEIGHT;
    public static int MAP_AREA;

    // -------------- Variables set during unit initialization ----------------
    public static MapLocation spawnTowerLocation;
    public static Direction spawnDirection;
    public static UnitType unitType;
    public static int actionRadiusSquared;

    // -------------- Variables that vary by turn ----------------
    // Game state info
    public static int phase;
    public static int chips;
    public static int chipsRate;
    // Comms info

    // Internal info
    public static MapLocation robotLoc;

    // External info
    public static RobotInfo[] nearbyAllies;
    public static RobotInfo[] nearbyEnemies;
    public static MapLocation[] nearbyRuins;
    public static MapLocation nearestPaintTower = null;
    public static MapInfo[] nearbyTiles;




    public static void init() {
        // -------------- Variables that vary by game  ----------------
        MAP_WIDTH = rc.getMapWidth();
        MAP_HEIGHT = rc.getMapHeight();
        MAP_AREA = MAP_WIDTH * MAP_HEIGHT;
        // -------------- Variables set during unit initialization ----------------
        unitType = rc.getType();
        actionRadiusSquared = unitType.actionRadiusSquared;

    }

    // -------------- Variables that vary by turn ----------------
    public static void update() throws GameActionException {
        // ---------- Game state info ------------
        phase = Phase.getPhase(rc.getRoundNum(), MAP_AREA);
        chips = rc.getChips();
        chipsRate = ChipProductionRate.calculate();
        // ------------ Comms info ------------

        // ----------- Internal info ------------
        robotLoc = rc.getLocation();

        // ----------- External info ------------
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
}

