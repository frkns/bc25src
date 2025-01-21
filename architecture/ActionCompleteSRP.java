package architecture;

import battlecode.common.*;

import static java.lang.Math.max;

public class ActionCompleteSRP extends RobotPlayer {

    static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_COMPLETE_SRP:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//

        // Check for mark primary
        int minDistance = 3600;
        MapLocation srp = null;

        for (MapInfo tile : nearbyTiles) {
            if (tile.getMark() == PaintType.ALLY_PRIMARY) {
                MapLocation tileLoc = tile.getMapLocation();

                // Check if better distance
                int distance = tileLoc.distanceSquaredTo(rc.getLocation());
                if (distance < minDistance) {

                    // Check if one robot is not already making it
                    if(rc.senseNearbyRobots(tileLoc, max(0, distance - 1), rc.getTeam()).length == 0) {
                        minDistance = distance;
                        srp = tileLoc;
                    }
                }
            }
        }

        // Todo: what is units is too far to complete patern ?
        if(!rc.canCompleteResourcePattern(srp)){
            Debug.println("\tX - ACTION_COMPLETE_SRP  : Can't complete");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }


        Debug.println("\t0 - ACTION_COMPLETE_SRP  : Playing!");
        rc.completeResourcePattern(srp);
        action = Action.ACTION_WAITING_FOR_ACTION;
    }
}
