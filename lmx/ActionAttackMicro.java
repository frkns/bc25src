package lmx;

import battlecode.common.GameActionException;

public class ActionAttackMicro extends RobotPlayer {
    static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_ATTACK:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//

        ImpureUtils.updateNearestEnemyTower();
        if (nearestEnemyTower == null && rc.getRoundNum() < siegePhase) {
            Debug.println("\tX - ACTION_ATTACK_MICRO  : No enemy tower or siege phase");
            action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Attack if in range
        if (rc.canAttack(nearestEnemyTower)) {
            Debug.println("\tX - ACTION_ATTACK_MICRO  : Can't attack");
            rc.attack(nearestEnemyTower);
        }

        // stop attacking if low health, 30 or less means we die to level 1 paint/money tower shot + AoE
        if (rc.getHealth() < 31) {
            Debug.println("\tX - ACTION_ATTACK_MICRO  : Low on health");
            RobotPlayer.action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\tO - ACTION_ATTACK_MICRO  : Playing!");
        RobotPlayer.action = Action.ACTION_ATTACK;


        HeuristicPath.move(nearestEnemyTower, Heuristic.HEURISTIC_TOWER_MICRO);
        if (rc.canAttack(nearestEnemyTower)) {
            rc.attack(nearestEnemyTower);
        }

    }
}
