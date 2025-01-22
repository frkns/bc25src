# Taille : 70x70
for func_name, sign in (("addScoreTo", ""), ("subScoreTo", "MINUS_")):

    print(f"public static void {func_name}(int cell, char type) " + "{")
    print("switch (type) {")
    print("case 0: break;")
    print("case 1:")
    pattern = """
  P
 PPP
PPPPP
 PPP
  P
    """

    for y, l in enumerate([x for x in pattern.split("\n") if x != ""]):
        for x, c in enumerate(l):
            if(c in (" ", "#")):
                continue

            x_shift = x - 2
            y_shift = y - 2

            print(f"\tscores[cell + { y_shift * 70 + x_shift }] += SCORE_{sign}{c};")

    print("\tbreak;")


    print("case 2:")
    pattern = """
SS SS
S   S
  #
S   S
SS SS
    """
    # I have used # because my editor remove line with only spaces
    for y, l in enumerate([x for x in pattern.split("\n") if x != ""]):
        for x, c in enumerate(l):
            if(c in (" ", "#")):
                continue

            x_shift = x - 2
            y_shift = y - 2

            print(f"\tscores[cell + { y_shift * 70 + x_shift }] += SCORE_{sign}{c};")

    print("\tbreak;")

    print("case 3:")
    print("case 4:")
    pattern = """
EEEEE
EEEEE
EEEEE
EEEEE
EEEEE
    """

    for y, l in enumerate([x for x in pattern.split("\n") if x != ""]):
        for x, c in enumerate(l):
            if(c in (" ", "#")):
                continue

            x_shift = x - 2
            y_shift = y - 2

            print(f"\tscores[cell + { y_shift * 70 + x_shift }] += SCORE_{sign}{c};")

    print("\tbreak;")

    print("// Wall or ruin with tower")
    print("case 5:")
    pattern = """
CBAAABC
BXXXXXB
AXXXXXA
AXXXXXA
AXXXXXA
BXXXXXB
CBAAABC"""

    for y, l in enumerate([x for x in pattern.split("\n") if x != ""]):
        for x, c in enumerate(l):
            if(c in (" ", "#")):
                continue

            x_shift = x - 3
            y_shift = y - 3

            print(f"\tscores[cell + { y_shift * 70 + x_shift }] += SCORE_{sign}{c};")

    print("\tbreak;")

    print("// Empty ruin")
    print("case 6:")
    pattern = """
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
"""

    for y, l in enumerate([x for x in pattern.split("\n") if x != ""]):
        for x, c in enumerate(l):
            if(c in (" ", "#")):
                continue

            x_shift = x - 4
            y_shift = y - 4

            print(f"\tscores[cell + { y_shift * 70 + x_shift }] += SCORE_{sign}{c};")

    print("\tbreak;")


    print("// Other SRP")
    print("case 7:")
    pattern = """
CXXXCXXXC
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
CXXXXXXXC
XXXXXXXXX
XXXXXXXXX
XXXXXXXXX
CXXXCXXXC
"""

    for y, l in enumerate([x for x in pattern.split("\n") if x != ""]):
        for x, c in enumerate(l):
            if(c in (" ", "#")):
                continue

            x_shift = x - 4
            y_shift = y - 4

            print(f"\tscores[cell + { y_shift * 70 + x_shift }] += SCORE_{sign}{c};")

    print("\tbreak;")
    print("}") # Switch
    print("}") # Function
    print("")
