package c_actiontemp.Interests;

import battlecode.common.MapLocation;
import c_actiontemp.Robot;

public class InterestConsistancy extends Interest {
    final int N = 5;
    MapLocation[] locations = new MapLocation[N];
    MapLocation nextLocation;
    int i = 0;
    public InterestConsistancy() {
        super();
        name = "Consistancy";

        rc = Robot.rc;
        for(int i=0; i<N; i++)
            locations[i] = rc.getLocation();
    }

    @Override
    public void initTurn() {
        locations[i] = rc.getLocation();
        nextLocation = null;
        i = (i+1) % N;

        MapLocation cumulateLocation = new MapLocation(0, 0);
        for(int i = 0; i<N; i++){
            cumulateLocation = cumulateLocation.translate(locations[i].x, locations[i].y);
        }

        MapLocation meanLocation = new MapLocation(cumulateLocation.x / N, cumulateLocation.y / N);
        nextLocation = rc.getLocation().add(meanLocation.directionTo(rc.getLocation()));
    }

    @Override
    public int getScore(MapLocation loc) {
        if(nextLocation != null && loc.distanceSquaredTo(nextLocation) <= 4){
            return Robot.INTEREST_CONSISTANCY;
        }
        return 0;
    }
}
