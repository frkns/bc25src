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
    RobotController rc;
    int shift = 0; // Number of rotation to turn left before get back to original direction

    public InterestExplore() {
        super();
        name = "Explore    ";

        rc = Robot.rc;
        dir = directions[rng.nextInt(directions.length)];

    }

    @Override
    public void initTurn() {
        super.initTurn();

        // Adding some random
        if(Robot.rng.nextInt(3) == 0){
            dir = dir.rotateLeft();
        }
        if(Robot.rng.nextInt(3) == 0){
            dir = dir.rotateRight();
        }


        if(shift > 0){
            dir = dir.rotateLeft();
            shift--;
        }
        next = rc.getLocation().add(dir);


        for(int i=0; i<5; i++){
            try {
                if(rc.canSenseLocation(next) && rc.senseMapInfo(next).isPassable()){
                    break;
                }

                dir = dir.rotateRight();
                shift++;
                next = rc.getLocation().add(dir);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int getScore(MapLocation loc) {
        if(next != null && loc.isAdjacentTo(next)){
            return Robot.INTEREST_EXPLORE;
        }
        return 0;
    }
}
