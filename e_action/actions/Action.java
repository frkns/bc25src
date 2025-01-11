package e_action.actions;

import e_action.Robot;

import battlecode.common.*;

public abstract class Action {
    public RobotController rc;
    public String name = "ABSTRACT Act.";
    public int score = 0; 
    public int cooldown_reqs; // None = 0, Action = 1, Move = 2, Action + Move = 3
    public boolean debugAction = false;

    public Action(){
        rc = Robot.rc;
    }

    public abstract void initUnit() throws GameActionException;
    public abstract void calcScore() throws GameActionException;
    public abstract int getScore();
    public abstract void play() throws GameActionException;
}
