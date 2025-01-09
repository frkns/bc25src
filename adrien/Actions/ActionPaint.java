package adrien.Actions;

import battlecode.common.*;
import adrien.Robot;
import adrien.utils.DebugUnit;

public class ActionPaint extends Action {
    MapLocation target;

    public ActionPaint() {
        super();
        name = "PaintUnder  ";
    }

    @Override
    public int getScore() {
        target = null;
        try {
            if (Robot.nearestIncorrectMark != null && rc.canAttack(Robot.nearestIncorrectMark.getMapLocation())) {
                target = Robot.nearestIncorrectMark.getMapLocation();
                name = "PaintUnder  - MARK";
                return Robot.ACTION_PAINT_MARK;
            }

            for (RobotInfo unit : Robot.enemies) {
                if (rc.canAttack(unit.location)
                        && !rc.senseMapInfo(unit.getLocation()).getPaint().isEnemy()
                        && !unit.getType().isTowerType()
                ) {
                    target = unit.location;
                    name = "PaintUnder  - ENEMIES";
                    return Robot.ACTION_PAINT_UNDER_ENEMIES;
                }
            }

            for (RobotInfo unit : Robot.allies) {
                if (rc.canAttack(unit.location) && !rc.senseMapInfo(unit.getLocation()).getPaint().isEnemy()
                        && !unit.getType().isTowerType()
                ) {
                    target = unit.location;
                    name = "PaintUnder  - ALLIES";
                    return Robot.ACTION_PAINT_UNDER_ALLIES;
                }
            }

            if (rc.canAttack(rc.getLocation()) && rc.senseMapInfo(rc.getLocation()).getPaint().equals(PaintType.EMPTY)) {
                target = rc.getLocation();
                name = "PaintUnder  - SELF";
                return Robot.ACTION_PAINT_UNDER_ALLIES;
            }

            for (MapInfo info : rc.senseNearbyMapInfos(rc.getLocation(), Robot.action_range)) {
                if (rc.canAttack(info.getMapLocation()) && info.getPaint().equals(PaintType.EMPTY)) {
                    target = info.getMapLocation();
                    name = "PaintUnder  - EMPTY";
                    return Robot.ACTION_PAINT_EMPTY;
                }
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void play() {
        try {
            if (rc.canAttack(target)) {
                boolean useSecondaryColor = rc.senseMapInfo(target).getMark() == PaintType.ALLY_SECONDARY;
                DebugUnit.print(3, "Paint at " + target);
                rc.attack(target, useSecondaryColor);
                rc.setIndicatorLine(rc.getLocation(), target, 0, 0, 255);
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }

    }
}
