package c_actiontemp.Interests;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import c_actiontemp.Robot;

public class InterestMark extends Interest {
    MapLocation targetLoc;

    public InterestMark() {
        super();
        name = "Mark      ";
    }

    @Override
    public void initTurn(){
        targetLoc = null;
        if(Robot.nearestIncorrectMark != null) {
            targetLoc = rc.getLocation().add(rc.getLocation().directionTo(Robot.nearestIncorrectMark.getMapLocation()));

            // Debug
            try {
                rc.setIndicatorLine(rc.getLocation(), Robot.nearestIncorrectMark.getMapLocation(), 0, 255, 0);
            } catch (GameActionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getScore(MapLocation loc) {
        if(targetLoc != null && loc.isAdjacentTo(targetLoc)){ // Important
            return Robot.INTEREST_MARK;
        }
        return 0;
    }
}
