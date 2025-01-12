package templateBot.units;

import battlecode.common.RobotController;
import templateBot.Actions.ActionAttackTower;
import templateBot.Actions.ActionPaint;
import templateBot.Interests.InterestExplore;
import templateBot.Robot;

public class Soldier extends Robot {
    public Soldier(RobotController controlller){
        super(controlller);

        interests.add(new InterestExplore(this));
        actions.add(new ActionAttackTower(this));
        actions.add(new ActionPaint(this));
    }
}
