package remake;

import battlecode.common.*;

public class RobotPlayer {
    static RobotController rc;
    static int PHASE = 1;

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        Utils.init(rc);
        UnitFuncs.init(rc);

        while (true) {
            if (rc.getRoundNum() < 200) {
                PHASE = 1;
            } else if (true) {
                PHASE = 2;
            }
            try {
                // Move switch statements into phases
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
