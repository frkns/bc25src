package ryan;

import battlecode.common.*;

public class Splashers extends RobotPlayer {
    // Movement target variables
    static MapLocation target;
    static int targetChangeWaitTime = mx;
    static int lastTargetChangeRound = 0;
    
    static int stopQuadrantModifierPhase = mx * 1;
    
    // Tracking attackable tiles
    static MapInfo[] _attackableNearbyTiles;

    public static void run() throws GameActionException {
        // Update nearby unit information
        ImpureUtils.updateNearbyUnits();

        // Check and execute self-destruct if conditions are met
        if (Utils.selfDestructRequirementsMet()) {
            System.out.println("Self destructing...  Type: " + rc.getType() + ", Round: " + rc.getRoundNum() + ", Nearby Friend Robots: " + nearbyFriendlyRobots + ", Paint: " + rc.getPaint());
            rc.disintegrate();
        }

        // Handle tower targeting and attacks
        ImpureUtils.updateNearestEnemyTower();
        if (nearestEnemyTower != null && rc.getRoundNum() > siegePhase) {
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
            if (rc.isMovementReady())
                HeuristicPath.towerMicro();
            inTowerRange = false;
            if (rc.canAttack(nearestEnemyTower)) {
                rc.attack(nearestEnemyTower);
                inTowerRange = true;
            }
        }

        // Handle paint refilling logic
        boolean canRefill = true;
        MapLocation paintTarget = nearestPaintTower;
        if (paintTarget == null) {
            paintTarget = spawnTowerLocation;
            if (spawnTowerType == UnitType.LEVEL_ONE_DEFENSE_TOWER) {
                canRefill = false;
                isRefilling = false;
            }
        }
        ImpureUtils.withdrawPaintIfPossible(paintTarget);

        isRefilling = rc.getPaint() < 100 && canRefill;

        // Move towards paint source if refilling
        if (isRefilling) {
            HeuristicPath.fullFill = false;
            Pathfinder.move(paintTarget);
            rc.setIndicatorLine(rc.getLocation(), paintTarget, 131, 252, 131);
        }

        // Random movement and target selection
        if (target == null
            || rc.getLocation().isWithinDistanceSquared(target, 9)
            || rc.getRoundNum() - lastTargetChangeRound > targetChangeWaitTime) {

            // First priority: Find enemy paint
            ImpureUtils.updateNearestEnemyPaint();
            if (nearestEnemyPaint != null) {
            target = nearestEnemyPaint;
            } 
            // Second priority: Move toward map center initially
            else if (rc.getRoundNum() > stopQuadrantModifierPhase) {
            target = new MapLocation(
                mapCenter.x + rng.nextInt(11) - 5,
                mapCenter.y + rng.nextInt(11) - 5
            );
            }
            // Fall back to random exploration
            else {
            target = new MapLocation(rng.nextInt(mapWidth), rng.nextInt(mapHeight));
            }
            lastTargetChangeRound = rc.getRoundNum();
        }

        // Movement and painting logic
        boolean fullFilling = rc.getRoundNum() >= fullFillPhase;
        
        if (fullFilling) {
            ImpureUtils.updateNearestEmptyTile();
        }

        if (rc.isMovementReady()) {
            HeuristicPath.fullFill = fullFilling;
            HeuristicPath.targetIncentive = 500;
            HeuristicPath.move(target);
        }

        // Paint floor tiles when appropriate
        if (rc.getNumberTowers() >= startPaintingFloorTowerNum && !isRefilling)
            ImpureUtils.paintFloor();

        // Attack empty tiles when possible
        if (rc.isActionReady() && fullFilling && !isRefilling) {
            _attackableNearbyTiles = rc.senseNearbyMapInfos(9);
            for (MapInfo tile : _attackableNearbyTiles) {
                if (tile.getPaint() == PaintType.EMPTY && rc.canAttack(tile.getMapLocation())) {
                    rc.attack(tile.getMapLocation());
                }
            }
        }
    }
}
