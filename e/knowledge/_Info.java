package e.knowledge;

import battlecode.common.*;
import e.Robot;
import e.utils.Utils;
import e.utils.fast.FastIterableLocSet;
import e.utils.fast.FastLocSet;

import java.util.Random;

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
    public static int id;
    public static UnitType unitType;
    public static int actionRadiusSquared;

    // -------------- Variables that vary by turn ----------------
    // Game state info
    public static int round;
    public static int chips;
    public static int chipsRate;

    // Internal info
    public static MapLocation robotLoc;
    public static boolean isActionReady;
    public static boolean isMovementReady;


    // External info
    public static RobotInfo[] nearbyAllies;
    public static RobotInfo[] nearbyEnemies;
    public static MapInfo[] nearbyTiles;
    public static MapLocation[] nearbyRuins;


    // Memory
    public static FastIterableLocSet knownRuins = new FastIterableLocSet(); // knows ruins, with or without tower
    public static FastIterableLocSet knownEmptyRuins = new FastIterableLocSet(); // ruins without towers
    public static FastIterableLocSet knownTowers = new FastIterableLocSet(); // ruins with tower

    public static void init() {
        // -------------- Variables that vary by game  ----------------
        MAP_WIDTH = rc.getMapWidth();
        MAP_HEIGHT = rc.getMapHeight();
        MAP_AREA = MAP_WIDTH * MAP_HEIGHT;

        // -------------- Variables set during unit initialization ----------------
        id = rc.getID();
        unitType = rc.getType();
        actionRadiusSquared = unitType.actionRadiusSquared;
    }

    // -------------- Variables that vary by turn ----------------
    public static void update() throws GameActionException {
        // ---------- Game state info ------------
        round = rc.getRoundNum();
        chips = rc.getChips();
        chipsRate = ChipProductionRate.calculate();
        // ------------ Comms info ------------

        // ----------- Internal info ------------
        robotLoc = rc.getLocation();
        isActionReady = rc.isActionReady();
        isMovementReady = rc.isMovementReady();

        // ----------- External info ------------
        nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
        nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        nearbyTiles = rc.senseNearbyMapInfos();
        if (rc.getType().isRobotType()) {
            // Update known ruin locations
            nearbyRuins = rc.senseNearbyRuins(-1);
            for(MapLocation loc: nearbyRuins){
                knownRuins.add(loc);
                if(rc.isLocationOccupied(loc)){
                    knownTowers.add(loc);
                    knownEmptyRuins.remove(loc);
                } else {
                    knownTowers.remove(loc);
                    knownEmptyRuins.add(loc);
                }
            }
            knownRuins.updateIterable();
            knownTowers.updateIterable();
            knownEmptyRuins.updateIterable();
        }

    }
    // -------------- End of turn communication functions ---------------
    // -------------- End of turn bytecode intensive functions ---------------

}


