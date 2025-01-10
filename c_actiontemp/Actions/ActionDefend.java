package c_actiontemp.Actions;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import c_actiontemp.Robot;
import c_actiontemp.utils.DebugUnit;

public class ActionDefend extends Action {
    public MapLocation target;

    public ActionDefend() {
        super();
        name = "Defend      ";
    }

    @Override
    public int getScore() {
        target = null;

        for(RobotInfo rInfo : Robot.enemies){
            if(rc.canAttack(rInfo.location)){
                target = rInfo.location;
                DebugUnit.print(3, "Found " + target);
                return Robot.ACTION_DEFEND;
            }
        }

        return 0;

    }

    @Override
    public void play() {
        if(rc.canAttack(target)){
            try {
                rc.attack(target);
                DebugUnit.print(3, "Attack " + target);
            } catch (GameActionException e) {
                DebugUnit.print(3, "Can't attack " + target);
            }
        }

    }
}
