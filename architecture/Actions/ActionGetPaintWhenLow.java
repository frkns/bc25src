package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.ImpureUtils;
import architecture.Tools.Pathfinder;
import battlecode.common.GameActionException;

public class ActionGetPaintWhenLow extends RobotPlayer {
    static int MIN_PAINT = 15;

    public static void run() throws GameActionException {
        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//

        if (rc.getPaint() > MIN_PAINT || nearestPaintTower == null) {
            Debug.println("\tX - ACTION_GET_PAINT_LOW : No paint tower or no need for paint");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }


        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\t0 - ACTION_GET_PAINT_LOW : Playing!");
        Debug.println("\t\tRefill from " + nearestPaintTower);

        Pathfinder.move(nearestPaintTower);
        ImpureUtils.withdrawPaintIfPossible(nearestPaintTower);
        rc.setIndicatorLine(nearestPaintTower, rc.getLocation(),0, 0, 255);

        if(rc.getPaint() > 10){
            action = Action.ACTION_WAITING_FOR_ACTION;
        }
    }
}
