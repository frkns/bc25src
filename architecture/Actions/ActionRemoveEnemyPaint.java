package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.ImpureUtils;
import architecture.Tools.Pathfinder;
import battlecode.common.*;

public class ActionRemoveEnemyPaint extends RobotPlayer {
    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_REMOVE_ENEMY_PAINT:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Check
        //------------------------------------------------------------------------------//

        ImpureUtils.updateNearestEnemyPaint();

        // Target not null
        if (nearestEnemyPaint == null) {
            return;
        }

        //------------------------------------------------------------------------------//
        // Play
        //------------------------------------------------------------------------------//

        if(rc.canAttack(nearestEnemyPaint)){
            rc.attack(nearestEnemyPaint);
            return;
        }

        Pathfinder.move(nearestEnemyPaint);

        if(rc.canAttack(nearestEnemyPaint)){
            rc.attack(nearestEnemyPaint);
        }
    }
}
