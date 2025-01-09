package adrien.units;

import battlecode.common.RobotController;
import battlecode.common.UnitType;
import adrien.Actions.ActionAttackTower;
import adrien.Actions.ActionBuildTower;
import adrien.Actions.ActionSetTowerPattern;
import adrien.Actions.ActionPaint;
import adrien.Interests.InterestConsistancy;
import adrien.Interests.InterestExplore;
import adrien.Interests.InterestMark;
import adrien.Interests.InterestRune;
import adrien.Robot;

public class Splasher extends Robot {
    public Splasher(RobotController controlller){
        super(controlller);

        interests.add(new InterestConsistancy());
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
