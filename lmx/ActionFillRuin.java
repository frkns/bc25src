package lmx;

import battlecode.common.*;
import scala.Unit;

public class ActionFillRuin extends RobotPlayer {
    static int numWrongTilesInRuin; // Number of missed paint
    static MapLocation nearestWrongInRuin;
    static MapLocation nearestWrongInRuinEnemie;

    static boolean canHelp() {
        return switch (rc.getType()) {
            case UnitType.SOLDIER -> nearestWrongInRuin != null && rc.getPaint() >= 5;
            case UnitType.MOPPER -> nearestWrongInRuinEnemie != null;
            default -> false;
        };
    }

    static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_RUINS:
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
        updateNearestWrongInRuin(towerType);

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
        RobotPlayer.action = Action.ACTION_RUINS;

        Pathfinder.move(nearestEmptyRuin);
        MapLocation target = switch (rc.getType()){
            case UnitType.SOLDIER -> nearestWrongInRuin;
            case UnitType.MOPPER -> nearestWrongInRuinEnemie;
            default -> null;
        };

        // Calculate paint type to use
        boolean[][] towerPattern = rc.getTowerPattern(towerType);
        int delta_x = nearestEmptyRuin.x - target.x;
        int delta_y = nearestEmptyRuin.y - target.y;
        int mask_x = 2 - delta_x;  // towerPatter[2][2] is the center
        int mask_y = 2 - delta_y;

        if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
            System.out.println("ruin deltas are off. curRuin: " + nearestEmptyRuin + ", nearestWrongInRuin: " + nearestWrongInRuin);
            return;
        }
        boolean useSecondary = towerPattern[mask_x][mask_y];

        // Paint incorrect tile
        if (rc.canAttack(target)) {
            rc.attack(target, useSecondary);
            numWrongTilesInRuin--;

            if (numWrongTilesInRuin == 0) {
                action = Action.ACTION_WAITING_FOR_ACTION;
            }else{
                action = Action.ACTION_RUINS;
            }
        }

        // Check if we can build tower just after
        if (rc.canCompleteTowerPattern(towerType, nearestEmptyRuin)) {
            rc.completeTowerPattern(towerType, nearestEmptyRuin);

            if (rc.getPaint() < 150) {
                action = Action.ACTION_GET_PAINT;
            }else{
                action = Action.ACTION_WAITING_FOR_ACTION;
            }
        }
    }


    //------------------------------------------------------------------------------//
    // updateNearestWrongInRuin
    //------------------------------------------------------------------------------//

    public static void updateNearestWrongInRuin(UnitType towerType) throws GameActionException {
        numWrongTilesInRuin = 0;
        nearestWrongInRuin = null;
        nearestWrongInRuinEnemie = null;

        boolean[][] towerPattern = rc.getTowerPattern(towerType);

        // Check each cells of the pattern
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 2 && j == 2)
                    continue;

                MapLocation loc = new MapLocation(nearestEmptyRuin.x + i - 2, nearestEmptyRuin.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;

                PaintType paint = rc.senseMapInfo(loc).getPaint();
                if (paint.isEnemy()) {

                    numWrongTilesInRuin++;
                    if(nearestWrongInRuinEnemie == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrongInRuinEnemie)){
                        nearestWrongInRuinEnemie = loc;
                    }
                }

                if (paint == PaintType.EMPTY
                        || (paint == PaintType.ALLY_SECONDARY && !towerPattern[i][j])
                        || (paint == PaintType.ALLY_PRIMARY && towerPattern[i][j])) {

                    numWrongTilesInRuin++;
                    if (nearestWrongInRuin == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrongInRuin)) {
                        nearestWrongInRuin = loc;
                    }
                }
            }
        }
    }
}
