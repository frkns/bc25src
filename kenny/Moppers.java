package kenny;

import battlecode.common.*;

public class Moppers extends RobotPlayer{

    static MapLocation target;

    // how long of not being able to reach target till we change it?
    static int targetChangeWaitTime = mx;
    static int lastTargetChangeRound = 0;

    // should we stand in place this turn? resets to false at the end of every turn
    static boolean mopperStand = false;  // unused at the moment

    // experimental
    // static int stopQuadrantModifierPhase = mx * 2;  // pending deletion?
    /* */

    static MapLocation nearestEnemyPaintOnRuin;

    public static void run() throws GameActionException {

        // ImpureUtils.updateNearbyUnits(); // pending removal

        ImpureUtils.tryUpgradeNearbyTowers();

        ImpureUtils.updateNearbyMask(true);
        ImpureUtils.updateNearestEnemyTower();
        ImpureUtils.updateNearestEnemyPaint();
        ImpureUtils.updateNearestEnemyPaintOnRuin();

        if (Utils.selfDestructRequirementsMet()) {
            System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum() + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
            rc.disintegrate();
        }


        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            // selecting a random target location on the map has an inherent bias towards the center if e.g. we are in a corner
            // this is more of a problem on big maps
            // try to combat this but also instead sometimes selecting a location in our current quadrant
            /*if (rc.getRoundNum() % 2 == 0 && rc.getRoundNum() < stopQuadrantModifierPhase)
                target = Utils.randomLocationInQuadrant(Utils.currentQuadrant());
            else*/ {
                target = Utils.randomLocationInQuadrant(rng.nextInt(4));
            }
            lastTargetChangeRound = rc.getRoundNum();
        }

        // if (nearestEnemyPaint != null && rc.getLocation().distanceSquaredTo(nearestEnemyPaint) <= 2)
        //     mopperStand = true;

        if (!mopperStand)
            HeuristicPath.mopperMove(target);

        // mop vs mop swing relative scoring logic
        // let's suppose that removing enemy paint from a tile has a value of ~10 paint

        // mop (10 action cooldown):
        // * enemy robot mop = -10 enemy and +5 mopper, net = +15      = 15
        // * enemy robot on enemy paint                     = +15 + 10 = 25
        // * enemy paint                                    = 0   + 10 = 10
        // mop swing (20 action cooldown):
        // * can hit up to 6 and remove 5 from each for a max of +30, but has double action cooldown... is this even worth it?

        // also, we should take into account the removing of enemy paint from a ruin

        // "mop" tile action scoring, scores are multiplied by 10 for easy calcs
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
                        score = 150; // this tile just has an enemy robot there
                    } else {

                        score = 250; // this tile has enemy paint and an enemy robot there!
                    }
                }
            }
            else if (hasEnemyPaint) {
                score = 100;         // this tile just has enemy paint there
            }
            if (score > bestScore) {
                bestScore = score;
                bestAttackTarget = tileLoc;
            }
        }


        if (bestAttackTarget != null) {
            rc.attack(bestAttackTarget);
        }


        mopperStand = false;
    }
}
