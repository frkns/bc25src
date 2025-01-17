package e.utils.fast;

import battlecode.common.RobotInfo;

public class FastUnitSet {
    char size = 0;
    char maxSize;
    char[] hashmap; // hashmap[id] = index of robot in data.
    RobotInfo[] data;

    public FastUnitSet(char maxLen){
        maxSize = maxLen;

        data = new RobotInfo[maxSize + 1]; // Last element is null
        hashmap = String.valueOf(maxSize).repeat(20000).toCharArray();

        data[maxSize] = null;
    }

    public void add(RobotInfo robot){
        // If robot not in data, give index to hashmap[id]
        if(hashmap[robot.ID] != maxSize) {
            if(size == maxSize){
                size = 0;
                hashmap = String.valueOf(maxSize).repeat(20000).toCharArray();

                System.out.println("FastUniqueUnit: Reach max size. Flushing data.");
            }

            hashmap[robot.ID] = size;
            size++;
        }

        // Updating value
        data[hashmap[robot.ID]] = robot;
    }

    public RobotInfo get(int id){
        return data[hashmap[id]];
    }

    public void pop(int id){
        if(hashmap[id] != maxSize){

            // Replace with last element
            data[hashmap[id]] = data[size+1];
            hashmap[ // Update the index of moved element
                    data[hashmap[id]].ID
                    ] = hashmap[id]; // That it is now at hashmap[id] location

            hashmap[id] = maxSize; // Pointing to null
            size--;
        }
    }
}
