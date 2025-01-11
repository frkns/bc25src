package e_action.interests.unit;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import e_action.Robot;
import e_action.interests.Interest;
import e_action.utils.*;

import java.util.Objects;

public class Explore extends Interest {
    public RobotController rc;

    public Explore(){
        rc = Robot.rc;
        name = "EXPLORE";
        debugInterest = false;
        Debug.print(3, Debug.INIT + name, debugInterest);
    }


    public static MapLocation target = null;
    public static int explorationBoundary = 5;

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugInterest);
    }

    //TODO Make based on rounds, painted tiles, or even have tower send message to robot
    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);
        if (target == null || rc.getLocation().distanceSquaredTo(target) < 3 || outOfExplorationBounds(rc.getLocation())) {
            target = getRandomInBoundLocation();
        }
        Direction dir = Pathfinder.getMoveDir(target);
        if (dir != null) {
            addDirectionScore(dir, Constants.ExploreScore);
        }
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
