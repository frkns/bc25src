package e_action.interests.unit;

import battlecode.common.*;

import e_action.Robot;
import e_action.utils.Constants;
import e_action.knowledge._Info;

public abstract class StayOnAllyPaint {
    public RobotController rc;
    public String name = "ABSTRACT Int.";
    public boolean debugInterest = false;

    public static int [] directionScores = new int[9]; // TODO use cheap hash map instead

    public StayOnAllyPaint(){
        rc = Robot.rc;
    }

    public void initUnit() throws GameActionException {

    }

    public void updateDirectionScores() throws GameActionException {
        for(int i  = 0 ; i < _Info.directions.length ; i++) {
            Direction direction = _Info.directions[i];
            PaintType paint = rc.senseMapInfo(rc.getLocation().add(direction)).getPaint();

            if(paint.isEnemy()) {
                directionScores[i] = -5;
            } else if (paint == PaintType.EMPTY){
                directionScores[i] = -2;
            }
        }
    }
    public void resetDirectionScores() throws GameActionException {
        directionScores[0] = 0;
        directionScores[1] = 0;
        directionScores[2] = 0;
        directionScores[3] = 0;
        directionScores[4] = 0;
        directionScores[5] = 0;
        directionScores[6] = 0;
        directionScores[7] = 0;
        directionScores[8] = 0;
    }
    public void addDirectionScore(Direction dir, int score){
        directionScores[dir.ordinal()] = Constants.ExploreScore;
    }
}
