package adrien.Interests;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import adrien.Robot;

import java.util.Random;

public class InterestExplore extends Interest {
    Direction[] directions = Robot.directions;
    Random rng = Robot.rng;
    Direction dir;
    MapLocation next;
    MapLocation target;
    RobotController rc;

    int width;
    int height;

    public InterestExplore() {
        super();
        name = "Explore    ";

        rc = Robot.rc;
        width = rc.getMapWidth();
        height = rc.getMapHeight();
        dir = directions[rng.nextInt(directions.length)];

    }

    void reloadTarget(){
            switch (rng.nextInt(4)){
                case 0:
                    target = new MapLocation(rng.nextInt(width),0);
                    break;
                case 1:
                    target = new MapLocation(rng.nextInt(width), height - 1);
                    break;
                case 2:
                    target = new MapLocation(0,rng.nextInt(height));
                    break;
                case 3:
                    target = new MapLocation(width - 1,rng.nextInt(height));
                    break;
            }
    }

    @Override
    public void initTurn() {
        super.initTurn();

        target = null;
        while(target == null || rc.getLocation().equals(target)) {
            reloadTarget();
        }

        Direction dir = rc.getLocation().directionTo(target);
        next = rc.getLocation().add(dir);


        for(int i=0; i<5; i++){
            try {
                if(rc.canSenseLocation(next) && rc.senseMapInfo(next).isPassable()){
                    break;
                }

                reloadTarget();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getScore(MapLocation loc) {
        if(next != null && loc.distanceSquaredTo(next)<=1){
            return Robot.INTEREST_EXPLORE;
        }
        return 0;
    }
}
