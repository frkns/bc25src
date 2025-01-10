package e_actions.utils;

public class Phase {
    public static int getPhase (int round, int mapSize) {
        if (mapSize < 1000) {
            if (round < ActionConstants.SMALL_PHASE1) {
                return 1;
            } else if (round < ActionConstants.SMALL_PHASE2) {
                return 2;
            } else {
                return 3;
            }
        } else if (mapSize < 2000) {
            if (round < ActionConstants.MEDIUM_PHASE1) {
                return 1;
            } else if (round < ActionConstants.MEDIUM_PHASE2) {
                return 2;
            } else {
                return 3;
            }
        } else if (mapSize < 3000) {
            if (round < ActionConstants.LARGE_PHASE1) {
                return 1;
            } else if (round < ActionConstants.LARGE_PHASE2) {
                return 2;
            } else {
                return 3;
            }
        } else {
            if (round < ActionConstants.HUGE_PHASE1) {
                return 1;
            } else if (round < ActionConstants.HUGE_PHASE2) {
                return 2;
            } else {
                return 3;
            }
        }
    }
}
