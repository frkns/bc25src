package lmx;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

public class ActionCompleteTower extends RobotPlayer {

    static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_COMPLETE_TOWER:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//

        // Check for ruin
        if(nearestEmptyRuin == null){
            Debug.println("\tX - ACTION_COMPLETE_TOWER: No ruins");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        UnitType tower = Utils.getBuildType(nearestEmptyRuin);

        // And no one is nearby
        for(RobotInfo ally: rc.senseNearbyRobots(nearestEmptyRuin, 2, rc.getTeam())){
            Debug.println("\tX - ACTION_COMPLETE_TOWER: Someone is already here");
            if(ally.ID < rc.getID()){ // Avoid to be self detected
                action = Action.ACTION_WAITING_FOR_ACTION;
                return;
            }
        }

        Debug.println("\t0 - ACTION_COMPLETE_TOWER: Playing!");
        RobotPlayer.action = Action.ACTION_COMPLETE_TOWER;
        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\t0 - ACTION_COMPLETE_TOWER: Playing!");
        Pathfinder.move(nearestEmptyRuin);

        if(rc.canCompleteTowerPattern(tower, nearestEmptyRuin)){
            rc.completeTowerPattern(tower, nearestEmptyRuin);
            action = Action.ACTION_WAITING_FOR_ACTION;
        }else{
            Debug.println("\t0 - ACTION_COMPLETE_TOWER: Can't complete now, I am waiting.");
        }
    }
}
