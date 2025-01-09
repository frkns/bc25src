package adrien.Actions;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import adrien.Robot;
import adrien.units.Mopper;
import adrien.utils.DebugUnit;

public class ActionRemovePaint extends Action {
    MapLocation target;

    public ActionRemovePaint() {
        super();
        name = "Remove paint";
    }

    @Override
    public int getScore() {
        if(Mopper.neighbourEnemiePaint == null){
            return 0;
        }

        if(rc.canAttack(Mopper.neighbourEnemiePaint)){
            return Robot.ACTION_MOPSWING;
        }

        return 0;
    }

    @Override
    public void play() {
        if(Mopper.neighbourEnemiePaint != null
                && rc.canAttack(Mopper.neighbourEnemiePaint)){
            try {
                rc.attack(Mopper.neighbourEnemiePaint);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }
}
