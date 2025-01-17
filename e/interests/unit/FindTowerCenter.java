package e.interests.unit;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import e.Robot;
import e.interests.Interest;
import e.knowledge._Info;
import e.knowledge.Constants;
import e.utils.Debug;


public class FindTowerCenter extends Interest {
    public RobotController rc;

    public FindTowerCenter(){
        rc = Robot.rc;
        name = "FindTowerCenter";
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);
        
        if (rc.getChips() > 1000) {
            if (_Info.towerCenter == null){
                _Info.towerCenter = nearestUnvalidatedRuin();
            }
            if (_Info.towerCenter != null){
                adjustDirectionScore(rc.getLocation().directionTo(_Info.towerCenter).rotateLeft(), Constants.FindTowerCenterScore - 2);
                adjustDirectionScore(rc.getLocation().directionTo(_Info.towerCenter), Constants.FindTowerCenterScore);
                adjustDirectionScore(rc.getLocation().directionTo(_Info.towerCenter).rotateRight(), Constants.FindTowerCenterScore - 2);
            }
        }
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
