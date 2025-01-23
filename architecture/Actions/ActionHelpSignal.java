package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.ImpureUtils;
import architecture.Tools.Pathfinder;
import battlecode.common.GameActionException;
import battlecode.common.PaintType;
import battlecode.common.UnitType;

public class ActionHelpSignal extends RobotPlayer {
    static int lastRoundHelpSignal = -100;
    static int periodBeetweenSignal = 5;

    public static void remove() throws GameActionException {
        if(rc.senseMapInfo(rc.getLocation()).getMark() == PaintType.ALLY_SECONDARY){
            rc.removeMark(rc.getLocation());
        }
    }

    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_FILL_RUINS:
            case Action.ACTION_FILL_SRP:
            case Action.ACTION_ATTACK_MICRO:
            case Action.ACTION_ATTACK_WAVE:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Check
        //------------------------------------------------------------------------------//

        // Already to many robots
        if(rc.senseNearbyRobots(-1, rc.getTeam()).length > 5){
            return;
        }

        // Low on paint
        if(rc.getPaint() > 30){
            return;
        }

        // Enough paint
        if (rc.getPaint() < 31 || rc.getRoundNum() < 10) {
            return;
        }

        // Not spamming help
        if(rc.getRoundNum() - lastRoundHelpSignal < periodBeetweenSignal){
            return;
        }

        //------------------------------------------------------------------------------//
        // Play
        //------------------------------------------------------------------------------//
        lastRoundHelpSignal = rc.getRoundNum();
        rc.mark(rc.getLocation(), true);
    }
}
