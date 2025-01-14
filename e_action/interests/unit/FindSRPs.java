package e_action.interests.unit;

import e_action.Robot;
import e_action.interests.Interest;
import e_action.knowledge._Info;
import e_action.utils.*;
import e_action.actions.unit.CompleteSrp;

import battlecode.common.*;

public class FindSRPs extends Interest {
    public RobotController rc;
    public MapLocation center = null;

    public FindSRPs(){
        rc = Robot.rc;
        name = "FUNC NAME HERE";
        debugInterest = false;
        Debug.print(3, Debug.INIT + name, debugInterest);
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);

        if(rc.getPaint() < 5) {
            return;
        }

        if(center != null && _Info.illegalOrCompletedCenters.contains(center)){
            center = null;
            rc.setTimelineMarker("e",255,0,0);
        }

        if (center == null){
            center = SRP.findUnvalidatedCenter();
        }
        if (center != null) {
            if (_Info.robotLoc.isWithinDistanceSquared(center, 4)) { // Robot can see all 4 corners of potential SRP
                if (SRP.centerIsValid(center)){
                    adjustDirectionScore(rc.getLocation().directionTo(center),Constants.CompleteSrpScore);
                } else {
                    _Info.illegalOrCompletedCenters.add(center);
                    center = null;
                }
            }
        }
    }
}
