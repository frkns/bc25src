package bastien.units;

import bastien.Actions.ActionExplore;
import bastien.Actions.ActionPaint;
import battlecode.common.RobotController;
import battlecode.common.UnitType;
import bastien.Robot;

public class Soldier extends Robot {
    public Soldier(RobotController controlller){
        super(controlller);
        attackCost = UnitType.SOLDIER.attackCost;


        actions.add(new ActionExplore());
        actions.add(new ActionPaint());
    }
}
