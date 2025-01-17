front = [(0, 1), (0, 2), (1, 1), (2, 1), (-1, 1), (-1, 2)] # (x,y) for front direction

print("switch(delta.x*1000 + delta.y){")

options = []
for start_x, start_y in front:
    for dx, dy in [(0, 0), (0, 1), (1, 1), (-1, 1)]:
        x = start_x + dx
        y = start_y + dy
        options.extend([
            x*1000 + y,
            y*1000 - x,
            -x*1000 - y,
            -y*1000 + x
        ])

for v in set(options):
    print(f"\tcase {v}:") # Rotate 270°

print("\t\treturn true;")
print("\tdefault: return false;")
print("}")








