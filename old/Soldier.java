package old;

import battlecode.common.*;

import java.util.Random;

public class Soldier extends RobotPlayer {
    static MapLocation spawnLocation;
    static MapLocation nearestPaintTower;
    static RobotController rc;
    static UnitType bunnyType;

    static Direction direction;
    static MapLocation target;


    static void runSoldier() throws GameActionException {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        for (RobotInfo robot : nearbyRobots) {
            if (phase == 3 && rc.canUpgradeTower(robot.getLocation())) {
                rc.upgradeTower(robot.getLocation());
            }
            if (robot.getType().isTowerType()) {
                if (robot.getType().getBaseType() == UnitType.LEVEL_ONE_PAINT_TOWER) {
                    if (nearestPaintTower == null || rc.getLocation().distanceSquaredTo(robot.getLocation()) < rc
                            .getLocation().distanceSquaredTo(nearestPaintTower)) {
                        nearestPaintTower = robot.getLocation();
                    }
                }
            }
        }

        if (rc.getLocation() == target) {
            target = null;
        }

        int myPaint = rc.getPaint();
        int amtTillFull = bunnyType.paintCapacity - myPaint;
        if (nearestPaintTower != null && amtTillFull > 50 && rc.canTransferPaint(nearestPaintTower, -1 * amtTillFull)) {
            rc.transferPaint(nearestPaintTower, -1 * amtTillFull);
            target = null;
        }
        if (myPaint < 40 && nearestPaintTower != null) {
            target = nearestPaintTower;
        }

        if (target != null) {
            // if (rc.canMove(rc.getLocation().directionTo(target))) {
            //     rc.move(rc.getLocation().directionTo(target));
            // }
            Pathfinder.move(target);
        }

        // build ruin
        if (phase == 1)
            RunesGavin.findRuinAndBuildTower(rc, UnitType.LEVEL_ONE_MONEY_TOWER);
        if (phase == 2)
            RunesGavin.findRuinAndBuildTower(rc, UnitType.LEVEL_ONE_PAINT_TOWER);
        if (phase == 3)
            RunesGavin.findRuinAndBuildTower(rc, UnitType.LEVEL_ONE_DEFENSE_TOWER);

        if (phase == 3) {
            while (true) {
                if (target == null)
                    target = Utils.randomEnemyLocation();
                Direction d = rc.getLocation().directionTo(target);
                int tries = 8;
                while (tries-- > 0 &&
                        (!rc.canMove(d) || rc.senseMapInfo(rc.getLocation().add(d)).getPaint().isEnemy())) {
                    if (rc.getID() % 2 == 0)
                        d = d.rotateRight();
                    else
                        d = d.rotateLeft();
                }
                if (tries > 0) {
                    rc.move(d);
                    break;
                }
            }
        }

        // exploration
        if (phase <= 2) {
            boolean inBoundsNow = !Utils.outOfExplorationBounds(rc.getLocation());
            boolean inBoundsAfter = !Utils.outOfExplorationBounds(rc.getLocation().add(direction));
            MapLocation newLoc = rc.getLocation().add(direction);
            while (inBoundsNow && !inBoundsAfter
                    || !rc.canMove(direction) || rc.senseMapInfo(newLoc).getPaint().isEnemy()) {
                direction = direction.rotateRight();
                inBoundsNow = !Utils.outOfExplorationBounds(rc.getLocation());
                inBoundsAfter = !Utils.outOfExplorationBounds(rc.getLocation().add(direction));
                newLoc = rc.getLocation().add(direction);
            }
            if (rc.canMove(direction)) {
                rc.move(direction);
            }
        }

        // paint the ground if we can
        if (rc.senseMapInfo(rc.getLocation()).getPaint() == PaintType.EMPTY) {
            if (rc.canAttack(rc.getLocation())) {
                rc.attack(rc.getLocation());
            }
        }

        if (paintEverywhere) {
            MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
            // paint empty tiles
            for (MapInfo tile : nearbyTiles) {
                if (tile.getPaint() == PaintType.EMPTY) {
                    if (rc.canAttack(tile.getMapLocation())) {
                        rc.attack(tile.getMapLocation());
                    }
                }
            }
        }

    }

    static void init(RobotController r) throws GameActionException {
        rc = r;
        bunnyType = rc.getType();
        spawnLocation = rc.getLocation();

        // MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        for (RobotInfo robot : nearbyRobots) {
            if (robot.getType().isTowerType()) {
                direction = spawnLocation.directionTo(robot.getLocation()).opposite();
            }
        }
    }

}
