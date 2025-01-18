package e.interests.unit;

import battlecode.common.*;
import e.Robot;
import e.interests.Interest;
import e.knowledge._Info;
import e.knowledge.Constants;
import e.utils.Debug;

// If the robot does not have a target SRP center, find one and set that as the center of the next SRP to validate.
public class UnmarkTiles extends Interest {
    public RobotController rc;

    public UnmarkTiles(){
        rc = Robot.rc;
        name = "UnmarkTiles";
        debugInterest = false;
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);
        Debug.print(3, "[UnmarkTiles] updateDirectionScores called", debugInterest);
        Debug.print(3, "[UnmarkTiles] Tiles to unmark size: " + _Info.tilesToMark.size(), debugInterest);

        if (_Info.tilesToMark.size() > 0) {
            MapLocation tileToMark = _Info.tilesToMark.top();
            Debug.print(3, "[UnmarkTiles] Next tile to unmark: " + tileToMark, debugInterest);
            
            if (tileToMark != null) {
                Direction dirToTile = _Info.robotLoc.directionTo(tileToMark);
                Debug.print(3, "[UnmarkTiles] Direction to tile: " + dirToTile, debugInterest);
                adjustDirectionScore(dirToTile, Constants.UnmarkTilesScore);
                Debug.print(3, "[UnmarkTiles] Adjusted direction score for: " + dirToTile + " by " + Constants.UnmarkTilesScore, debugInterest);
            } else {
                Debug.print(3, "[UnmarkTiles] Tile to unmark is null!", debugInterest);
            }
        } else {
            Debug.print(3, "[UnmarkTiles] No tiles to unmark", debugInterest);
        }
    }

}
