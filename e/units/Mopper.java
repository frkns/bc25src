package e.units;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e.Robot;
import e.actions.unit.MopPaint;
import e.interests.unit.StayOnAllyPaint;


public class Mopper extends Robot {
    public Mopper(RobotController rc) throws GameActionException {
        super(rc);

        // interests.add(new Explore());
        // interests.add(new FindEnemyPaint());
        actions.add(new MopPaint());
        interests.add(new StayOnAllyPaint());

        initUnit();
    }
}
