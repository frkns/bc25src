// https://github.com/chenyx512/battlecode24/blob/main/src/bot1/MapRecorder.java
package ryan;

import battlecode.common.*;


public class MapRecorder extends RobotPlayer {
    // Sets to track wall locations that have been reported and need to be reported
    private static int W = rc.getMapWidth();
    private static int H = rc.getMapHeight();
    private static FastLocSet knownWalls = new FastLocSet();
    private static FastIterableLocSet knownRuins = new FastIterableLocSet(); // Will break if robot learns of more than 100 ruins
    private static FastIterableLocSet knownEmptyRuins = new FastIterableLocSet();
    private static FastIterableLocSet knownAlliedTowers = new FastIterableLocSet();
    private static FastIterableLocSet knownEnemyTowers = new FastIterableLocSet();

    // Symmetry tracking variables
    private static boolean symConfirmed = false; // Whether map symmetry has been confirmed
    private static boolean needWallFlip = false; // Whether walls & ruins need to be mirrored across symmetry line
    private static int wallFlipIndex = -1; // Index for processing wall flipping
    private static int ruinFlipIndex = -1; // Index for processing ruin flipping

    // Bit flags for map cell states
    public static final char SEEN_BIT = 1; // Marks cell as seen
    public static final char WALL_BIT = 1 << 1; // Marks cell as wall

    // Pre-allocated strings for efficient char array initialization
    // Creating strings of specific lengths to avoid array allocation costs
    public static final String ONE_HUNDRED_LEN_STRING = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
    public static final String SIX_HUNDRED_LEN_STRING = ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING + ONE_HUNDRED_LEN_STRING;
    public static final String STRING_LEN_4200 = SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING + SIX_HUNDRED_LEN_STRING;

    // Map state array: 0=unseen, 1=no wall, 2=wall or ruin
    public static char[] vals = STRING_LEN_4200.toCharArray();

    // Symmetry state: bits represent eliminated symmetries (100=rotational, 010=vertical, 001=horizontal)
    private static int symmetry;

    // Returns the nearest enemy tower or the first enemy tower within a squared radius of 144
    // If no enemy tower locations are known, we assume they are symmetric to the known allied towers
    public static MapLocation getPotentialEnemyTower(){
        int nearestDist = 999999;
        MapLocation nearestEnemyTower = null;
        if (knownEnemyTowers.size > 0){
            knownEnemyTowers.updateIterable();
            for (int i = knownEnemyTowers.size; --i >= 0;){
                MapLocation loc = knownEnemyTowers.locs[i];
                int dist = rc.getLocation().distanceSquaredTo(loc);
                if (dist < nearestDist){
                    if (dist < 144) {
                        return loc;
                    } else {
                        nearestDist = dist;
                        nearestEnemyTower = loc;
                    }
                }
            }
            return nearestEnemyTower;
        } else if (knownAlliedTowers.size > 0 && symConfirmed){
            knownAlliedTowers.updateIterable();
            for (int i = knownAlliedTowers.size; --i >= 0;){
                MapLocation loc = getSymmetricLoc(knownAlliedTowers.locs[i]);
                if (!knownAlliedTowers.contains(loc) && !knownEmptyRuins.contains(loc)) {
                    int dist = rc.getLocation().distanceSquaredTo(loc);
                    if (dist < nearestDist) {
                        if (dist < 144) {
                            return loc;
                        } else {
                            nearestDist = dist;
                            nearestEnemyTower = loc;
                        }
                    }
                }
            }
            return nearestEnemyTower;
        } else {
            return null;
        }
    }


    // Check if a location is passable
    public static boolean getPassible(MapLocation loc) {
        int val = vals[Utils.loc2int(loc)];
        if (val == WALL_BIT)
            return false;
        return true;
    }

    // Record map data and check symmetry until bytecode limit
    public static void recordSym(int leaveBytecodeCnt) throws GameActionException {
        // Process pending wall flips if symmetry was just confirmed
        if (needWallFlip) {
            // Mirror each known wall across symmetry line
            if (wallFlipIndex == -1) {
                wallFlipIndex = knownWalls.size;
            }
            for (; --wallFlipIndex >= 0; ) {
                MapLocation loc = knownWalls.pop();
                MapLocation symloc = getSymmetricLoc(loc);
                knownWalls.add(symloc);
                vals[Utils.loc2int(symloc)] = WALL_BIT;
                if (Clock.getBytecodesLeft() <= leaveBytecodeCnt) {
                    return;
                }
            }
            // Mirror each known ruin across symmetry line
            if (ruinFlipIndex == -1) {
                ruinFlipIndex = knownRuins.size;
                knownRuins.updateIterable();
            }
            for (; --ruinFlipIndex >= 0; ) {
                MapLocation loc = knownRuins.locs[ruinFlipIndex];
                MapLocation symloc = getSymmetricLoc(loc);
                knownRuins.add(symloc);
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


            int locID = Utils.loc2int(loc);
            if (vals[locID] != 0) // We can skip if we've already seen the wall because walls never change
                continue;

            // Record ruin locations. Leave them marked as unseen as they are constantly updated
            if (info.hasRuin()) {
                knownRuins.add(loc);
                if (symConfirmed) {
                    MapLocation symloc = getSymmetricLoc(loc);
                    knownRuins.add(symloc);
                }
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

            if (info.isWall()) { // Record walls
                vals[locID] = WALL_BIT;
                knownWalls.add(loc);
                if (symConfirmed) {
                    MapLocation symloc = getSymmetricLoc(loc);
                    knownWalls.add(symloc);
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
        
//        Debug.println(Debug.INFO, String.format("eliminate sym %d at %d %d now sym=%d", sym, loc.x, loc.y, symmetry));
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
