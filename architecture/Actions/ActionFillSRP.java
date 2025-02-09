package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.*;
import battlecode.common.*;

public class ActionFillSRP extends RobotPlayer {
    static PatternReport report;

    static boolean canHelp(PatternReport r) {
        return switch (rc.getType()) {
            case UnitType.SOLDIER -> r.nearestWrongPaint != null;
            case UnitType.MOPPER -> r.nearestWrongEnemies != null;
            default -> false;
        };
    }

    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_FILL_SRP:
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
        int minDistance = 3600;
        MapLocation bestSRP = null;
        PatternReport bestReport = null;

        // Check for mark primary
        for (MapInfo tile : nearbyTiles) {
            if (tile.getMark() == PaintType.ALLY_PRIMARY) {
                MapLocation tileLoc = tile.getMapLocation();

                // Check if not too far
                int distance = rc.getLocation().distanceSquaredTo(tileLoc);
                if (distance < minDistance) {

                    // Check if I can help
                    if (canHelp(CheckPattern.analyseSRP(tileLoc))) {
                        minDistance = distance;
                        bestSRP = tileLoc;
                    }
                    else{
                        System.out.println("\t\tSRP detected at " + tileLoc + " but can't help.");
                    }
                } else{
                    System.out.println("\t\tSRP detected at " + tileLoc + " but too far.");
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
        if (rc.getType() == UnitType.SOLDIER && rc.getPaint() < 5) {
            Debug.println("\tX - ACTION_FILL_SRP      : Not enough paint");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Check if can recover paint
        report = CheckPattern.analyseSRP(bestSRP);

        MapLocation target = switch (rc.getType()) {
            case UnitType.SOLDIER -> report.nearestWrongPaint;
            case UnitType.MOPPER -> report.nearestWrongEnemies;
            default -> null; // Never append, because canHelp.
        };

        if (target == null) {
            Debug.println("\tX - ACTION_FILL_SRP      : Can't help");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Check if not empty ruins nearby
        for(MapLocation ruin: rc.senseNearbyRuins(-1)) {
            if (Utils.chessDistance(target, ruin) <= 2) {
                Debug.println("\tX - ACTION_FILL_SRP      : Empty ruins nearby.");
                action = Action.ACTION_WAITING_FOR_ACTION;
                return;
            }
        }

        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\tX - ACTION_FILL_SRP      : Playing!");
        RobotPlayer.action = Action.ACTION_FILL_SRP;

        // Calculate paint type to use
        boolean[][] towerPattern = rc.getResourcePattern();
        int delta_x = curSRP.x - target.x;
        int delta_y = curSRP.y - target.y;
        int mask_x = 2 - delta_x;  // towerPatter[2][2] is the center
        int mask_y = 2 - delta_y;

        if (mask_x < 0 || mask_x > 4 || mask_y < 0 || mask_y > 4) {
            System.out.println("SRP deltas are off. curSRP: " + curSRP + ", getNearestWrongInSrp: " + target);
            return;
        }
        boolean useSecondary = towerPattern[mask_x][mask_y];

        // Attack incorrect tile
        Pathfinder.move(target);
        if (rc.canAttack(target)) {
            rc.attack(target, useSecondary);
            report.numWrongTiles--;

            if (report.numWrongTiles == 0 && report.numberUnknown == 0) {
                action = Action.ACTION_COMPLETE_SRP;
            } else {
                action = Action.ACTION_FILL_SRP;
            }
        }
    }
}
