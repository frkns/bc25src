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

    // Comms info
    public static FastLocSet tilesToMark = new FastLocSet();
    public static FastLocSet tilesToUnmark = new FastLocSet();

    // Internal info
    public static MapLocation robotLoc;
    public static boolean isActionReady;
    public static boolean isMovementReady;
    public static int getPaint;

    // External info
    public static RobotInfo[] nearbyAllies;
    public static RobotInfo[] nearbyEnemies;
    public static MapLocation[] nearbyRuins;
    public static MapLocation nearestPaintTower = null;
    public static MapInfo[] nearbyTiles;
    public static MapLocation[] nearbyTileLocsR4;

    public static FastIterableLocSet knownRuins = new FastIterableLocSet(); // knows ruins, with or without tower
    public static FastIterableLocSet knownEmptyRuins = new FastIterableLocSet(); // ruins without towers
    public static FastIterableLocSet knownTowers = new FastIterableLocSet(); // ruins with tower

    // Memory

    // --------- SRP only ----------
    public static MapLocation srpCenter;
    public static FastLocSet srpMarkedCorners = new FastLocSet();

    //An SRP center cannot be within a distanceSquared of 4 of any of these tiles.
    public static FastLocSet enemyPaintTiles = new FastLocSet(); // Only for enemy paint. Pop and add tiles in 5x5 area to tempInvalidSrpCenters at the end of turn with extra bytecode
    public static FastLocSet blockerTiles = new FastLocSet(); //  Pop and add tiles in 5x5 area to InvalidSrpCenters at the end of turn with extra bytecode
    public static FastLocSet processedBlockerTiles = new FastLocSet(); // Keeps track of proccesed tiles so they don't get added again
    public static FastLocSet corners = new FastLocSet(); // Centers must be >= radiusSquared of 8 from corners.
    public static FastLocSet processedCorners = new FastLocSet(); // Keeps track of proccesed corners so they don't get added again
    // public static FastLocSet tempInvalidSrpCenters = new FastLocSet(); // Blocked by enemy paint. Clear every 25 turns.
    public static FastLocSet invalidSrpCenters = new FastLocSet();  // Blocked by permanent tiles


    public static MapLocation towerCenter;
    public static FastLocSet invalidTowerCenters = new FastLocSet(); // Includes completed tower centers & enemy paint blocking. Clear every 25 turns.
    public static FastLocSet completedPatterns = new FastLocSet();


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
        nearbyTileLocsR4 = rc.getAllLocationsWithinRadiusSquared(_Info.robotLoc, 4);
        if (rc.getType().isRobotType()) {
            nearbyRuins = rc.senseNearbyRuins(-1);

            for(MapLocation loc: nearbyRuins){
                knownRuins.add(loc);
            }


            for(MapLocation loc: nearbyRuins){
                if(rc.isLocationOccupied(loc)){
                    knownTowers.add(loc);
                    knownEmptyRuins.remove(loc);
                }else{
                    knownTowers.remove(loc);
                    knownRuins.add(loc);
                }
                knownRuins.add(loc);
            }
        }

        knownRuins.updateIterable();
        knownTowers.updateIterable();
        knownEmptyRuins.updateIterable();

        //update nearestPaintTower (assumes the last paintTower we passed by is closest)
        for (RobotInfo robot : nearbyAllies) {
            if (robot.getType().isTowerType() && (Utils.getTowerType(robot.getType()) == Utils.towerType.PAINT_TOWER)) {
                nearestPaintTower = robot.getLocation();
            }
        }

    }
    // -------------- End of turn communication functions ---------------
    public static void markTiles() throws GameActionException { // Currently ONLY for Srp
        if (tilesToMark.size() > 0) {
            MapLocation tileToMark = _Info.tilesToMark.top();
            if (rc.canMark(tileToMark)) {
                rc.mark(tileToMark, false);
                tilesToMark.remove(tileToMark);
                srpMarkedCorners.add(tileToMark); // remove this line to generalize
            }
            }
        }

    public static void unmarkTiles() throws GameActionException { // Currently ONLY for Srp
        if (srpMarkedCorners.size() > 0) {
            MapLocation tileToUnmark = srpMarkedCorners.top();
            if (rc.canRemoveMark(tileToUnmark)) {
                rc.removeMark(tileToUnmark);
                srpMarkedCorners.remove(tileToUnmark); // remove this line to generalize
            }
        }
    }
    // -------------- End of turn bytecode intensive functions ---------------
    public static void processBlockerTiles() {
        MapLocation blockerTile; // 1000 bytecode per blocker tile
        while ((blockerTile = blockerTiles.pop()) != null && Clock.getBytecodesLeft() > 1000) {            
            invalidSrpCenters.add(blockerTile.x - 2, blockerTile.y - 2);
            invalidSrpCenters.add(blockerTile.x - 2, blockerTile.y - 1);
            invalidSrpCenters.add(blockerTile.x - 2, blockerTile.y);
            invalidSrpCenters.add(blockerTile.x - 2, blockerTile.y + 1);
            invalidSrpCenters.add(blockerTile.x - 2, blockerTile.y + 2);
            
            invalidSrpCenters.add(blockerTile.x - 1, blockerTile.y - 2);
            invalidSrpCenters.add(blockerTile.x - 1, blockerTile.y - 1);
            invalidSrpCenters.add(blockerTile.x - 1, blockerTile.y);
            invalidSrpCenters.add(blockerTile.x - 1, blockerTile.y + 1);
            invalidSrpCenters.add(blockerTile.x - 1, blockerTile.y + 2);
            
            invalidSrpCenters.add(blockerTile.x, blockerTile.y - 2);
            invalidSrpCenters.add(blockerTile.x, blockerTile.y - 1);
            invalidSrpCenters.add(blockerTile.x, blockerTile.y);
            invalidSrpCenters.add(blockerTile.x, blockerTile.y + 1);
            invalidSrpCenters.add(blockerTile.x, blockerTile.y + 2);
            
            invalidSrpCenters.add(blockerTile.x + 1, blockerTile.y - 2);
            invalidSrpCenters.add(blockerTile.x + 1, blockerTile.y - 1);
            invalidSrpCenters.add(blockerTile.x + 1, blockerTile.y);
            invalidSrpCenters.add(blockerTile.x + 1, blockerTile.y + 1);
            invalidSrpCenters.add(blockerTile.x + 1, blockerTile.y + 2);
            
            invalidSrpCenters.add(blockerTile.x + 2, blockerTile.y - 2);
            invalidSrpCenters.add(blockerTile.x + 2, blockerTile.y - 1);
            invalidSrpCenters.add(blockerTile.x + 2, blockerTile.y);
            invalidSrpCenters.add(blockerTile.x + 2, blockerTile.y + 1);
            invalidSrpCenters.add(blockerTile.x + 2, blockerTile.y + 2);

            processedBlockerTiles.add(blockerTile);
        }
    }
    public static void processCorners(){
        MapLocation corner; // 1000 bytecode per corner
        while ((corner = corners.pop()) != null && Clock.getBytecodesLeft() > 1000) {            
            invalidSrpCenters.add(corner.x - 2, corner.y - 1);
            invalidSrpCenters.add(corner.x - 2, corner.y);
            invalidSrpCenters.add(corner.x - 2, corner.y + 1);
            
            invalidSrpCenters.add(corner.x - 1, corner.y - 2);
            invalidSrpCenters.add(corner.x - 1, corner.y - 1);
            invalidSrpCenters.add(corner.x - 1, corner.y);
            invalidSrpCenters.add(corner.x - 1, corner.y + 1);
            invalidSrpCenters.add(corner.x - 1, corner.y + 2);
            
            invalidSrpCenters.add(corner.x, corner.y - 2);
            invalidSrpCenters.add(corner.x, corner.y - 1);
            invalidSrpCenters.add(corner.x, corner.y);
            invalidSrpCenters.add(corner.x, corner.y + 1);
            invalidSrpCenters.add(corner.x, corner.y + 2);
            
            invalidSrpCenters.add(corner.x + 1, corner.y - 2);
            invalidSrpCenters.add(corner.x + 1, corner.y - 1);
            invalidSrpCenters.add(corner.x + 1, corner.y);
            invalidSrpCenters.add(corner.x + 1, corner.y + 1);
            invalidSrpCenters.add(corner.x + 1, corner.y + 2);
            
            invalidSrpCenters.add(corner.x + 2, corner.y - 1);
            invalidSrpCenters.add(corner.x + 2, corner.y);
            invalidSrpCenters.add(corner.x + 2, corner.y + 1);

            processedCorners.add(corner);
        }
    }
}


