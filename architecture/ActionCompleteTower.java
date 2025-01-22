package architecture;

import battlecode.common.*;

public class ActionCompleteTower extends RobotPlayer {
    static int numWrongTilesInRuin; // Number of missed paint
    static MapLocation nearestWrongInRuin;
    static MapLocation nearestWrongInRuinEnemie;

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
        if(tower == null){
            Debug.println("\tE - ACTION_COMPLETE_TOWER: Can't build null tower type");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        updateNearestWrongInRuin(tower);

        if(numWrongTilesInRuin != 0){
            Debug.println("\tX - ACTION_COMPLETE_TOWER: Pattern not complete");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // And no one is nearby
        for(RobotInfo ally: rc.senseNearbyRobots(nearestEmptyRuin, 2, rc.getTeam())){
            if(ally.ID < rc.getID()){ // Avoid to be self detected
                Debug.println("\tX - ACTION_COMPLETE_TOWER: Someone is already here");
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
    }}
