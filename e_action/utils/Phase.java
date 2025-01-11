package e_action.utils;

public class Phase {
    public static int getPhase (int round, int mapArea) {
        if (mapArea < 1000) {
            if (round < Constants.SMALL_PHASE1) {
                return 1;
            } else if (round < Constants.SMALL_PHASE2) {
                return 2;
            } else {
                return 3;
            }
        } else if (mapArea < 2000) {
            if (round < Constants.MEDIUM_PHASE1) {
                return 1;
            } else if (round < Constants.MEDIUM_PHASE2) {
                return 2;
            } else {
                return 3;
            }
        } else if (mapArea < 3000) {
            if (round < Constants.LARGE_PHASE1) {
                return 1;
            } else if (round < Constants.LARGE_PHASE2) {
                return 2;
            } else {
                return 3;
            }
        } else {
            if (round < Constants.HUGE_PHASE1) {
                return 1;
            } else if (round < Constants.HUGE_PHASE2) {
                return 2;
            } else {
                return 3;
            }
        }
    }
}
