package e_action.actions;

import e_action.Robot;

import battlecode.common.*;

public abstract class Action {
    public RobotController rc;
    public String name = "ABSTRACT Act.";
    public int score = 0;

    public Action(){
        rc = Robot.rc;
    }

    public abstract void calcScore() throws GameActionException;
    public abstract int getScore();
    public abstract void play() throws GameActionException;
}
