package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.ImpureUtils;
import architecture.Tools.Pathfinder;
import battlecode.common.GameActionException;
import battlecode.common.PaintType;
import battlecode.common.UnitType;

public class ActionPaintUnder extends RobotPlayer {
    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }
        if(rc.getType() != UnitType.SOLDIER){
            return;
        }

        //------------------------------------------------------------------------------//
        // Check
        //------------------------------------------------------------------------------//

        // Enough paint
        if (rc.getPaint() < 31 || rc.getRoundNum() < 10) {
            return;
        }

        //------------------------------------------------------------------------------//
        // Play
        //------------------------------------------------------------------------------//

        if(rc.senseMapInfo(rc.getLocation()).getPaint() == PaintType.EMPTY){
            rc.attack(rc.getLocation());
        }
    }
}
