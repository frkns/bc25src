// Adapted from https://github.com/chenyx512/battlecode24/blob/main/src/bot1/PathFinder.java
package ref_best;

import battlecode.common.*;

import ref_best.fast.*;

public class Pathfinder extends RobotPlayer{

    static MapLocation target = null;
    static MapLocation stayawayFrom = null;
    static int stuckCnt;
    static boolean ignoreTowers = false;


    public static void tryMove(Direction dir) throws GameActionException {
        if (dir == Direction.CENTER)
            return;
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public static void move(MapLocation loc) throws GameActionException {
        move(loc, false);
        return;
    }

    public static void move(MapLocation loc, boolean _ignoreTowers) throws GameActionException {
        ignoreTowers = _ignoreTowers;
        if (!rc.isMovementReady() || loc == null)
            return;
        target = loc;
        stayawayFrom = null;
        Direction dir = BugNav.getMoveDir();
        if (dir == null)
            return;
        tryMove(dir);
    }

    public static Direction getMoveDir(MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady() || loc == null)
            return null;
        target = loc;
        stayawayFrom = null;
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
            //EXTRA
            if (rc.getRoundNum() == lastMoveRound) {
                System.out.println("Null return: Already moved this round");
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
                if (canMoveOrFill(dir)) {
                    return dir;
                }

                // If robot cannot move
                MapLocation loc = rc.getLocation().add(dir);
                // Try to sidestep enemy or empty paint
                if (rc.canSenseLocation(loc) && !rc.senseMapInfo(loc).getPaint().isAlly()) {
                    Direction dirL = dir.rotateLeft();
                    MapLocation locL = rc.getLocation().add(dirL);
                    Direction dirR = dir.rotateRight();
                    MapLocation locR = rc.getLocation().add(dirR);
                    if (target.distanceSquaredTo(locL) < target.distanceSquaredTo(locR)) {
                        if (canMoveOrFill(dirL)) {
                            return dirL;
                        }
                        if (canMoveOrFill(dirR)) {
                            return dirR;
                        }
                    } else {
                        if (canMoveOrFill(dirR)) {
                            return dirR;
                        }
                        if (canMoveOrFill(dirL)) {
                            return dirL;
                        }
                    }
                }

                // ADAPT bot 1 code that starts with: if there is water and sideway passage...
                currentTurnDir = getTurnDir(dir);
                // obstacle encountered, rotate and add new dirs to stack
                while (!canMoveOrFill(dir) && dirStack.size < 8) {
                    if (!rc.onTheMap(rc.getLocation().add(dir))) {
                        currentTurnDir ^= 1;
                        dirStack.clear();
                        System.out.println("Null return: Hit map boundary");
                        return null; // do not move
                    }
                    dirStack.push(dir);
                    dir = turn(dir);
                }
                if (dirStack.size != 8) {
                    return dir;
                }
            }
            else {
                // dxx
                // xo
                // x
                // suppose you are at o, x is wall, and d is another duck, you are pathing left and bugging up rn
                // and the duck moves away, you wanna take its spot
                if (dirStack.size > 1 && canMoveOrFill(dirStack.top(2))) {
                    dirStack.pop(2);
                } else if (dirStack.size == 1 && canMoveOrFill(turn(dirStack.top(), 1 - currentTurnDir))) {
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
                while (dirStack.size > 0 && canMoveOrFill(dirStack.top())) {
                    dirStack.pop();
                }
                if (dirStack.size == 0) {
                    Direction dir = rc.getLocation().directionTo(target);
                    if (canMoveOrFill(dir)) {
                        return dir;
                    }
                    MapLocation loc = rc.getLocation().add(dir);
                    // ADAPT bot 1 code that starts with:if (rc.canSenseLocation(loc) && rc.senseMapInfo(loc).isWater()...
                    dirStack.push(dir);
                }
                // keep rotating and adding things to the stack
                Direction curDir;
                int stackSizeLimit = Math.min(DirectionStack.STACK_SIZE, dirStack.size + 8);
                while (dirStack.size > 0 && !canMoveOrFill(curDir = turn(dirStack.top()))) {
                    if (!rc.onTheMap(rc.getLocation().add(curDir))) {
                        currentTurnDir ^= 1;
                        dirStack.clear();
                        System.out.println("Null return: Hit map boundary");
                        return null; // do not move
                    }
                    dirStack.push(curDir);
                    if (dirStack.size == stackSizeLimit) {
                        dirStack.clear();
                        System.out.println("Null return: Stack size limit reached");
                        return null;
                    }
                }
                if (dirStack.size >= stackDepthCutoff) {
                    int cutoff = stackDepthCutoff + 8;
                    dirStack.clear();
                    stackDepthCutoff = cutoff;
                    System.out.println("Null return: Stack depth cutoff reached, new cutoff: " + cutoff);
                }
                Direction moveDir = dirStack.size == 0 ? dirStack.dirs[0] : turn(dirStack.top());
                if (canMoveOrFill(moveDir)) {
                    return moveDir;
                }
            }
            System.out.println("Null return: Final moveDir cannot be moved to");
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

        static boolean canMoveOrFill(Direction dir) throws GameActionException {
            MapLocation loc = rc.adjacentLocation(dir);
            if (stayawayFrom != null && loc.isAdjacentTo(stayawayFrom))
                return false;

            if (!ignoreTowers) {
                if (nearestEnemyTower != null && loc.isWithinDistanceSquared(nearestEnemyTower,
                        nearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                    return false;
                }
                if (sndNearestEnemyTower != null && loc.isWithinDistanceSquared(sndNearestEnemyTower,
                        sndNearestEnemyTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER ? 16 : 9)) {
                    return false;
                }
            }

            if (rc.canMove(dir)) {
                return true;
            }
            // ADAPT bot 1 code that starts with: if (info.isWater()) ...
            // EXTRA
            if (!rc.canSenseLocation(loc))
                return false;

            RobotInfo robot = rc.senseRobotAtLocation(loc);
            if (robot != null && robot.getType().isRobotType()) {
                return FastMath.rand256() % 5 == 0; // small chance robot might be gone by the time duck reaches location
            }
            return false;
        }

        static boolean canPass(MapLocation loc, Direction targetDir) throws GameActionException {
            MapLocation newLoc = loc.add(targetDir);
            if (!rc.onTheMap(newLoc))
                return false;
            if (rc.canSenseLocation(newLoc)) {
                return rc.senseMapInfo(newLoc).isPassable();
            } else {
                return false;
                // return MapRecorder.getPassible(newLoc);
            }
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
