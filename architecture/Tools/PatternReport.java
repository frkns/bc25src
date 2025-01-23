package architecture.Tools;
import battlecode.common.MapLocation;

public class PatternReport {
    public int numWrongTiles;
    public int numberUnknown;
    public MapLocation nearestWrongPaint;
    public MapLocation nearestWrongEnemies;

    public PatternReport(
            int numWrongTiles,
            int numberUnknown,
            MapLocation nearestWrongPaint,
            MapLocation nearestWrongEnemies
    ) {
        this.numWrongTiles = numWrongTiles;
        this.numberUnknown = numberUnknown;
        this.nearestWrongPaint = nearestWrongPaint;
        this.nearestWrongEnemies = nearestWrongEnemies;
    }
}