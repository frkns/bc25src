// modified from our 2023 code
package ryan;

import battlecode.common.*;
import ryan.fast.*;


public class MapRecorder extends RobotPlayer {
    // Sets to track wall locations that have been reported and need to be reported
    private static int W = rc.getMapWidth();
    private static int H = rc.getMapHeight();
    private static FastLocSet reportedWalls = new FastLocSet();
    private static FastIterableLocSet knownEmptyRuins = new FastIterableLocSet();
    private static FastIterableLocSet knownAlliedTowers = new FastIterableLocSet();
    private static FastIterableLocSet knownEnemyTowers = new FastIterableLocSet();

    // Symmetry tracking variables
    private static boolean symConfirmed = false; // Whether map symmetry has been confirmed
    private static boolean needWallFlip = false; // Whether walls need to be mirrored across symmetry line
    private static int wallFlipIndex = -1; // Index for processing wall flipping

    // Bit flags for map cell states
    public static final char SEEN_BIT = 1; // Marks cell as seen
    public static final char WALL_BIT = 1 << 1; // Marks cell as wall

    // Pre-allocated strings for efficient char array initialization
    // Creating strings of specific lengths to avoid array allocation costs
    public static final String ONE_HUNDRED_LEN_STRING = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
    public static final String SIX_HUNDRED_LEN_STRING = ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING;
    public static final String STRING_LEN_3600 = SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING;

    // Map state array: 0=unseen, 1=no wall, 2=wall
    public static char[] vals = STRING_LEN_3600.toCharArray();

    // Symmetry state: bits represent eliminated symmetries (100=rotational, 010=vertical, 001=horizontal)
    private static int symmetry;

    // Check if a location is passable
    public static boolean getPassible(MapLocation loc) {
        int val = vals[Utils.loc2int(loc)];
        if (val == WALL_BIT)
            return false;
        return true;
    }

    // Get raw data value for a location
    public static int getData(MapLocation loc) {
        return vals[Utils.loc2int(loc)];
    }

    // Record map data and check symmetry until bytecode limit
    public static void recordSym(int leaveBytecodeCnt) throws GameActionException {
        // Process pending wall flips if symmetry was just confirmed
        if (needWallFlip) {
            if (wallFlipIndex == -1) {
                wallFlipIndex = reportedWalls.size;
            }
            // Mirror each known wall across symmetry line
            for (; --wallFlipIndex >= 0; ) {
                MapLocation loc = reportedWalls.pop();
                MapLocation symloc = getSymmetricLoc(loc);
                if (!reportedWalls.contains(symloc)) {
                    reportedWalls.add(symloc);
                    vals[Utils.loc2int(symloc)] = WALL_BIT;
                }
                if (Clock.getBytecodesLeft() <= leaveBytecodeCnt) {
                    return;
                }
            }
            needWallFlip = false;
        }

        // Scan and process nearby map information
        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (int i = infos.length; --i >= 0; ) {
            if (Clock.getBytecodesLeft() <= leaveBytecodeCnt) {
                return;
            }
            MapInfo info = infos[i];
            MapLocation loc = info.getMapLocation();

            // Track ruin locations
            if (info.hasRuin()) {
                // Is it an allied tower, enemy tower, or empty ruin
                RobotInfo robot = rc.senseRobotAtLocation(loc);
                if (robot != null) {
                    if (robot.getTeam() == rc.getTeam()) {
                        knownAlliedTowers.add(loc);
                        knownEnemyTowers.remove(loc);
                        knownEmptyRuins.remove(loc);
                    } else {
                        knownEnemyTowers.add(loc);
                        knownAlliedTowers.remove(loc);
                        knownEmptyRuins.remove(loc);
                    }
                } else {
                    knownEmptyRuins.add(loc);
                    knownAlliedTowers.remove(loc);
                    knownEnemyTowers.remove(loc);
                }
            }

            int locID = Utils.loc2int(loc);
            if (vals[locID] != 0)
                continue;

            // Record walls and update shared knowledge
            if (info.isWall()) {
                vals[locID] = WALL_BIT;
                if (!reportedWalls.contains(loc)) {
                    reportedWalls.add(loc);
                }
                if (symConfirmed) {
                    MapLocation symloc = getSymmetricLoc(loc);
                    reportedWalls.add(symloc);
                    vals[Utils.loc2int(symloc)] = WALL_BIT;
                }
            } else {
                vals[locID] = SEEN_BIT;
            }

            // Verify map symmetry if not yet confirmed
            if (!symConfirmed) {
                for (int sym = 0b100; sym > 0; sym >>= 1) {
                    if ((sym & symmetry) > 0)
                        continue;
                    MapLocation symloc = getSymmetricLoc(loc, 0b111 - sym);
                    int symVal = vals[Utils.loc2int(symloc)];
                    if (symVal == 0) {
                        continue;
                    }

                    // Eliminate symmetries based on various conditions
                    if (vals[locID] != symVal) {
                        eliminateSym(sym, loc);
                    }
                }
            }
        }
    }

    // Initialize turn by processing shared information
    public static void initTurn() throws GameActionException {
        updateSym();
    }

    // Update symmetry information from shared memory
    public static void updateSym() throws GameActionException {
//        if (Comms.readSymmetrySym() != symmetry) {
//            symmetry |= Comms.readSymmetrySym();
//
//            // Update opponent spawn locations based on symmetry
//            Robot.oppSpawnCenters[0] = getSymmetricLoc(Robot.mySpawnCenters[0]);
//            Robot.oppSpawnCenters[1] = getSymmetricLoc(Robot.mySpawnCenters[1]);
//            Robot.oppSpawnCenters[2] = getSymmetricLoc(Robot.mySpawnCenters[2]);
//
//            // Check if symmetry is confirmed
//            switch (symmetry) {
//                case 0b011: case 0b101: case 0b110:
//                    symConfirmed = true;
//                    needWallFlip = true;
//            }
//        }
    }

    // Eliminate a potential symmetry and update shared knowledge
    private static void eliminateSym(int sym, MapLocation loc) throws GameActionException {
        symmetry |= sym;  // Add new symmetry to eliminated symmetries
        
        // Check if symmetry is confirmed after elimination
        switch (symmetry) {
            case 0b011: case 0b101: case 0b110:
                symConfirmed = true;
                needWallFlip = true;
                break;
        }
        
        Debug.println(Debug.INFO, String.format("eliminate sym %d at %d %d now sym=%d", sym, loc.x, loc.y, symmetry));
    }

    // Get symmetric location for a given symmetry type
    public static MapLocation getSymmetricLoc(MapLocation loc, int sym) {
        switch (sym) {
            case 0b000: case 0b001: case 0b010: case 0b011: // rotational
                return new MapLocation(W - loc.x - 1, H - loc.y - 1);
            case 0b100: case 0b101: // vertical
                return new MapLocation(loc.x, H - loc.y - 1);
            case 0b110:  // horizontal
                return new MapLocation(W - loc.x - 1, loc.y);
        }
        assert false;
        return null;
    }

    public static MapLocation getSymmetricLoc(MapLocation loc) {
        switch (symmetry) {
            case 0b000: case 0b001: case 0b010: case 0b011: // rotational
                return new MapLocation(W - loc.x - 1, H - loc.y - 1);
            case 0b100: case 0b101: // vertical
                return new MapLocation(loc.x, H - loc.y - 1);
            case 0b110:  // horizontal
                return new MapLocation(W - loc.x - 1, loc.y);
        }
        Debug.failFast("impossible sym" + symmetry);
        return new MapLocation(W - loc.x - 1, H - loc.y - 1);
    }
}
