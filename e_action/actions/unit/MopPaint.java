package e_action.actions.unit;

import e_action.Robot;
import e_action.actions.Action;
import e_action.knowledge._Info;
import e_action.utils.*;

import battlecode.common.*;

public class MopPaint extends Action {
    public RobotController rc;


    public MopPaint(){
        rc = Robot.rc;
        name = "Mop Paint";
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }

    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);
    }

    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

        MapInfo north = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x, _Info.robotLoc.y + 1));
        if (north.getPaint().isEnemy()) {
            targetLoc = north.getMapLocation();
            score = Constants.MopEnemyPaint;
            rc.setIndicatorString(north.getMapLocation()+"");
            return;
        }
        MapInfo northeast = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x + 1, _Info.robotLoc.y + 1));
        if (northeast.getPaint().isEnemy()) {
            targetLoc = northeast.getMapLocation();
            score = Constants.MopEnemyPaint;
            rc.setIndicatorString(northeast.getMapLocation()+"");
            return;
        }
        MapInfo east = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x + 1, _Info.robotLoc.y));
        if (east.getPaint().isEnemy()) {
            targetLoc = east.getMapLocation();
            score = Constants.MopEnemyPaint;
            rc.setIndicatorString(east.getMapLocation()+"");
            return;
        }
        MapInfo southeast = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x + 1, _Info.robotLoc.y - 1));
        if (southeast.getPaint().isEnemy()) {
            targetLoc = southeast.getMapLocation();
            score = Constants.MopEnemyPaint;
            rc.setIndicatorString(southeast.getMapLocation()+"");
            return;
        }
        MapInfo south = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x, _Info.robotLoc.y - 1));
        if (south.getPaint().isEnemy()) {
            targetLoc = south.getMapLocation();
            score = Constants.MopEnemyPaint;
            rc.setIndicatorString(south.getMapLocation()+"");
            return;
        }
        MapInfo southwest = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x - 1, _Info.robotLoc.y - 1));
        if (southwest.getPaint().isEnemy()) {
            targetLoc = southwest.getMapLocation();
            score = Constants.MopEnemyPaint;
            rc.setIndicatorString(southwest.getMapLocation()+"");
            return;
        }
        MapInfo west = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x - 1, _Info.robotLoc.y));
        if (west.getPaint().isEnemy()) {
            targetLoc = west.getMapLocation();
            score = Constants.MopEnemyPaint;
            rc.setIndicatorString(west.getMapLocation()+"");
            return;
        }
        MapInfo northwest = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x - 1, _Info.robotLoc.y + 1));
        if (northwest.getPaint().isEnemy()) {
            targetLoc = northwest.getMapLocation();
            score = Constants.MopEnemyPaint;
            rc.setIndicatorString(northwest.getMapLocation()+"");
            return;
        }

        if(targetLoc == null) {
            score = 0;
        }
    }


    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        if(targetLoc != null) {
            if(rc.canAttack(targetLoc)) {
                rc.attack(targetLoc);
                targetLoc = null;
            }
        }

    }

    // Add helper functions here

}
