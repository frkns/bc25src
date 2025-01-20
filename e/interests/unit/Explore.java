package e.interests.unit;

import battlecode.common.*;
import e.Robot;
import e.interests.Interest;
import e.knowledge.*;
import e.utils.*;



public class Explore extends Interest {    
    MapLocation checkLoc;

    public Explore(){
        rc = Robot.rc;
        name = "Explore";
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INIT + name, debugInterest);
    }

    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);

        checkLoc = _Info.robotLoc.translate(3, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.EAST.ordinal()] += Constants.ExploreScore;
        checkLoc = _Info.robotLoc.translate(-3, 0); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.WEST.ordinal()] += Constants.ExploreScore;
        checkLoc = _Info.robotLoc.translate(0, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTH.ordinal()] += Constants.ExploreScore;
        checkLoc = _Info.robotLoc.translate(0, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTH.ordinal()] += Constants.ExploreScore;
        checkLoc = _Info.robotLoc.translate(3, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHEAST.ordinal()] += Constants.ExploreScore;
        checkLoc = _Info.robotLoc.translate(-3, 3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.NORTHWEST.ordinal()] += Constants.ExploreScore;
        checkLoc = _Info.robotLoc.translate(3, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHEAST.ordinal()] += Constants.ExploreScore;
        checkLoc = _Info.robotLoc.translate(-3, -3); if (rc.onTheMap(checkLoc) && rc.senseMapInfo(checkLoc).getPaint() == PaintType.EMPTY) directionScores[Direction.SOUTHWEST.ordinal()] += Constants.ExploreScore;

        Debug.printDirectionScores(3, Interest.directionScores, debugInterest);
    }
}
