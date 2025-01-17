package e.utils.fast;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class FastLocSetCustom {
    char size;
    char maxSize;
    char[] hashmap; // hashmap[id] = index of robot in data.
    MapLocation[] data;

    public FastLocSetCustom(int maxLen){
        size = 0;
        maxSize = (char) maxLen;

        data = new MapLocation[maxSize + 1]; // Last element is null
        hashmap = String.valueOf(maxSize).repeat(3600).toCharArray();

        data[maxSize] = null;
    }

    public void add(MapLocation loc){
        // If robot not in data, give index to hashmap[id]
        int index = loc.x + loc.y * 60;
        if(hashmap[index] == maxSize) {
            if(size == maxSize){
                size = 0;
                hashmap = String.valueOf(maxSize).repeat(20000).toCharArray();

                System.out.println("FastUniqueUnit: Reach max size. Flushing data.");
            }

            hashmap[index] = size;
            size++;
        }

        // Updating value
        data[hashmap[index]] = loc;
    }

    public void pop(MapLocation loc){
        int index = loc.x + loc.y * 60;
        int pointerIndex = hashmap[index];
        if(pointerIndex != maxSize){

            // Pop element
            hashmap[index] = maxSize; // Pointing to null

            size--;
            if(size != 0) {
                // Moving last element to the empty cell
                data[pointerIndex] = data[size];

                // Edit hashmap[last element of data] to point to the new cell
                hashmap[data[size].x + data[size].y * 60] = (char) pointerIndex;
            }
        }
    }
}
