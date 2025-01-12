package e_action.knowledge;

import battlecode.common.*;
import e_action.Robot;

public class ChipProductionRate {
    public static Integer prevChips = null;
    public static Integer prevRate = 0;

    public static int calculate () {
        RobotController rc = Robot.rc;

        if(prevChips == null || rc.getChips() == 0) {
            prevChips= rc.getChips();
            return -1;
        } else {
            if(rc.getChips() > prevChips && prevRate - (rc.getChips() - prevChips) <= 60) {
                prevRate = rc.getChips() - prevChips;
            }
        }
        prevChips = rc.getChips();
        return prevRate;
    }
}
