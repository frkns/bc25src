package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.Pathfinder;
import battlecode.common.*;
import gavin.fast.FastMath;

public class ActionExplore extends RobotPlayer {
    static MapLocation target;
    static int lastTargetChangeRound;
    static int targetChangeWaitTime = 20;
    static int width;
    static int height;

    public static MapLocation getBorderLocation() {
        switch (FastMath.rand256() % 4) {
            case 0: // Top
                return new MapLocation(FastMath.rand256() % width, height - 1);
            case 1: // Bottom
                return new MapLocation(FastMath.rand256() % width, 0);
            case 2: // Left
                return new MapLocation(0, FastMath.rand256() % height);
            case 3: // Top
                return new MapLocation(width - 1, FastMath.rand256() % height);
            default:
                Debug.println("Init explore : Should not be possible.");
                return null;
        }
    }

    public static void init() {
        // Explore in the direction given by the direction from tower to unit.
        width = rc.getMapWidth();
        height = rc.getMapHeight();

        Direction dir = spawnTowerLocation.directionTo(rc.getLocation());

        for(int i=0; i < 10; i++) {
            target = getBorderLocation();
            if(rc.getLocation().directionTo(target) == dir){
                return;
            }
        }
    }

    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_EXPLORE:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        if (!rc.isMovementReady()) {
            Debug.println("\tX - ACTION_EXPLORE       : Not movement ready");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        Debug.println("\t0 - ACTION_EXPLORE       : Playing!");

        if (target == null
                || rc.getLocation().isWithinDistanceSquared(target, 9)
                || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {
            target = new MapLocation(rng.nextInt(rc.getMapWidth()), rng.nextInt(rc.getMapHeight()));
            lastTargetChangeRound = rc.getRoundNum();
        }

        Pathfinder.move(target);
        rc.setIndicatorLine(rc.getLocation(), target, 196, 20, 236); // Pink
    }
}
