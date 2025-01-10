package e_action.actions.unit;

import battlecode.common.*;
import e_action.Robot;
import e_action.actions.Action;
import e_action.utils.*;

public class Explore extends Action {
    public RobotController rc;

    public Explore(){
        rc = Robot.rc;
        name = "EXPLORE";
        score = 0;
        debugAction = false;
        Debug.print(3, Debug.INIT + name, debugAction);
    }


    // Initialize variables specific to the function here
    public static MapLocation spawnTowerLocation;
    public static Direction spawnDirection;
    public static MapLocation target = null;

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugAction);

        for (RobotInfo robot : rc.senseNearbyRobots(2)) {
            if (robot.getType().isTowerType()) {
                spawnTowerLocation = robot.getLocation();
                spawnDirection = rc.getLocation().directionTo(spawnTowerLocation).opposite();
            }
        }
    }

    // Use Robot.variable_name to access the variables in Robot file
    // e.g. Robot.directions
    public void calcScore() throws GameActionException {
        Debug.print(3, Debug.CALCSCORE + name, debugAction);
        score = 10;  // Make based on rounds/painted tiles later, or even have tower send message to robot
     }

    public int getScore(){
        return score;
    }

    public void play() throws GameActionException {
        Debug.print(3, Debug.PLAY + name, debugAction);
        if (target == null || rc.getLocation().distanceSquaredTo(target) < 3 || Utils.outOfExplorationBounds(rc.getLocation())) {
            target = Utils.getRandomInBoundLocation();
        }
        Pathfinder.move(target);
    }
}
