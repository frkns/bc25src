package gavin;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class Moppers extends RobotPlayer {

    static MapLocation target;

    // how long of not being able to reach target till we change it?
    static int targetChangeWaitTime = Math.max(mapWidth, mapHeight);
    static int lastTargetChangeRound = 0;


    public static void run() throws GameActionException {

        ImpureUtils.updateNearbyUnits(); // pending removal

        ImpureUtils.updateNearbyMask();

        ImpureUtils.updateNearestEnemyPaint();
        ImpureUtils.updateNearestEnemyTower();

        if (Utils.selfDestructRequirementsMet()) {
            System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum() + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
            rc.disintegrate();
        }

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            // if (target != null) rc.setIndicatorDot(target, 0, 0, 0);
            target = new MapLocation(rng.nextInt(mapWidth), rng.nextInt(mapHeight));
            lastTargetChangeRound = rc.getRoundNum();
        }
        // rc.setIndicatorDot(target, 200, 200, 200);

        // target = Utils.mirror(spawnTowerLocation);
        HeuristicPath.mopperMove(target);

        MapLocation bestAttackTarget = null;
        int bestScore = 0;

        MapInfo[] veryNearbyTiles = rc.senseNearbyMapInfos(2);
        for (MapInfo tile : veryNearbyTiles) {
            MapLocation tileLoc = tile.getMapLocation();
            if (!rc.canAttack(tileLoc))
                continue;

            int score = 0;
            boolean hasEnemyPaint = tile.getPaint().isEnemy();

            if (rc.canSenseRobotAtLocation(tileLoc))  {
                RobotInfo robot = rc.senseRobotAtLocation(tileLoc);
                if (robot.getTeam() != rc.getTeam()) {
                    if (!hasEnemyPaint) {
                        // this tile just has an enemy robot there
                        score = 30;
                        // System.out.println("Mopper: I can attack an enemy that is *not* on enemy paint");
                    } else {
                        // this tile has enemy paint and an enemy robot there!
                        score = 60;
                    }
                }
            }
            else if (hasEnemyPaint) {
                // this tile just has enemy paint there
                score = 20;
            }

            if (score > bestScore) {
                bestScore = score;
                bestAttackTarget = tileLoc;
            }
        }

        if (bestAttackTarget != null) {
            // if (bestScore == 30) {
            //     rc.setIndicatorLine(new MapLocation(0, 0), bestAttackTarget, 255, 0, 0);
            // }
            rc.attack(bestAttackTarget);
        }




    }
}
