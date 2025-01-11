package e_action.actions.unit;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.Debug;
import e_action.utils.Pathfinder;

public class MopperAttack extends Action {
    public RobotController rc;

    public MopperAttack(){
        rc = Robot.rc;
        name = "MOPPER ATTACK";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }




    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugAction);
    }


    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        score = 4;
        cooldown_reqs = 1;
     }

    
    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
    }


}
