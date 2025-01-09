package bastien.units;

import bastien.Actions.ActionExplore;
import bastien.Actions.ActionPaint;
import battlecode.common.*;
import bastien.Robot;

public class Mopper extends Robot {
    public static MapLocation enemyPaint;
    public static MapLocation neighbourEnemyPaint;

    public Mopper(RobotController controller){
        super(controller);
        attackCost = UnitType.MOPPER.attackCost;

        actions.add(new ActionExplore());
        actions.add(new ActionPaint());
    }

    @Override
    public void initTurn() throws GameActionException {
        super.initTurn();
    }
}
