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

        for(MapInfo tile : rc.senseNearbyMapInfos(2)) {
            if(tile.getPaint().isEnemy()) {
                targetLoc = tile.getMapLocation();
                score = Constants.MopEnemyPaint;
                rc.setIndicatorString(tile.getMapLocation()+"");
                if(tile.getMapLocation().equals(_Info.robotLoc)) {
                    return;
                }
            }
        }
        MapInfo north = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x, _Info.robotLoc.y + 1));
        MapInfo northeast = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x + 1, _Info.robotLoc.y + 1));
        MapInfo east = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x + 1, _Info.robotLoc.y));
        MapInfo southeast = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x + 1, _Info.robotLoc.y - 1));
        MapInfo south = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x, _Info.robotLoc.y - 1));
        MapInfo southwest = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x - 1, _Info.robotLoc.y - 1));
        MapInfo west = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x - 1, _Info.robotLoc.y));
        MapInfo northwest = rc.senseMapInfo(new MapLocation(_Info.robotLoc.x - 1, _Info.robotLoc.y + 1));

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
