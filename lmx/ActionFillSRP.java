package lmx;

import battlecode.common.*;

public class ActionFillSRP extends RobotPlayer {
    static int numWrongTilesInSRP; // Number of missed paint
    static MapLocation nearestWrongInSRP = null;
    static MapLocation nearestWrongInSRPEnemie = null;

    static boolean canHelp() {
        return switch (rc.getType()) {
            case UnitType.SOLDIER -> nearestWrongInSRP != null;
            case UnitType.MOPPER -> nearestWrongInSRPEnemie != null;
            default -> false;
        };
    }

    static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_SRP:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Init and check if can play
        //------------------------------------------------------------------------------//


        // Update nearest wrong SRP
        int minDistance = (int) 2e9;
        MapLocation bestSRP = curSRP;

        // Check for mark primary
        for (MapInfo tile : nearbyTiles) {
            if (tile.getMark() == PaintType.ALLY_PRIMARY) {
                MapLocation tileLoc = tile.getMapLocation();

                // Check if not too far
                if (tileLoc.distanceSquaredTo(rc.getLocation()) < minDistance) {
                    updateNearestWrongInSRP(tileLoc);

                    // Check if one robot is already making it
                    if (canHelp()) {
                        int distance = tileLoc.distanceSquaredTo(rc.getLocation());
                        if (distance < minDistance) {
                            minDistance = distance;
                            bestSRP = tileLoc;
                        }
                    }
                }
            }
        }
        curSRP = bestSRP;


        // Check if SRP
        if (curSRP == null) {
            Debug.println("\tX - ACTION_FILL_SRP      : No SRP to fill");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Check if enough paint
        if (rc.getType() == UnitType.SOLDIER && rc.getPaint() < 5){
            Debug.println("\tX - ACTION_FILL_SRP      : Not enough paint");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Check if can recover paint
        if (canHelp()) {
            Debug.println("\tX - ACTION_FILL_SRP      : Can't help");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\tX - ACTION_FILL_SRP      : Playing!");
        RobotPlayer.action = Action.ACTION_SRP;

        MapLocation target = switch (rc.getType()){
            case UnitType.SOLDIER -> nearestWrongInSRP;
            case UnitType.MOPPER -> nearestWrongInSRPEnemie;
            default -> null;
        };
        Pathfinder.move(target);

        // Calculate paint type to use
        boolean[][] towerPattern = rc.getResourcePattern();
        int delta_x = curSRP.x - target.x;
        int delta_y = curSRP.y - target.y;
        int mask_x = 2 - delta_x;  // towerPatter[2][2] is the center
        int mask_y = 2 - delta_y;

        if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
            System.out.println("SRP deltas are off. curSRP: " + curSRP + ", nearestWrongInSRP: " + target);
            return;
        }
        boolean useSecondary = towerPattern[mask_x][mask_y];

        // Attack incorrect tile
        if (rc.canAttack(target)) {
            rc.attack(target, useSecondary);
            numWrongTilesInSRP--;

            if (numWrongTilesInSRP == 0) {
                action = Action.ACTION_COMPLETE_SRP;
            } else {
                action = Action.ACTION_SRP;
            }
        }
    }


    //------------------------------------------------------------------------------//
    // updateNearestWrongInSRP
    //------------------------------------------------------------------------------//

    // Update : numWrongTilesInSRP, nearestWrongInSRP, nearestWrongInSRPEnemie
    public static boolean updateNearestWrongInSRP(MapLocation srpCorner) throws GameActionException {
        numWrongTilesInSRP = 0;
        nearestWrongInSRP = null;
        nearestWrongInSRPEnemie = null;
        boolean[][] resourcePattern = rc.getResourcePattern();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                MapLocation loc = new MapLocation(srpCorner.x + i - 2, srpCorner.y + j - 2);
                if (!rc.canSenseLocation(loc))
                    continue;

                PaintType paint = rc.senseMapInfo(loc).getPaint();

                if (paint.isEnemy()) {

                    numWrongTilesInSRP++;
                    if (nearestWrongInSRPEnemie == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrongInSRPEnemie)) {
                        nearestWrongInSRPEnemie = loc;
                    }
                }

                if (paint == PaintType.EMPTY
                        || (paint == PaintType.ALLY_SECONDARY && !resourcePattern[i][j])
                        || (paint == PaintType.ALLY_PRIMARY && resourcePattern[i][j])) {

                    numWrongTilesInSRP++;
                    if (nearestWrongInSRP == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(nearestWrongInSRP)) {
                        nearestWrongInSRP = loc;
                    }
                }
            }
        }
        return true;
    }
}
