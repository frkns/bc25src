package remake;

import battlecode.common.*;

public class RobotPlayer {
    static int PHASE = 1;
    static int WIDTH;
    static int HEIGHT;

    public static void run(RobotController rc) throws GameActionException {
        WIDTH = rc.getMapWidth();
        HEIGHT = rc.getMapHeight();

        Debug.init(rc);
        Pathfinder.init(rc);
        Utils.init(rc);
        UnitFuncs.init(rc);

        while (true) {
            if (rc.getRoundNum() < 1000) {
                PHASE = 1;
            } else if (rc.getRoundNum() >= 1000) {
                PHASE = 2;
            }
            try {
                switch (rc.getType()) {
                    case SOLDIER: UnitFuncs.runSoldier(); break;
                    case MOPPER: UnitFuncs.runMopper(); break;
                    case SPLASHER: UnitFuncs.runSplasher(); break;
                    default: TowerFuncs.runTower(rc); break;
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }
}
