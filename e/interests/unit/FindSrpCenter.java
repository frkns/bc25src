package e.interests.unit;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import e.Robot;
import e.interests.Interest;
import e.knowledge._Info;
import e.knowledge.Constants;
import e.utils.Debug;

// If the robot does not have a target SRP center, find one and set that as the center of the next SRP to validate.
public class FindSrpCenter extends Interest {
    public RobotController rc;

    public FindSrpCenter(){
        rc = Robot.rc;
        name = "FindSrpCenter";
        debugInterest = true;
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);

        if (_Info.srpCenter == null){
            _Info.srpCenter = nearestUnvalidatedCenter();
        } else { // Give the action a chance to validate before moving there
            adjustDirectionScore(rc.getLocation().directionTo(_Info.srpCenter), Constants.FindSrpCenterScore);
            Debug.printDirectionScores(3, Interest.directionScores, debugInterest);
            Debug.print(3, "Target SRP center " + _Info.srpCenter.toString(), debugInterest);
        }
    }

    // Checks the 5x5 grid around the robot for the first unvalidated SRP center
    public MapLocation nearestUnvalidatedCenter() throws GameActionException{
        switch(_Info.nearbyTileLocsR4.length){
            case 13: // Maximum possible length
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[12])){
                    return _Info.nearbyTileLocsR4[12];
                }
            case 12:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[11])){
                    return _Info.nearbyTileLocsR4[11];
                }
            case 11:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[10])){
                    return _Info.nearbyTileLocsR4[10];
                }
            case 10:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[9])){
                    return _Info.nearbyTileLocsR4[9];
                }
            case 9:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[8])){
                    return _Info.nearbyTileLocsR4[8];
                }
            case 8:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[7])){
                    return _Info.nearbyTileLocsR4[7];
                }
            case 7:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[6])){
                    return _Info.nearbyTileLocsR4[6];
                }
            case 6:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[5])){
                    return _Info.nearbyTileLocsR4[5];
                }
            case 5:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[4])){
                    return _Info.nearbyTileLocsR4[4];
                }
            case 4:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[3])){
                    return _Info.nearbyTileLocsR4[3];
                }
            case 3:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[2])){
                    return _Info.nearbyTileLocsR4[2];
                }
            case 2:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[1])){
                    return _Info.nearbyTileLocsR4[1];
                }
            case 1:
                if(!_Info.invalidSrpCenters.contains(_Info.nearbyTileLocsR4[0])){
                    return _Info.nearbyTileLocsR4[0];
                }
        }
        return null;
    }
}
