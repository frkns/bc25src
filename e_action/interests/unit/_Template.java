package e_action.interests.unit;

import e_action.Robot;
import e_action.interests.Interest;
import e_action.utils.*;
import e_action.knowledge._Info;

import battlecode.common.*;

import battlecode.common.*;
public class _Template extends Interest {
    public RobotController rc;


    public _Template(){
        rc = Robot.rc;
        name = "FUNC NAME HERE";
        debugInterest = false;
        Debug.print(3, Debug.INIT + name, debugInterest);
    }

    public void initUnit()  throws GameActionException {
        Debug.print(1, Debug.INITUNIT + name, debugInterest);
    }


    public void updateDirectionScores() throws GameActionException {
        Debug.print(3, Debug.UPDATE_DIR_SCORES + name, debugInterest);

        // Use the function below to assign a score to the variable
        // adjustDirectionScore(dir, Constants.ExploreScore);
     }
}
