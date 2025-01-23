package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.*;
import battlecode.common.*;

public class ActionFillRuin extends RobotPlayer {
    static PatternReport repport;

    static boolean canHelp() {
        return switch (rc.getType()) {
            case UnitType.SOLDIER -> repport.nearestWrongPaint != null && rc.getPaint() >= 5;
            case UnitType.MOPPER -> repport.nearestWrongEnemie != null;
            default -> false;
        };
    }

    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_FILL_RUINS:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Init and check if can play
        //------------------------------------------------------------------------------//
        // Todos : Check Mopper in range, Check Soldier in range (with enought paint)

        // Check if ruin
        if (nearestEmptyRuin == null) {
            Debug.println("\tX - ACTION_FILL_RUIN     : No empty ruin");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Get tower for this ruins
        UnitType towerType = Utils.getBuildType(nearestEmptyRuin);

        if(towerType == null){
            Debug.println("\tE - ACTION_FILL_RUIN     : Can't build null tower type");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        repport = CheckPattern.analyseTowerPatern(nearestEmptyRuin, towerType);

        // Check if I can help
        if(canHelp() == false){
            Debug.println("\tX - ACTION_FILL_RUIN     : Can't help");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }


        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\t0 - ACTION_FILL_RUIN     : Playing!");
        action = Action.ACTION_FILL_RUINS;

        Pathfinder.move(nearestEmptyRuin);
        MapLocation target = switch (rc.getType()){
            case UnitType.SOLDIER -> repport.nearestWrongPaint;
            case UnitType.MOPPER -> repport.nearestWrongEnemie;
            default -> null;
        };

        // Calculate paint type to use
        boolean[][] towerPattern = rc.getTowerPattern(towerType);
        int delta_x = nearestEmptyRuin.x - target.x;
        int delta_y = nearestEmptyRuin.y - target.y;
        int mask_x = 2 - delta_x;  // towerPatter[2][2] is the center
        int mask_y = 2 - delta_y;

        if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
            System.out.println("ruin deltas are off. curRuin: " + nearestEmptyRuin + ", nearestWrongInRuin: " + target);
            return;
        }
        boolean useSecondary = towerPattern[mask_x][mask_y];

        // Paint incorrect tile
        Pathfinder.move(target);
        if (rc.canAttack(target)) {
            rc.attack(target, useSecondary);
            repport.numWrongTiles--;

            if (repport.numWrongTiles == 0) {
                action = Action.ACTION_COMPLETE_TOWER;
            }else{
                action = Action.ACTION_FILL_RUINS;
            }
        }

        // Check if we can build tower just after
        if (rc.canCompleteTowerPattern(towerType, nearestEmptyRuin)) {
            rc.completeTowerPattern(towerType, nearestEmptyRuin);

            if (rc.getPaint() < 80) {
                action = Action.ACTION_GET_PAINT;
            }else{
                action = Action.ACTION_WAITING_FOR_ACTION;
            }
        }
    }
}
