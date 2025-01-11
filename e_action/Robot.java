package e_action;

import e_action.interests.Interest;
import e_action.actions.Action;
import e_action.utils.*;

import battlecode.common.*;

import java.util.ArrayDeque;
import java.util.Random;

public abstract class Robot{

    public static RobotController rc;
    public static ArrayDeque<Action> actions = new ArrayDeque<>();
    public static ArrayDeque<Interest> interests = new ArrayDeque<>();


    public Robot(RobotController r){
        rc = r;
        // messageUnit = new MessageUnit(this);
        Debug.init();
        Pathfinder.init(rc);
    }

    // Only runs on a unit's first turn
    public void init() throws GameActionException{
        Debug.print(0, "Create unit => " + rc.getType() + " at " + rc.getLocation());
        Info.init();
        for (Interest interest: interests){
            interest.initUnit();
        }
        for(Action action: actions){
            action.initUnit();
        }
    }

    public void initTurn() throws GameActionException {
        Interest.resetDirectionScores();
        Info.update();
        // Comms.update();
    }

    public void playTurn() throws GameActionException {
        Debug.reset(rc.getRoundNum()); // Reset clock to benchmark

        Debug.print(0, "Start turn => " + rc.getType() + " at " + rc.getLocation());
        Debug.print(1, "Init.");
        initTurn();

        Debug.print(1, "");
        Debug.print(1, "Calculate interests.");
        for (Interest interest: interests){
            Debug.print(2, interest.name + " ...", interest.debugInterest);
            interest.updateDirectionScores();
        }
        

        Debug.print(1, "");
        Debug.print(1, "Calculate actions.");
        for(Action action: actions){
            Debug.print(2, action.name + " ...", action.debugAction);
            action.calcScore();
            int score = action.getScore();
            switch (action.type){
                case 1: // Regular action
                    if(score > bestActionScore){
                        bestActionScore = score;
                        bestActionAction = action;
                    }
                    break;
                case 2: // Combo action

                    if(score > bestComboScore){
                        bestComboScore = score;
                        bestComboAction = action;
                    }
                    break;
            }

            Debug.print(2,  "SCORE : " + score, action.debugAction);
            Debug.print(2,  "", action.debugAction);
        }

        Debug.print(1, "");

        // Debug.print(1, "Playing action: " + bestMoveAction.name + " with directionScores " + bestMoveScore)
        // Debug.setActionIndicatorString();
        } else {
            Debug.print(1, "No action to play");
        }
    }

    public void endTurn(){
        Debug.print(0, "End turn.");
    }
}