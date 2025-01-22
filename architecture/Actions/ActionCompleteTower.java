package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.*;
import architecture.fast.FastLocHashmap;
import battlecode.common.*;

public class ActionCompleteTower extends RobotPlayer {
    public static FastLocHashmap checked; // When impossible to perform action on target, mark target has checked for X rounds.

    public static void init(){
        checked = new FastLocHashmap();
    }

    public static void run() throws GameActionException {
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
        if (nearestEmptyRuin == null) {
            Debug.println("\tX - ACTION_COMPLETE_TOWER: No ruins");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        UnitType tower = Utils.getBuildType(nearestEmptyRuin);

        if (tower == null) {
            Debug.println("\tE - ACTION_COMPLETE_TOWER: Can't build null tower type");
            checked.set(nearestEmptyRuin, (char)rc.getRoundNum());

            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        PatternReport report = CheckPattern.analyseTowerPatern(nearestEmptyRuin, tower);

        if (report.numWrongTiles != 0) {
            Debug.println("\tX - ACTION_COMPLETE_TOWER: Pattern not complete");
            checked.set(nearestEmptyRuin, (char)rc.getRoundNum());
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // And no one is nearby
        for (RobotInfo ally : rc.senseNearbyRobots(nearestEmptyRuin, 2, rc.getTeam())) {
            if (ally.ID < rc.getID()) { // Avoid to be self detected
                Debug.println("\tX - ACTION_COMPLETE_TOWER: Someone is already here");
                checked.set(nearestEmptyRuin, (char)rc.getRoundNum());
                action = Action.ACTION_WAITING_FOR_ACTION;
                return;
            }
        }

        // If target not already checked, and we make this action by default.
        int turn = (int)checked.get(nearestEmptyRuin);
        if(action == Action.ACTION_WAITING_FOR_ACTION) {
            if (turn == 0 || rc.getRoundNum() - turn < 10) {
                Debug.println("\tX - ACTION_COMPLETE_TOWER: Target already checked at turn " + turn);
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

        if (rc.canCompleteTowerPattern(tower, nearestEmptyRuin)) {
            rc.completeTowerPattern(tower, nearestEmptyRuin);

            if (rc.getPaint() < 150) {
                action = Action.ACTION_GET_PAINT;
            }else{
                action = Action.ACTION_WAITING_FOR_ACTION;
            }
        } else {
            Debug.println("\t0 - ACTION_COMPLETE_TOWER: Can't complete now, I am waiting.");
        }
    }
}
