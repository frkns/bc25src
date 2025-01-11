package e_action.interests;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import e_action.Robot;
import e_action.utils.Constants;

public abstract class Interest {
    public RobotController rc;
    public String name = "ABSTRACT Int.";
    public boolean debugAction = false;

    public static int [] directionScores = new int[9]; // TODO use cheap hash map instead

    public Interest(){
        rc = Robot.rc;
    }

    public abstract void initUnit() throws GameActionException;
    public abstract void updateDirectionScores() throws GameActionException;
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
