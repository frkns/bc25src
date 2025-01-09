package adrien.units;

import battlecode.common.*;
import adrien.Actions.ActionBuildTower;
import adrien.Actions.ActionSetTowerPattern;
import adrien.Actions.ActionRemovePaint;
import adrien.Interests.InterestConsistancy;
import adrien.Interests.InterestExplore;
import adrien.Interests.InterestRune;
import adrien.Robot;

public class Mopper extends Robot {
    public static MapLocation enemiePaint;
    public static MapLocation neighbourEnemiePaint;

    public Mopper(RobotController controlller){
        super(controlller);

        // interests.add(new InterestConsistancy());
        interests.add(new InterestRune());
        interests.add(new InterestExplore());

        actions.add(new ActionSetTowerPattern());
        actions.add(new ActionBuildTower());
        actions.add(new ActionRemovePaint());

        attackCost = UnitType.MOPPER.attackCost;
    }

    @Override
    public void initTurn() throws GameActionException {
        super.initTurn();

        enemiePaint = null;
        neighbourEnemiePaint = null;

        for(MapInfo info: rc.senseNearbyMapInfos(2)){
            if(info.getPaint().isEnemy()){
                enemiePaint = info.getMapLocation();
                neighbourEnemiePaint = info.getMapLocation();
                break;
            }
        }

        if(enemiePaint == null) {
            for (MapInfo info : rc.senseNearbyMapInfos(-1)) {
                if (info.getPaint().isEnemy()) {
                    enemiePaint = info.getMapLocation();
                }
            }
        }
    }
}
