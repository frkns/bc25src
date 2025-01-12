package e_action.actions.unit;

import c_actiontemp.Robot;
import e_action.knowledge._Info;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

public class RefillPaint extends Action {
    public RobotController rc;

    // Initialize any variables needed for the action here
    public Boolean take = true;

    public RefillPaint(){
        rc = Robot.rc;
        name = "REPLACE WITH FUNC NAME";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }

    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);
        // Initialize any variable needed when a unit first spawns in
    }

    // Use GatherInfo.varname to access the variables
    // Included are: GatherInfo.nearbyAllies, GatherInfo.nearbyRuins...
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        for(RobotInfo robot : _Info.nearbyAllies) {
            if(robot.getType().isTowerType()) {
                if(rc.getPaint() < 100) {
                    if(rc.canTransferPaint(robot.getLocation(),-1)) {
                        score = Constants.RefillPaintScore;
                        take = true;
                        return;
                    }
                }
            } else {
                if(rc.getPaint() > 60) {
                    if(rc.canTransferPaint(robot.getLocation(),1) ) {
                        score = Constants.RefillPaintScore;
                        take = false;
                        return;
                    }
                }
            }
        }
    }

    public int getScore(){
        return score;
    }

    // TODO <-- Use TODO to make notes on potential future improvements
    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

        if(take){
            RobotInfo r = null;
            int max = Integer.MIN_VALUE;
            for(RobotInfo robot : _Info.nearbyAllies) {
                if(robot.getType().isTowerType() && rc.canTransferPaint(rc.getLocation(),-1)) {
                    if(robot.getPaintAmount() > max) {
                        r = robot;
                        max = robot.getPaintAmount();
                    }
                }
            }
            if(r != null) {
                if(rc.getType() == UnitType.MOPPER) {
                    rc.transferPaint(r.getLocation(),Math.max(-40,r.getPaintAmount()));
                } else {
                    rc.transferPaint(r.getLocation(),Math.max(-140,r.getPaintAmount()));
                }
            }

        } else {
            MapLocation loc = null;
            int min = Integer.MAX_VALUE;
            for(RobotInfo robot : _Info.nearbyAllies) {
                if(robot.getType().isRobotType() && rc.canTransferPaint(rc.getLocation(),1)) {
                    if(robot.getPaintAmount() < min) {
                        min = robot.getPaintAmount();
                        loc = robot.getLocation();
                    }
                }
            }
            if(loc != null) {
                rc.transferPaint(loc,Math.max(rc.getPaint()-60,0));
            }
        }
    }

    // Add helper functions here

}
