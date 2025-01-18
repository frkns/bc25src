package lmx;

import battlecode.common.*;

public class HeuristicPath extends RobotPlayer {
    // keep track of the last 8 positions and have a cost for all 8 directions,
    // increasing the cost a bit if it's one of the last 8 positions.
    // plus some other costs for preferring to stay on my own paint, avoiding enemy paint etc
    // (also try to move away from our spawn tower)
    //                                                      -- Super Cow Powers


    // these are only for soldier explore and refill?
    static int enemyTowerPenalty = 1_000_000;
    static int enemyPaintPenalty = 3000;
    static int neutralPaintPenalty = 3000;
    static boolean fullFill = false;  // do we want to prioritize starting to paint everything
    static int targetIncentive = 500;

    public static void moveDefault(MapLocation targetLoc) throws GameActionException {
        // Move with behavior set for this unit
        move(targetLoc, behavior);
    }

    public static void move(MapLocation targetLoc, Behavior moveBehavior) throws GameActionException {
        Debug.print(1, "Move to " + targetLoc + " with " + moveBehavior.name());
        rc.setIndicatorString("behaviorMove.move(" + targetLoc + ", " + moveBehavior.name() + ")");
        int[] directionCost = new int[8];
        int cost = 0;
        int score;
        int lastScore;

        for (int i = 0; i < 8; i++) {
            //------------------------------------------------------------------------------//
            // Init and get infos on direction
            //------------------------------------------------------------------------------//
            Direction dir = directions[i];

            if (!rc.canMove(dir)) {
                continue; // Gain is zero.
            }
            Debug.print(2, "Direction " + dir.name());


            cost = 0;
            lastScore = 0;
            MapLocation newLoc = rc.adjacentLocation(dir);
            MapInfo tileInfo = rc.senseMapInfo(newLoc);


            //------------------------------------------------------------------------------//
            // Score depending of history, spawn
            //------------------------------------------------------------------------------//

            // add a cost for moving in a direction that gets closer to the last 8 positions
            cost = switch (moveBehavior) {
                case Behavior.WRONG_SRP -> 0;
                case Behavior.TOWER_MICRO -> 0;
                default -> 1000;
            };
            if (cost != 0) {
                for (MapLocation prevLoc : locationHistory) {
                    if (prevLoc == null)
                        continue;
                    if (dir == rc.getLocation().directionTo(prevLoc)) {
                        directionCost[i] += cost;
                    }
                }
            }

            // add a cost if new location is the previous one
            cost = switch (moveBehavior) {
                case Behavior.TOWER_MICRO -> 100;
                case Behavior.WRONG_SRP -> 200;
                default -> 1000;
            };
            if (cost != 0) {
                MapLocation lastLoc = locationHistory[(rc.getRoundNum() - 1 + 8) % 8];
                if (newLoc.equals(lastLoc)) {
                    directionCost[i] += cost;
                }
            }

            // add a cost for moving in a direction that gets closer to the tower that spawned us
            cost = switch (moveBehavior) {
                case Behavior.TOWER_MICRO -> 0;
                case Behavior.WRONG_SRP -> 0;
                case Behavior.WRONG_RUINS -> 0;
                case Behavior.REFILL -> 0;
                default -> 200;
            };
            if (cost != 0) {
                if (dir == rc.getLocation().directionTo(spawnTowerLocation))
                    directionCost[i] += cost;
            }

            Debug.print(3, "By history / spawn    + " + (directionCost[i] - lastScore));
            lastScore = directionCost[i];
            //------------------------------------------------------------------------------//
            // Score depending paint, location in map
            //------------------------------------------------------------------------------//

            // add a cost if on enemie paint
            cost = switch (moveBehavior) {
                case Behavior.WRONG_RUINS -> 0;
                case Behavior.TOWER_MICRO -> 100;
                case Behavior.REFILL -> 1000;
                case Behavior.MOPPER -> (rc.getNumberTowers() >= startPaintingFloorTowerNum) ? 5000 : 1500;
                default -> enemyPaintPenalty;
            };
            if (cost != 0) {
                if (tileInfo.getPaint().isEnemy()) {
                    directionCost[i] += cost;
                }
            }

            // add a cost if on empty paint
            cost = switch (moveBehavior) {
                case Behavior.WRONG_RUINS -> 0;
                case Behavior.REFILL -> 1000;
                case Behavior.MOPPER -> (rc.getNumberTowers() >= startPaintingFloorTowerNum) ? 4000 : 1400;
                default -> neutralPaintPenalty;
            };
            if (cost != 0) {
                if (tileInfo.getPaint() == PaintType.EMPTY) {
                    if ((!rc.isActionReady()))
                        directionCost[i] += cost;
                    // assume we can paint under ourselves so no cost is added if action ready
                }
            }

            Debug.print(3, "By paint              + " + (directionCost[i] - lastScore));
            lastScore = directionCost[i];
            //------------------------------------------------------------------------------//
            // Score depending on target
            //------------------------------------------------------------------------------//
            if (targetLoc != null) {

                // add cost for moving in a direction that gets us further away from target
                cost = switch (moveBehavior) {
                    case Behavior.WRONG_SRP -> 500;
                    case Behavior.TOWER_MICRO -> 500;
                    case Behavior.REFILL -> 1000;
                    default -> targetIncentive;
                };
                if (cost != 0) {
                    directionCost[i] += Utils.manhattanDistance(newLoc, targetLoc) * cost;
                }
            }

            Debug.print(3, "By target             + " + (directionCost[i] - lastScore));
            lastScore = directionCost[i];
            //------------------------------------------------------------------------------//
            // Score depending on others units
            //------------------------------------------------------------------------------//
            // add (negative) cost for moving in a direction that gets us closer to a clump
            cost = switch (moveBehavior) {
                case REFILL -> 50;
                default -> 1000;
            };
            if (cost != 0) {
                int maskx = dir.dx + 2;
                int masky = dir.dy + 2;
                int allyRobotsInNewLoc = 0;
                for (int d = 8; d-- > 0; ) {
                    if (nearbyAlliesMask[maskx + dx8[d]][masky + dy8[d]]) {
                        allyRobotsInNewLoc++;
                    }
                }
                if (allyRobotsInNewLoc > 2) {
                    directionCost[i] += allyRobotsInNewLoc * cost;
                }
            }

            // add a cost for moving in range of an enemy tower
            cost = switch (moveBehavior) {
                case Behavior.WRONG_RUINS -> 10;
                case Behavior.TOWER_MICRO -> (inTowerRange) ? 9000 : -9000; // If in rang, move out, if too far, move in
                default -> enemyTowerPenalty;
            };
            if (cost != 0) {
                if (nearestEnemyTower != null && newLoc.isWithinDistanceSquared(nearestEnemyTower, 9)) {
                    directionCost[i] += cost;
                }
            }

            Debug.print(3, "By other units        + " + (directionCost[i] - lastScore));
            lastScore = directionCost[i];
            //------------------------------------------------------------------------------//
            // Behavior specific condition
            //------------------------------------------------------------------------------//

            switch (moveBehavior) {
                case Behavior.WRONG_SRP:
                    // add cost for moving in a direction that gets us further away from target
                    if (curSRP != null) {
                        directionCost[i] += Utils.manhattanDistance(newLoc, curSRP) * 500;
                    }
                    break;

                case Behavior.SOLDIER:
                    // Nearest empty tile
                    if (fullFill && nearestEmptyTile != null) {
                        int distance = Utils.manhattanDistance(newLoc, nearestEmptyTile);
                        if (distance > 5) {
                            directionCost[i] += distance * 300;
                        }
                    }

                case Behavior.MOPPER:
                    // add cost for moving in a direction that gets us further away from enemie paint
                    if (nearestEnemyPaint != null) {
                        int distance = Utils.manhattanDistance(newLoc, nearestEnemyPaint);
                        if (distance > 5) {
                            directionCost[i] += Math.max(2, distance / 3) * 1000;
                        }
                    }

            }

            Debug.print(3, "By other behaviors    + " + (directionCost[i] - lastScore));
            Debug.print(2, "Total                 = " + directionCost[i] );
            //------------------------------------------------------------------------------//
            // End for each direction
            //------------------------------------------------------------------------------//
        }

        // find the minimum cost Direction and move there
        int minCost = Integer.MAX_VALUE;
        Direction minDir = null;
        for (int i = 0; i < 8; i++) {
            if (rc.canMove(directions[i]) && directionCost[i] < minCost) {
                minCost = directionCost[i];
                minDir = directions[i];
            }
        }
        if (minDir != null) {
            Debug.print(1, "Moving to " + minDir + "(" + minCost + ")");
            rc.move(minDir);
        }
    }
}
