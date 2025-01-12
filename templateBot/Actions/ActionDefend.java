package templateBot.Actions;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import templateBot.Robot;
import templateBot.utils.DebugUnit;

public class ActionDefend extends Action {
    public RobotInfo target;
    public int range;

    public ActionDefend(Robot r) {
        super(r);

        switch (rc.getType()){
            case UnitType.LEVEL_ONE_DEFENSE_TOWER:
            case UnitType.LEVEL_TWO_DEFENSE_TOWER:
            case UnitType.LEVEL_THREE_DEFENSE_TOWER:
                range = 4;
                break;
            default:
                range = 3;
        }
    }

    @Override
    public int getScore() {
        target = null;

        for(RobotInfo rInfo : Robot.enemies){
            if(rc.getLocation().distanceSquaredTo(rInfo.location) < range){
                target = rInfo;
                DebugUnit.print(3, "Found " + target.location);
                return 10;
            }
        }

        return 0;

    }

    @Override
    public void play() {
        if(target != null){
            try {
                rc.attack(target.location);
                DebugUnit.print(3, "Attack " + target.location);
            } catch (GameActionException e) {
                DebugUnit.print(3, "Can't attack " + target.location);
            }
        }

    }
}
