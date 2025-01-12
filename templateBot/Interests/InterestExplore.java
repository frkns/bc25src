package templateBot.Interests;

import battlecode.common.MapLocation;
import templateBot.Robot;

public class InterestExplore extends Interest {
    public InterestExplore(Robot r) {
        super(r);
    }

    @Override
    public int getScore(MapLocation loc) {
        return 0;
    }
}
