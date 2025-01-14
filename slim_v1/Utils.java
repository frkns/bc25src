package slim_v1;
import java.util.Random;
import battlecode.common.*;

// these Utils are pure functions they should not have any side-effects or modify game state in anyway

public class Utils {
    static int chessDistance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }
    static int manhattanDistance(MapLocation A, MapLocation B) {
        return Math.abs(A.x - B.x) + Math.abs(A.y - B.y);
    }
}
