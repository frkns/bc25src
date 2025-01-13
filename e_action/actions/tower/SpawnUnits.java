package e_action.actions.tower;

import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;
import e_action.knowledge._Info;
import battlecode.common.*;

public class SpawnUnits extends Action {
    public RobotController rc;

    public SpawnUnits(){
        rc = Robot.rc;
        name = "SPAWN UNITS";
        Debug.print(3, Debug.INIT + name, debugAction);
    }

    static UnitType[] productionList = {UnitType.SOLDIER, UnitType.MOPPER, UnitType.SPLASHER};
    static int productionTypeCounter = 0;
    static MapLocation spawnLoc;
    static UnitType Unit;

    public int spawnedUnits = 0;

    public void initUnit(){
        Debug.print(1, Debug.INITUNIT + name, debugAction);

    }

    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);

        Direction dir = _Info.directions[_Info.rng.nextInt(_Info.directions.length)];
        if (dir.getDeltaX() != 0 && dir.getDeltaY() != 0) {
            spawnLoc = rc.getLocation().add(dir); // If diagonal we can only spawn 1 tile away
        } else {
            spawnLoc = rc.getLocation().add(dir).add(dir);
        }
        Unit = productionList[productionTypeCounter % productionList.length];

        if (rc.canBuildRobot(Unit, spawnLoc) && shouldSpawn()) {
            score = Constants.SpawnUnitsScore;
        } else {
            score = 0;
        }
    }

    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);

        rc.buildRobot(Unit, spawnLoc);

        // Exemple
        int id = rc.senseRobotAtLocation(spawnLoc).getID();
        Debug.print(0, "### Test ### Send message to " + id);
        /*
        Communication.sendClassMessage(id, 42);
        Communication.sendLocationMessage(id, 4, new MapLocation(0, 0)); // can send an int with the location
          */

        spawnedUnits++;
        productionTypeCounter++;
    }

    //helper function that determines when units should be spawned
    public boolean shouldSpawn() {
        int mapArea = _Info.MAP_AREA;
        int chips = _Info.chips;
        int chipRate = _Info.chipsRate;

        //spawns 3 soldiers at the start
        if(rc.getRoundNum() < 6) {
            if(rc.getType() == UnitType.LEVEL_ONE_MONEY_TOWER) {
                 if(spawnedUnits  < 1) {
                     return true;
                 }
            } else {
                if(spawnedUnits < 2) {
                    return true;
                }
            }
        }

        if(mapArea < 1000) {
            if(chips > 2250) {
                return true;
            } else if (chipRate >= 60){
                return true;
            } else {
                return false;
            }
        } else if (mapArea < 2000) {
            if(chips > 2250) {
                return true;
            } else if (chipRate >= 100){
                return true;
            } else if(rc.getRoundNum()> 300){
                return true;
            } else {
                return false;
            }
        } else if (mapArea < 3000) {
            if(chips > 2250) {
                return true;
            } else if (chipRate >= 100){
                return true;
            } else if(rc.getRoundNum()> 300){
                return true;
            } else {
                return false;
            }
        } else {
            if(chips > 2250) {
                return true;
            } else if (chipRate >= 120){
                return true;
            } else if(rc.getRoundNum()> 200){
                return true;
            } else {
                return false;
            }
        }
    }


}
