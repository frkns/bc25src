aoe = [[(-1, 1), (0, 1), (1, 1), (-1, 2), (0, 2), (1, 2)], [(1, 1), (1, 0), (1, -1), (2, 1), (2, 0), (2, -1)], [(-1, -1), (0, -1), (1, -1), (-1, -2), (0, -2), (1, -2)], [(-1, 1), (-1, 0), (-1, -1), (-2, 1), (-2, 0), (-2, -1)]]
# Movement  : 9 
# Direction : Up, Right, Down, Left

print("public static void addMoperScore(MapLocation loc){")
print("\tMapLocation myLoc = RobotPlayer.rc.getLocation();")
print("\tint shift = (loc.x - myLoc.y) + (loc.y - myLoc.y) * 100;")
print("\tswitch(shift){")

for x in range(-8, 9):
    for y in range(-8, 9):
        score = 0
        
        inRangeOf = []
        for m, mouvement in enumerate(((0, 0), (-1, 0), (-1, 1), (0, 1), (1, 1), (1, 0), (1, -1), (0, -1), (-1, -1))):
            mx, my = mouvement
            for d, shifts in enumerate(aoe):
                for sx, sy in shifts:
                    if (x,y) == (mx+sx, my+sy):
                        inRangeOf.append((m, d))
                        break

        if inRangeOf != []:
            print(f"\t\tcase {x + y*60}:")
            for m, d in inRangeOf:
                print(f"\t\t\tscores[{m + d*10}]++;")
            print("\t\t\tbreak;")

print("\t}")
print("}")


