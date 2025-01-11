package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

public class Explore extends Action {
    public RobotController rc;

    public Explore(){
        rc = Robot.rc;
        name = "EXPLORE";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }


    public static MapLocation target = null;
    public static int explorationBoundary = 5;

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugAction);
    }

    //TODO Make based on rounds, painted tiles, or even have tower send message to robot
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        score = 2;
        cooldown_reqs = 2;
     }

    
    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        if (target == null || rc.getLocation().distanceSquaredTo(target) < 3 || outOfExplorationBounds(rc.getLocation())) {
            target = getRandomInBoundLocation();
        }
        Pathfinder.move(target);
    }

    public static boolean outOfExplorationBounds(MapLocation loc) {
        return loc.x - explorationBoundary < 0 || loc.y - explorationBoundary < 0
            || loc.x + explorationBoundary >= Robot.MAP_WIDTH || loc.y + explorationBoundary >= Robot.MAP_HEIGHT;
    }

    public static MapLocation getRandomInBoundLocation() {
        // should be within [explorationBoundary+1, Robot.MAP_WIDTH-explorationBoundary-1]
        return new MapLocation(Robot.rng.nextInt(Robot.MAP_WIDTH - 2*explorationBoundary - 1) + explorationBoundary + 1,
                               Robot.rng.nextInt(Robot.MAP_HEIGHT - 2*explorationBoundary - 1) + explorationBoundary + 1);

    }
}
