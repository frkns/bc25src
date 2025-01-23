package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.HeuristicPath;
import architecture.Tools.ImpureUtils;
import battlecode.common.GameActionException;

public class ActionGetPaintWhenLow extends RobotPlayer {
    static int MIN_PAINT = 20;

    public static void run() throws GameActionException {
        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//

        if (action == Action.ACTION_WAIT_COMPLETE_TOWER){
            return;
        }

        if (rc.getPaint() > MIN_PAINT || nearestPaintTower == null) {
            Debug.println("\tX - ACTION_GET_PAINT_LOW : No paint tower or no need for paint");

            if(action == Action.ACTION_GET_PAINT){
                // Check to avoid override another action.
                // Specific behavior because ACTION_GET_PAINT is prioritary.
                action = Action.ACTION_WAITING_FOR_ACTION;
            }
            return;
        }


        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\t0 - ACTION_GET_PAINT_LOW : Playing!");
        Debug.println("\t\tRefill from " + nearestPaintTower);

        HeuristicPath.move(nearestPaintTower, Heuristic.HEURISTIC_REFILL);
        ImpureUtils.withdrawPaintIfPossible(nearestPaintTower);
        rc.setIndicatorLine(nearestPaintTower, rc.getLocation(),0, 0, 255);

        if(rc.getPaint() > MIN_PAINT){
            action = Action.ACTION_WAITING_FOR_ACTION;
        }
    }
}
