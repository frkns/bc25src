package e_action.interests.unit;

import battlecode.common.*;
import e_action.Robot;
import e_action.interests.Interest;
import e_action.knowledge._Info;
import e_action.utils.Debug;
import e_action.utils.Constants;

// If the robot does not have a target SRP center, find one and set that as the center of the next SRP to validate.
public class FindSrpCenter extends Interest {
    public RobotController rc;

    public FindSrpCenter(){
        rc = Robot.rc;
        name = "FIND SRP CENTER";
        debugInterest = false;
        Debug.print(3, Debug.INIT + name, debugInterest);
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);
        if (_Info.srpCenter == null){
            _Info.srpCenter = nearestUnvalidatedCenter();
        }
        if (_Info.srpCenter != null){
            adjustDirectionScore(_Info.robotLoc.directionTo(_Info.srpCenter), Constants.CompleteSrpScore);
        }
    }

    // Returns the closest unvalidated srpCenter
    // TODO Add a score to all potential srpCenters. Only choose 1 center to prioritize once the center has been validated. Low priority.
    public MapLocation nearestUnvalidatedCenter() throws GameActionException{
        // Calculate the vertical offset needed to reach the next valid y-coordinate (y % 3 == 2)
        int dy = (2 - _Info.robotLoc.y % 3) % 3;
        // Calculate the shifted x-coordinate based on y position
        int x = _Info.robotLoc.x + ((_Info.robotLoc.y + dy) / 3);
        // Calculate the horizontal offset needed to reach the next valid x-coordinate ((x + y/3) % 4 == 2)
        int dx = (2 - x % 4) % 4;

        // Generate four potential srpCenter locations by applying offsets in all combinations
        MapLocation srpCenter1 = new MapLocation(_Info.robotLoc.x + dx, _Info.robotLoc.y + dy);
        MapLocation srpCenter2 = new MapLocation(_Info.robotLoc.x + dx, _Info.robotLoc.y - dy);
        MapLocation srpCenter3 = new MapLocation(_Info.robotLoc.x - dx, _Info.robotLoc.y + dy);
        MapLocation srpCenter4 = new MapLocation(_Info.robotLoc.x - dx, _Info.robotLoc.y - dy);

        // Calculate distances to each srpCenter, setting to MAX_VALUE if srpCenter is illegal or completed
        int d1 = _Info.invalidSrpCenters.contains(srpCenter1) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(srpCenter1);
        int d2 = _Info.invalidSrpCenters.contains(srpCenter2) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(srpCenter2);
        int d3 = _Info.invalidSrpCenters.contains(srpCenter3) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(srpCenter3);
        int d4 = _Info.invalidSrpCenters.contains(srpCenter4) ? Integer.MAX_VALUE : _Info.robotLoc.distanceSquaredTo(srpCenter4);

        // Return the closest valid srpCenter, or null if none are valid
        if (d1 <= d2 && d1 <= d3 && d1 <= d4 && d1 != Integer.MAX_VALUE) return srpCenter1;
        if (d2 <= d3 && d2 <= d4 && d2 != Integer.MAX_VALUE) return srpCenter2;
        if (d3 <= d4 && d3 != Integer.MAX_VALUE) return srpCenter3;
        if (d4 != Integer.MAX_VALUE) return srpCenter4;
        return null;
    }
}
