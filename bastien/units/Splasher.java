package bastien.units;

import bastien.Actions.ActionExplore;
import bastien.Actions.ActionPaint;
import battlecode.common.RobotController;
import battlecode.common.UnitType;
import bastien.Robot;

public class Splasher extends Robot {
    public Splasher(RobotController controlller){
        super(controlller);
        attackCost = UnitType.SPLASHER.attackCost;

        actions.add(new ActionExplore());
        actions.add(new ActionPaint());
    }
}
