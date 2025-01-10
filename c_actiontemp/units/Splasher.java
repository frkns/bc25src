package c_actiontemp.units;

import battlecode.common.RobotController;
import battlecode.common.UnitType;
import c_actiontemp.Actions.ActionAttackTower;
import c_actiontemp.Actions.ActionBuildTower;
import c_actiontemp.Actions.ActionSetTowerPattern;
import c_actiontemp.Actions.ActionPaint;
import c_actiontemp.Interests.InterestExplore;
import c_actiontemp.Interests.InterestMark;
import c_actiontemp.Interests.InterestRune;
import c_actiontemp.Robot;

public class Splasher extends Robot {
    public Splasher(RobotController controlller){
        super(controlller);

        // interests.add(new InterestConsistancy());
        interests.add(new InterestMark());
        interests.add(new InterestRune());
        interests.add(new InterestExplore());

        actions.add(new ActionSetTowerPattern());
        actions.add(new ActionAttackTower());
        actions.add(new ActionPaint());
        actions.add(new ActionBuildTower());

        attackCost = UnitType.SPLASHER.attackCost;
    }
}
