package templateBot.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import templateBot.Actions.ActionDefend;
import templateBot.Robot;

public class Tower extends Robot {
    public Tower(RobotController controlller){
        super(controlller);

        actions.add(new ActionDefend(this));
    }

    @Override
    public void initTurn() throws GameActionException {
        super.initTurn();
        rc.attack(null); // Radius attack
    }
}
