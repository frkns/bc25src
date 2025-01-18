package e.interests.unit;

import battlecode.common.*;
import e.Robot;
import e.interests.Interest;
import e.knowledge._Info;
import e.knowledge.Constants;
import e.utils.Debug;

// If the robot does not have a target SRP center, find one and set that as the center of the next SRP to validate.
public class MarkTiles extends Interest {
    public RobotController rc;

    public MarkTiles(){
        rc = Robot.rc;
        name = "MarkTiles";
        debugInterest = true;
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);
        Debug.print(3, "[MarkTiles] updateDirectionScores called", debugInterest);
        Debug.print(3, "[MarkTiles] Tiles to mark size: " + _Info.tilesToMark.size(), debugInterest);

        if (_Info.tilesToMark.size() > 0) {
            MapLocation tileToMark = _Info.tilesToMark.top();
            Debug.print(3, "[MarkTiles] Next tile to mark: " + tileToMark, debugInterest);
            
            if (tileToMark != null) {
                Direction dirToTile = _Info.robotLoc.directionTo(tileToMark);
                Debug.print(3, "[MarkTiles] Direction to tile: " + dirToTile, debugInterest);
                adjustDirectionScore(dirToTile.rotateLeft(), Constants.MarkTilesScore - 2);
                adjustDirectionScore(dirToTile, Constants.MarkTilesScore);
                adjustDirectionScore(dirToTile.rotateRight(), Constants.MarkTilesScore - 2);
                Debug.print(3, "[MarkTiles] Adjusted direction score for: " + dirToTile + " by " + Constants.MarkTilesScore, debugInterest);
            } else {
                Debug.print(3, "[MarkTiles] Tile to mark is null!", debugInterest);
            }
        } else {
            Debug.print(3, "[MarkTiles] No tiles to mark", debugInterest);
        }
    }

}
