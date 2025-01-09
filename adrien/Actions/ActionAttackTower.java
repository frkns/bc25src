package adrien.Actions;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import adrien.Robot;

public class ActionAttackTower extends Action {
    MapLocation target;
    public ActionAttackTower() {
        super();
        name = "AttackTower ";
    }

    @Override
    public int getScore() {
        target = null;
        for(RobotInfo enemie: Robot.enemies){
            if(enemie.getType().isTowerType()){
                if(rc.canAttack(enemie.location)){
                    target = enemie.location;
                    return Robot.ACTION_ATTACK_TOWER;
                }
            }
        }
        return 0;
    }

    @Override
    public void play() {
        if(rc.canAttack(target)){
            try {
                rc.attack(target);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }
}
