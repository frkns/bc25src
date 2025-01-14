package e_action.interests.unit;

import battlecode.common.GameActionException;
import battlecode.common.*;
import e_action.Robot;
import e_action.interests.Interest;
import e_action.knowledge._Info;
import e_action.utils.Constants;
import e_action.utils.Debug;
import e_action.utils.fast.FastLocSet;

public class Ruins extends Interest {
    public RobotController rc;

    public Ruins(){
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
        MapLocation[] nearbyRuins = _Info.nearbyRuins;

        if(_Info.completedPatterns.size() == 0) {
            int closest = Integer.MAX_VALUE;
            MapLocation ruinLoc = null;
            if (nearbyRuins != null) {
                for (MapLocation ruin : nearbyRuins) {
                    if (!rc.isLocationOccupied(ruin) && rc.getLocation().distanceSquaredTo(ruin) < closest) {
                        ruinLoc = ruin;
                        closest = rc.getLocation().distanceSquaredTo(ruin);
                    }
                }
            }

            if (ruinLoc != null) {
                if (!_Info.avoidRuins.contains(ruinLoc)) {
                    adjustDirectionScore(rc.getLocation().directionTo(ruinLoc), Constants.RuinScore);
                }
            }

            // Use the function below to assign a score to the variable
            // adjustDirectionScore(dir, Constants.ExploreScore);
        } else {
            if(rc.getChips() > 1000) {
                adjustDirectionScore(rc.getLocation().directionTo(_Info.completedPatterns.getKeys()[0]), Constants.RuinScore);
            }
        }
    }
}
