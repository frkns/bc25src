package architecture.Tools;
import battlecode.common.MapLocation;

public class PatternReport {
    public int numWrongTiles;
    public int numberUnknow;
    public MapLocation nearestWrongPaint;
    public MapLocation nearestWrongEnemie;

    public PatternReport(
            int numWrongTiles,
            int numberUnknow,
            MapLocation nearestWrongPaint,
            MapLocation nearestWrongEnemie
    ) {
        this.numWrongTiles = numWrongTiles;
        this.numberUnknow = numberUnknow;
        this.nearestWrongPaint = nearestWrongPaint;
        this.nearestWrongEnemie = nearestWrongEnemie;
    }
}