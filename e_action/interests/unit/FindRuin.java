package e_action.interests.unit;

import battlecode.common.GameActionException;
import battlecode.common.*;
import e_action.Robot;
import e_action.interests.Interest;
import e_action.knowledge._Info;
import e_action.utils.Constants;
import e_action.utils.Debug;


public class FindRuin extends Interest {

    public RobotController rc;

    public FindRuin(){
        rc = Robot.rc;
        name = "FIND RUINS";
        Debug.print(3, Debug.INIT + name, debugInterest);
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);
        
        if (rc.getChips() > 1000) {
            if (_Info.towerCenter == null){
                _Info.towerCenter = nearestUnvalidatedRuin();
            }
            if (_Info.towerCenter != null){
                adjustDirectionScore(rc.getLocation().directionTo(_Info.towerCenter), Constants.CompleteTowerPatternScore);
            }
        }

        // if(_Info.completedPatterns.size() == 0) {
        //     int closest = Integer.MAX_VALUE;
        //     MapLocation towerCenter = null;
        //     if (nearbyRuins != null) {
        //         for (MapLocation ruin : nearbyRuins) {
        //             if (!rc.isLocationOccupied(ruin) && rc.getLocation().distanceSquaredTo(ruin) < closest) {
        //                 towerCenter = ruin;
        //                 closest = rc.getLocation().distanceSquaredTo(ruin);
        //             }
        //         }
        //     }
        // } else {
        //     if(rc.getChips() > 1000) {
        //         adjustDirectionScore(rc.getLocation().directionTo(_Info.completedPatterns.getKeys()[0]), Constants.FindRuinScore);
        //     }
        // }
    }
    
    public MapLocation nearestUnvalidatedRuin() throws GameActionException {
        int closest = Integer.MAX_VALUE;
        MapLocation closestRuin = null;
        
        if (_Info.nearbyRuins != null) {
            for (MapLocation towerCenter : _Info.nearbyRuins) {
                if (!rc.isLocationOccupied(towerCenter) && rc.getLocation().distanceSquaredTo(towerCenter) < closest) {
                    if (!_Info.invalidTowerCenters.contains(towerCenter)) {
                        closestRuin = towerCenter;
                        closest = rc.getLocation().distanceSquaredTo(towerCenter);
                    }
                }
            }
        }
        return closestRuin;
    }

}
