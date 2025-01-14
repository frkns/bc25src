package heuristic_test;

import battlecode.common.*;

import java.awt.*;

//phase 2 for soldiers
//keep spreading out and build paint towers
public class Phase2 extends RobotPlayer{
    public static MapLocation target;
    public static Direction direction;
    public static void run (RobotController rc) throws GameActionException {
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();

        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        // Search for a nearby ruin to complete.
        MapInfo curRuin = null;
        int distance = Integer.MAX_VALUE;

        for(RobotInfo robot : rc.senseNearbyRobots()) {
            if(rc.canUpgradeTower(robot.getLocation())) {
                rc.upgradeTower(robot.getLocation());
            }
        }

        for (MapInfo tile : nearbyTiles){
            if (tile.hasRuin() && !rc.canSenseRobotAtLocation(tile.getMapLocation())){
                if(tile.getMapLocation().distanceSquaredTo(rc.getLocation()) < distance){
                    distance = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
                    curRuin = tile;
                }
            }
        }

        boolean attack = false;

        if(!FillPattern.play(rc,UnitType.LEVEL_ONE_PAINT_TOWER,false)) {
            attack = true;
        }

        if(attack == true) {
            MapLocation fill = FillPattern.locatePattern(rc);
            if(fill != null) {
                FillPattern.fillInPattern(rc,fill);
            } else {
                FillPattern.fillInPattern(rc,rc.getLocation());
            }
        }
        //NORTH WEST, NORTH EAST, SOUTHWEST, SOUTHEAST
        // int[] directionChance = {10,10,10,10};
        // for(RobotInfo robot : rc.senseNearbyRobots()) {
        //     if(robot.getTeam().isPlayer()) {
        //         switch (rc.getLocation().directionTo(robot.getLocation())) {
        //             case NORTH:
        //                 directionChance[0]--;
        //                 directionChance[1]--;
        //                 break;
        //                 case SOUTH:
        //                     directionChance[2]--;
        //                     directionChance[3]--;
        //                     break;
        //                     case EAST:
        //                         directionChance[1]--;
        //                         directionChance[3]--;
        //                         break;
        //                         case WEST:
        //                             directionChance[2]--;
        //                             directionChance[0]--;
        //                             break;
        //             case NORTHEAST:
        //                 directionChance[1]--;
        //                 break;
        //                 case SOUTHEAST:
        //                     directionChance[3]--;
        //                     break;
        //                     case SOUTHWEST:
        //                         directionChance[2]--;
        //                         break;
        //                         case NORTHWEST:
        //                             directionChance[0]--;
        //                             break;
        //         }
        //     }
        // }

        // int total = directionChance[0] + directionChance[1] + directionChance[2] + directionChance[3];
        // int cumulative = 0;
        // if(direction == null) {
        //     for(int i = 0; i < directionChance.length; i++) {
        //         cumulative += directionChance[i];
        //         if(rng.nextInt(total) < cumulative) {
        //             switch (i) {
        //                 case 0:
        //                     direction = Direction.NORTHWEST;
        //                     break;
        //                     case 1:
        //                         direction = Direction.NORTHEAST;
        //                         break;
        //                         case 2:
        //                             direction = Direction.SOUTHWEST;
        //                             break;
        //                             case 3:
        //                                 direction = Direction.SOUTHEAST;
        //             }
        //             break;
        //         }
        //     }
        // }

        // int t = 0;

        // while((!rc.canMove(direction) && t < 15) || rc.senseMapInfo(rc.getLocation().add(direction)).getMark() == PaintType.ENEMY_SECONDARY || rc.senseMapInfo(rc.getLocation().add(direction)).getMark() == PaintType.ENEMY_PRIMARY){
        //     total = directionChance[0] + directionChance[1] + directionChance[2] + directionChance[3];
        //     cumulative = 0;
        //     for(int i = 0; i < directionChance.length; i++) {
        //         cumulative += directionChance[i];
        //         if (rng.nextInt(total) < cumulative) {
        //             switch (i) {
        //                 case 0:
        //                     direction = Direction.NORTHWEST;
        //                     break;
        //                 case 1:
        //                     direction = Direction.NORTHEAST;
        //                     break;
        //                 case 2:
        //                     direction = Direction.SOUTHWEST;
        //                     break;
        //                 case 3:
        //                     direction = Direction.SOUTHEAST;
        //             }
        //             break;
        //         }
        //         t++;
        //     }
        // }

        // if(rc.canMove(direction) && (rc.senseMapInfo(rc.getLocation().add(direction)).getMark() != PaintType.ENEMY_SECONDARY && rc.senseMapInfo(rc.getLocation().add(direction)).getMark() != PaintType.ENEMY_PRIMARY)) {
        //     rc.move(direction);
        // }

        HeurisitcPath.move();

        /*
        for(MapInfo tile : nearbyTiles) {
            if(rc.canCompleteResourcePattern(tile.getMapLocation())) {
                rc.completeResourcePattern(tile.getMapLocation());
            }
        }
         */

    }
}