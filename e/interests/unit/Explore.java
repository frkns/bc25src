package e.interests.unit;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import e.Robot;
import e.interests.Interest;
import e.knowledge._Info;
import e.knowledge.Constants;
import e.utils.Debug;
import e.utils.Pathfinder;

public class Explore extends Interest {
    public RobotController rc;

    public Explore(){
        rc = Robot.rc;
        name = "Explore";
    }


    public static MapLocation target = null;
    public static int explorationBoundary = 0;

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INIT + name, debugInterest);
    }

    //TODO Follow empty tiles without stepping on them
    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);

        if (target == null || rc.getLocation().distanceSquaredTo(target) < 3 || outOfExplorationBounds(rc.getLocation())) {
            target = getRandomInBoundLocation();
        }
        Direction dir = Pathfinder.getMoveDir(target);
        if (dir != null) {
            adjustDirectionScore(dir.rotateLeft(), Constants.ExploreScore - 2);
            adjustDirectionScore(dir, Constants.ExploreScore);
            adjustDirectionScore(dir.rotateRight(), Constants.ExploreScore - 2);
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
