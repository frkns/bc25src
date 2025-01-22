package kennyr;

import battlecode.common.*;

public class Soldiersc extends RobotPlayer {
    // Priority order of states:
    // 0 = refill paint
    // 1 = building tower
    // 2 = building SRP
    // 3 = find and attack enemy tower
    // 4 = going back to tower for instructions
    public static int state = 4;


    public static void run() throws GameActionException {
        // readMessages();
        handleRefill();
        handleTowerBuilding();
        handleSRPBuilding();
        handleAttack();
        handleExplore();
        handleReturnToTower();
        // sendMessages();
    }
    // private static void readMessages() throws GameActionException {
    // }

    private static void handleRefill() throws GameActionException {
        if (rc.getPaint() < 100) {
            state = 0;
        }
        
    }

    private static void handleTowerBuilding() throws GameActionException {
        if (state > 1 && rc.getChips() > 1000){
            state = 1;
        }
    }

    private static void handleSRPBuilding() throws GameActionException {
        if (state > 2){
            state = 2;
        }
    }

    private static void handleAttack() throws GameActionException {
    }

    private static void handleExplore() throws GameActionException {
    }

    private static void handleReturnToTower() throws GameActionException {
    }

    // private static void sendMessages() throws GameActionException {
    // }

}
