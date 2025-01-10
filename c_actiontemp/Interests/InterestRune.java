package c_actiontemp.Interests;

import battlecode.common.MapLocation;
import c_actiontemp.Robot;

public class InterestRune extends Interest {
    MapLocation locTarget;

    public InterestRune() {
        super();
        name = "Rune       ";
    }

    @Override
    public void initTurn(){
        locTarget = null;
        if(Robot.nearestEmptyRuins != null) {
            locTarget = rc.getLocation().add(rc.getLocation().directionTo(Robot.nearestEmptyRuins));
        }
    }

    @Override
    public int getScore(MapLocation loc) {
        if(loc.equals(locTarget)){
            return Robot.INTEREST_RUNE;
        }
        return 0;
    }
}
