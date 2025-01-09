package bastien.units;

import bastien.Actions.ActionExplore;
import bastien.Actions.ActionPaint;
import battlecode.common.*;
import bastien.Robot;

public class Mopper extends Robot {
    public static MapLocation enemiePaint;
    public static MapLocation neighbourEnemiePaint;

    public Mopper(RobotController controlller){
        super(controlller);
        attackCost = UnitType.MOPPER.attackCost;

        actions.add(new ActionExplore());
        actions.add(new ActionPaint());
    }

    @Override
    public void initTurn() throws GameActionException {
        super.initTurn();
    }
}
