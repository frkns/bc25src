package architecture.Actions;

import architecture.RobotPlayer;
import architecture.Tools.Debug;
import architecture.Tools.Pathfinder;
import architecture.Tools.Utils;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

public class ActionAttackWave extends RobotPlayer {

    static MapInfo[] _attackableNearbyTiles;  // var names that start with an underscore are set static to save bytecode
    static MapLocation target;
    // create an array of the 3 possible symetries for enemy spawn location
    static MapLocation[] potentialEnemySpawnLocations = new MapLocation[3];

    static boolean[] visited = new boolean[3];
    static MapLocation nextLocToExplore = null;

    static boolean visFstTowerTarget = false;


    public static void init() throws GameActionException {
        potentialEnemySpawnLocations[0] = Utils.mirror(spawnTowerLocation);
        potentialEnemySpawnLocations[1] = Utils.verticalMirror(spawnTowerLocation);
        potentialEnemySpawnLocations[2] = Utils.horizontalMirror(spawnTowerLocation);


        if (spawnTowerLocation == null) {
            System.out.println("Spawn tower location not found? This should not happen.");
        }
    }


    public static void run() throws GameActionException {
        switch (RobotPlayer.action) {
            case Action.ACTION_ATTACK_WAVE:
            case Action.ACTION_WAITING_FOR_ACTION:
                break;
            default:
                // We are already playing an action
                return;
        }

        //------------------------------------------------------------------------------//
        // Init
        //------------------------------------------------------------------------------//
        // Update nextLocToExplore to the nearest unvisited location
        nextLocToExplore = null; // Check if robots are not oscillating
        int minDistance = 3600;

        for (int i = 0; i < 3; i++) {
            MapLocation potentialLoc = potentialEnemySpawnLocations[i];

            if (visited[i]) {
                continue;
            }

            if (rc.getLocation().isWithinDistanceSquared(potentialLoc, 20)) {
                visited[i] = true;
                continue;
            }

            int distance = rc.getLocation().distanceSquaredTo(potentialLoc);
            if (distance < minDistance) {
                minDistance = distance;
                nextLocToExplore = potentialLoc;
            }
        }

        //------------------------------------------------------------------------------//
        // Check if can play action
        //------------------------------------------------------------------------------//
        if (nearestEnemyTower == null && nextLocToExplore == null) {
            Debug.println("\tX - ACTION_ATTACK_WAVE   : No towers or exploration");
            RobotPlayer.action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        // Attack if in range
        if (rc.canAttack(nearestEnemyTower)) {
            Debug.println("\t0 - ACTION_ATTACK_WAVE   : Attacking !");
            rc.attack(nearestEnemyTower);
        }

        // stop attacking if low health, 30 or less means we die to level 1 paint/money tower shot + AoE
        if (rc.getHealth() < 31) {
            Debug.println("\tX - ACTION_ATTACK_WAVE   : Low on health");
            RobotPlayer.action = Action.ACTION_WAITING_FOR_ACTION;
            return;
        }

        //------------------------------------------------------------------------------//
        // Play action
        //------------------------------------------------------------------------------//
        Debug.println("\t0 - ACTION_ATTACK_WAVE   : Playing!");
        action = Action.ACTION_ATTACK_WAVE;

        if (nearestEnemyTower != null) {
            // Play micro

            Pathfinder.move(nearestEnemyTower);
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
            }

        } else {
            // Explore next possible loc
            Pathfinder.move(nextLocToExplore);
        }
    }
}
