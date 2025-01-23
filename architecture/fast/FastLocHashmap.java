package architecture.fast;

import battlecode.common.MapLocation;

public class FastLocHashmap {
    public char[] data;

    public FastLocHashmap(){
        data = "\u0000".repeat(3600).toCharArray();
    }

    public char get(MapLocation loc){
        return data[loc.x + loc.y * 60];
    }

    public void set(MapLocation loc, char val){
        data[loc.x + loc.y * 60] = val;
    }
}
