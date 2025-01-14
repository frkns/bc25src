package e_action.interests.unit;

import e_action.Robot;
import e_action.interests.Interest;
import e_action.utils.*;
import e_action.knowledge._Info;

import battlecode.common.*;

public class Explore extends Interest {
    public RobotController rc;

    public Explore(){
        rc = Robot.rc;
        name = "EXPLORE";
        Debug.print(3, Debug.INIT + name, debugInterest);
    }


    public static MapLocation target = null;
    public static int explorationBoundary = 0;

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugInterest);
    }

    //TODO Follow empty tiles without stepping on them
    public void updateDirectionScores() throws GameActionException {
        Debug.print(2, Debug.UPDATE_DIR_SCORES + name, debugInterest);
        if (target == null || rc.getLocation().distanceSquaredTo(target) < 3 || outOfExplorationBounds(rc.getLocation())) {
            target = getRandomInBoundLocation();
        }
        Direction dir = Pathfinder.getMoveDir(target);
        if (dir != null) {
            adjustDirectionScore(dir.rotateLeft(), Constants.ExploreScore - 1); // Add some flexibility to pathfinding. Only works well on open maps.
            adjustDirectionScore(dir, Constants.ExploreScore);
            adjustDirectionScore(dir.rotateRight(), Constants.ExploreScore - 1);
        }
     }

    public static boolean outOfExplorationBounds(MapLocation loc) {
        return loc.x - explorationBoundary < 0 || loc.y - explorationBoundary < 0
            || loc.x + explorationBoundary >= _Info.MAP_WIDTH || loc.y + explorationBoundary >= _Info.MAP_HEIGHT;
    }

    public static MapLocation getRandomInBoundLocation() {
        // should be within [explorationBoundary+1, Info.MAP_WIDTH-explorationBoundary-1]
        return new MapLocation(_Info.rng.nextInt(_Info.MAP_WIDTH - 2*explorationBoundary - 1) + explorationBoundary + 1,
                               _Info.rng.nextInt(_Info.MAP_HEIGHT - 2*explorationBoundary - 1) + explorationBoundary + 1);

    }
}
