// Adapted from https://github.com/chenyx512/battlecode24/blob/main/src/bot1/PathFinder.java
package ryan;

import battlecode.common.*;

import ryan.fast.*;

public class Pathfinder extends RobotPlayer{

    static MapLocation target = null;
    static int stuckCnt;


    public static void tryMove(Direction dir) throws GameActionException {
        if (dir == Direction.CENTER)
            return;
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public static void move(MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady() || loc == null){
//            Debug.println(Debug.PATHFINDER, "Null return: Movement not ready or loc is null");
            return;
        }
        target = loc;
        if (rc.getLocation().equals(target)) return; // Don't move if already at target location
        Direction dir = BugNav.getMoveDir();
        if (dir == null)
            return;
        tryMove(dir);
    }

    public static Direction getMoveDir(MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady() || loc == null)
            return null;
        target = loc;
        Direction dir = BugNav.getMoveDir();
        if (dir == null) {
            return null;
        }
        return dir;
    }


    public static class BugNav {
        static DirectionStack dirStack = new DirectionStack();
        static MapLocation prevTarget = null; // previous target
        static FastLocSet visitedLocs = new FastLocSet();
        static int currentTurnDir = 0;
        static int stackDepthCutoff = 8;
        static final int MAX_DEPTH = 20;
        static final int BYTECODE_CUTOFF = 1000;
        static int lastMoveRound = -1;

        static Direction turn(Direction dir) {
            return currentTurnDir == 0 ? dir.rotateLeft() : dir.rotateRight();
        }

        static Direction turn(Direction dir, int turnDir) {
            return turnDir == 0 ? dir.rotateLeft() : dir.rotateRight();
        }

        static Direction getMoveDir() throws GameActionException {
            if (rc.getRoundNum() == lastMoveRound) {
                Debug.println(Debug.PATHFINDER, "Null return: Already moved this round");
                return null;
            } else {
                lastMoveRound = rc.getRoundNum();
            }

            // Clear previous data if new target
            if (prevTarget == null || target.distanceSquaredTo(prevTarget) > 2) {
                resetPathfinding();
            }

            prevTarget = target;
            if (visitedLocs.contains(rc.getLocation())) {
                stuckCnt++;
            } else {
                stuckCnt = 0;
                visitedLocs.add(rc.getLocation());
            }
            if (dirStack.size == 0) {
                stackDepthCutoff = 8;
                Direction dir = rc.getLocation().directionTo(target);
                if (shouldMove(dir)) {
                    return dir;
                }
                MapLocation loc = rc.getLocation().add(dir);
                // Try to sidestep enemy or empty paint
                if (rc.canSenseLocation(loc) && !rc.senseMapInfo(loc).getPaint().isAlly()) {
                    Direction dirL = dir.rotateLeft();
                    MapLocation locL = rc.getLocation().add(dirL);
                    Direction dirR = dir.rotateRight();
                    MapLocation locR = rc.getLocation().add(dirR);
                    if (target.distanceSquaredTo(locL) < target.distanceSquaredTo(locR)) {
                        if (shouldMove(dirL)) {
                            return dirL;
                        }
                        if (shouldMove(dirR)) {
                            return dirR;
                        }
                    } else {
                        if (shouldMove(dirR)) {
                            return dirR;
                        }
                        if (shouldMove(dirL)) {
                            return dirL;
                        }
                    }
                }

                currentTurnDir = getTurnDir(dir);
                // obstacle encountered, rotate and add new dirs to stack
                while (!shouldMove(dir) && dirStack.size < 8) {
                    if (!rc.onTheMap(rc.getLocation().add(dir))) {
                        currentTurnDir ^= 1;
                        dirStack.clear();
                        Debug.println(Debug.PATHFINDER, "Null return: Hit map boundary");
                        return null; // do not move
                    }
                    dirStack.push(dir);
                    dir = turn(dir);
                }
                if (dirStack.size != 8) {
                    return dir;
                }
            } else {
                // dxx
                // xo
                // x
                // suppose you are at o, x is wall, and d is another duck, you are pathing left and bugging up rn
                // and the duck moves away, you wanna take its spot
                if (dirStack.size > 1 && shouldMove(dirStack.top(2))) {
                    dirStack.pop(2);
                } else if (dirStack.size == 1 && shouldMove(turn(dirStack.top(), 1 - currentTurnDir))) {
                    /*
                    consider bugging down around x and turning left to the location above y,
                    the stack will contain a single direction that is down,
                    which is blocked by y, without this special case it will turn left above y and go up
                    w00w
                    wx0w
                    w0yw
                    w00w
                     */
                    Direction d = turn(dirStack.top(), 1 - currentTurnDir);
                    dirStack.pop();
                    return d;
                }
                while (dirStack.size > 0 && shouldMove(dirStack.top())) {
                    dirStack.pop();
                }
                if (dirStack.size == 0) {
                    Direction dir = rc.getLocation().directionTo(target);
                    if (shouldMove(dir)) {
                        return dir;
                    }
                    MapLocation loc = rc.getLocation().add(dir);
                    if (rc.canSenseLocation(loc) && rc.senseMapInfo(loc).getPaint().isEnemy()) {
                        Direction dirL = dir.rotateLeft();
                        MapLocation locL = rc.getLocation().add(dirL);
                        Direction dirR = dir.rotateRight();
                        MapLocation locR = rc.getLocation().add(dirR);
                        if (target.distanceSquaredTo(locL) < target.distanceSquaredTo(locR)) {
                            if (shouldMove(dirL)) {
                                return dirL;
                            }
                            if (shouldMove(dirR)) {
                                return dirR;
                            }
                        } else {
                            if (shouldMove(dirR)) {
                                return dirR;
                            }
                            if (shouldMove(dirL)) {
                                return dirL;
                            }
                        }
                    }
                    dirStack.push(dir);
                }
                // keep rotating and adding things to the stack
                Direction curDir;
                int stackSizeLimit = Math.min(DirectionStack.STACK_SIZE, dirStack.size + 8);
                while (dirStack.size > 0 && !shouldMove(curDir = turn(dirStack.top()))) {
                    if (!rc.onTheMap(rc.getLocation().add(curDir))) {
                        currentTurnDir ^= 1;
                        dirStack.clear();
                        Debug.println(Debug.PATHFINDER, "Null return: Hit map boundary");
                        return null; // do not move
                    }
                    dirStack.push(curDir);
                    if (dirStack.size == stackSizeLimit) {
                        dirStack.clear();
                        Debug.println(Debug.PATHFINDER, "Null return: Stack size limit reached");
                        return null;
                    }
                }
                if (dirStack.size >= stackDepthCutoff) {
                    int cutoff = stackDepthCutoff + 8;
                    dirStack.clear();
                    stackDepthCutoff = cutoff;
                }
                Direction moveDir = dirStack.size == 0 ? dirStack.dirs[0] : turn(dirStack.top());
                if (shouldMove(moveDir)) {
                    return moveDir;
                }
            }
            Debug.println(Debug.PATHFINDER, "Null return: Final moveDir cannot be moved to");
            Debug.println(Debug.PATHFINDER, "Robot location: " + rc.getLocation());
            Debug.println(Debug.PATHFINDER, "Target location: " + target);
            return null;
        }

        static int simulate(int turnDir, Direction dir) throws GameActionException {
            int originalTurnDir = turnDir;
            MapLocation now = rc.getLocation();
            DirectionStack dirStack = new DirectionStack();
            while (!canPass(now, dir) && dirStack.size < 8) {
                dirStack.push(dir);
                dir = turn(dir, turnDir);
            }
            now = now.add(dir);
            int ans = 1;

            while (!now.isAdjacentTo(target)) {
                if (ans > MAX_DEPTH || Clock.getBytecodesLeft() < BYTECODE_CUTOFF) {
                    break;
                }
                if (now != null) {
                    rc.setIndicatorDot(now, originalTurnDir == 0? 255 : 0, 0, originalTurnDir == 0? 0 : 255);
                }
                Direction moveDir = now.directionTo(target);
                if (dirStack.size == 0) {
                    if (!canPass(now, moveDir)) {
                        Direction dirL = moveDir.rotateLeft();
                        MapLocation locL = now.add(dirL);
                        Direction dirR = moveDir.rotateRight();
                        MapLocation locR = now.add(dirR);
                        if (target.distanceSquaredTo(locL) <= target.distanceSquaredTo(locR)) {
                            if (canPass(now, dirL)) {
                                moveDir = dirL;
                            }  else {
                                while (!canPass(now, moveDir) && dirStack.size < 8) {
                                    dirStack.push(moveDir);
                                    moveDir = turn(moveDir, 0);
                                }
                                turnDir = 0;
                            }
                        } else {
                            if (canPass(now, dirR)) {
                                moveDir = dirR;
                            }  else {
                                while (!canPass(now, moveDir) && dirStack.size < 8) {
                                    dirStack.push(moveDir);
                                    moveDir = turn(moveDir, 1);
                                }
                                turnDir = 1;
                            }
                        }
                    }
                } else {
                    if (dirStack.size > 1 && canPass(now, dirStack.top(2))) {
                        dirStack.pop(2);
                    } else if (dirStack.size == 1 && canPass(now, turn(dirStack.top(), 1 - turnDir))) {
                        moveDir = turn(dirStack.top(), 1 - turnDir);
                        dirStack.pop();
                    }
                    while (dirStack.size > 0 && canPass(now, dirStack.top())) {
                        dirStack.pop();
                    }

                    if (dirStack.size == 0) {
                        if (!canPass(now, moveDir)) {
                            Direction dirL = moveDir.rotateLeft();
                            MapLocation locL = now.add(dirL);
                            Direction dirR = moveDir.rotateRight();
                            MapLocation locR = now.add(dirR);
                            if (target.distanceSquaredTo(locL) <= target.distanceSquaredTo(locR)) {
                                if (canPass(now, dirL)) {
                                    moveDir = dirL;
                                } else if (canPass(now, dirR)) {
                                    moveDir = dirR;
                                }
                            } else {
                                if (canPass(now, dirR)) {
                                    moveDir = dirR;
                                }  else if (canPass(now, dirL)) {
                                    moveDir = dirL;
                                }
                            }
                        }
                        if (!canPass(now, moveDir)) {
                            dirStack.push(moveDir);
                        }
                    }
                    while (dirStack.size > 0 && !canPass(now, turn(dirStack.top(), turnDir))) {
                        dirStack.push(turn(dirStack.top(), turnDir));
                        if (dirStack.size > 8) {
                            return -1;
                        }
                    }
                    moveDir = dirStack.size == 0 ? dirStack.dirs[0] : turn(dirStack.top(), turnDir);
                }
                now = now.add(moveDir);
                ans++;
            }
            return ans + Utils.manhattanDistance(now, target);
        }

        static int getTurnDir(Direction dir) throws GameActionException {
            int ansL = simulate(0, dir);
            int ansR = simulate(1, dir);
            if (ansL == ansR) return FastMath.rand256() % 2;
            if ((ansL <= ansR && ansL != -1) || ansR == -1) {
                return 0;
            } else {
                return 1;
            }
        }

        // clear some of the previous data
        public static void resetPathfinding() {
            stackDepthCutoff = 8;
            dirStack.clear();
            stuckCnt = 0;
            visitedLocs.clear();
        }

        static boolean shouldMove(Direction dir) throws GameActionException {
            MapLocation loc = rc.getLocation().add(dir);
            if (rc.canMove(dir)) {
                MapInfo info = rc.senseMapInfo(loc);  // If we can move there it is on the map and we can sense it
                if (inTowerRange(loc)){ // Logic for avoiding towers is complex so let's just minimize total damage
                    return false;
                }
                if (info.getPaint().isEnemy()) {
                    MapLocation leftLoc = rc.getLocation().add(dir.rotateLeft());
                    MapLocation rightLoc = rc.getLocation().add(dir.rotateRight());
                    if ((rc.canSenseLocation(leftLoc) && !inTowerRange(leftLoc) && !rc.senseMapInfo(leftLoc).getPaint().isEnemy() && rc.sensePassability(leftLoc)) 
                    || (rc.canSenseLocation (rightLoc) && !inTowerRange(rightLoc) && !rc.senseMapInfo(rightLoc).getPaint().isEnemy() && rc.sensePassability(rightLoc))) 
                        return false;

                    // We must be allowed to cross paint or enter tower range if it is adjacent to two non-connected walls
                    int wall_length = 0;
                    int wall_cnt = 0;
                    for (Direction d : directions) {
                        MapLocation adjLoc = loc.add(d);
                        if (!rc.sensePassability(adjLoc)) {
                            wall_length++;
                        } else {
                            if (wall_length > 0) {
                                wall_length = 0;
                                wall_cnt++;
                            }
                        }
                    }
                    if (wall_cnt > 1)
                        return true;

                    // we must be allowed to cross paint or enter tower range if it blocks a passage between EW or NS
                    MapLocation N = loc.add(Direction.NORTH);
                    MapLocation E = loc.add(Direction.EAST);
                    MapLocation S = loc.add(Direction.SOUTH);
                    MapLocation W = loc.add(Direction.WEST);
                    boolean canN = rc.canSenseLocation(N) && (!rc.senseMapInfo(N).getPaint().isEnemy() && rc.sensePassability(N) && !inTowerRange(N));
                    boolean canE = rc.canSenseLocation(E) && (!rc.senseMapInfo(E).getPaint().isEnemy() && rc.sensePassability(E) && !inTowerRange(E));
                    boolean canS = rc.canSenseLocation(S) && (!rc.senseMapInfo(S).getPaint().isEnemy() && rc.sensePassability(S) && !inTowerRange(S));
                    boolean canW = rc.canSenseLocation(W) && (!rc.senseMapInfo(W).getPaint().isEnemy() && rc.sensePassability(W) && !inTowerRange(W));
                    if (!canN && !canS) return true;
                    if (!canE && !canW) return true;
                    return false;
                }
                return true;
            }
            return false;
        }
    }

        // Not in range of any towers
        static boolean inTowerRange(MapLocation loc) throws GameActionException{
            if (nearestEnemyTower != null){
                int towerRange = 9;
                if (nearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ||
                    nearestEnemyTowerType == UnitType.LEVEL_TWO_DEFENSE_TOWER ||
                    nearestEnemyTowerType == UnitType.LEVEL_THREE_DEFENSE_TOWER) {
                    towerRange = 16;
                }
                if (loc.isWithinDistanceSquared(nearestEnemyTower, towerRange)) {
                    return true;
                }
            }
            if (sndNearestEnemyTower != null) {
                int sndTowerRange = 9;
                if (sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ||
                    sndNearestEnemyTowerType == UnitType.LEVEL_TWO_DEFENSE_TOWER ||
                    sndNearestEnemyTowerType == UnitType.LEVEL_THREE_DEFENSE_TOWER) {
                    sndTowerRange = 16;
                }
                if (loc.isWithinDistanceSquared(sndNearestEnemyTower, sndTowerRange)){
                    return true;
                }
            }
            return false;
        }
        static boolean canPass(MapLocation loc, Direction targetDir) throws GameActionException {
            MapLocation newLoc = loc.add(targetDir);
            if (!rc.onTheMap(newLoc))
                return false;
            if (inTowerRange(newLoc)){
                return false;
            }
            if (rc.canSenseLocation(newLoc)) {
                return rc.senseMapInfo(newLoc).isPassable();
            } else {
                return MapRecorder.getPassible(newLoc);
            }
        }
    }


class DirectionStack {
    static int STACK_SIZE = 60;
    int size = 0;
    Direction[] dirs = new Direction[STACK_SIZE];

    final void clear() {
        size = 0;
    }

    final void push(Direction d) {
        dirs[size++] = d;
    }

    final Direction top() {
        return dirs[size - 1];
    }

    /**
     * Returns the top n element of the stack
     */
    final Direction top(int n) {
        return dirs[size - n];
    }

    final void pop() {
        size--;
    }

    final void pop(int n) {
        size -= n;
    }
}
