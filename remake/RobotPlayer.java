package remake;

import battlecode.common.*;

public class RobotPlayer {
    static int PHASE = 1;
    static int WIDTH;
    static int HEIGHT;

    public static void run(RobotController rc) throws GameActionException {
        // just init here
        WIDTH = rc.getMapWidth();
        HEIGHT = rc.getMapHeight();

        Debug.init(rc);
        PathFinder.init(rc);
        Utils.init(rc);
        UnitFuncs.init(rc);

        while (true) {
            if (rc.getRoundNum() < 200) {
                PHASE = 1;
            } else if (true) {
                PHASE = 2;
            }
            try {
                // TODO Move switch statements into phases
                switch (rc.getType()) {
                    case SOLDIER:
                        UnitFuncs.runSoldier();
                        break;
                    case MOPPER:
                        UnitFuncs.runMopper();
                        break;
                    case SPLASHER:
                        UnitFuncs.runSplasher();
                        break;
                    default:
                        TowerFuncs.runTower(rc);
                        break;
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
