def cell_on_range(position, radius):
    x_start, y_start = position

    cells = []
    sqrt_radius = int(radius ** 0.5) + 1
    for x in range(- sqrt_radius, sqrt_radius + 1):
        for y in range(- sqrt_radius, sqrt_radius + 1):
            if x**2 + y**2 <= radius:
                cells.append((x_start + x, y_start + y))

    return cells

def array_difference(arr1, arr2):
    return [elem1 for elem1 in arr1 if elem1 not in arr2]

def convert_rec(element, type_principale, convert_map_location):
    # Easy type
    if(isinstance(element, type_principale)):
        if(isinstance(element, int)):
            return str(element);

        if(isinstance(element, str)):
            return element

        raise ValueError("Convert rec: Can't convert type if not the same has list. " + repr(element))    

    # Convert tuple to list, same behavior
    if(isinstance(element, tuple)):
        element = list(element)

    if(isinstance(element, list)):
        # Empty list
        if(len(element) == 0):
            return "{}"

        # Check wrong list
        if not all(type(x) == type(element[0]) for x in element):
            raise ValueError("Convert rec : Cant convert mixed array. " + repr(element))

        # Check if corresponding to MapLocation 
        if(len(element) == 2 and convert_map_location and isinstance(element[0], int)):
            return f"new MapLocation({element[0]}, {element[1]})"

        # Check if composed of final element
        if type(element[0]) not in (tuple, list) or (type(element[0]) in (list, tuple) and len(element[0]) == 2 and convert_map_location and isinstance(element[0][0], int)): 
            # Result in one line
            return "{" + ", ".join([convert_rec(x, type_principale, convert_map_location) for x in element]) + "}"

        # Check if composed of list for multi line print
        if type(element[0]) in (tuple, list): 
            new_elements = [convert_rec(x, type_principale, convert_map_location).replace("\n", "\n\t") for x in element]
            return "{\n\t" + ",\n\t".join(new_elements) + "\n}"


    raise ValueError("Convert rec : Element " + repr(element) + " Not supported.")


def python_to_java_string(tableau, start, type_principale, convert_map_location):
    """
    Convertit un tableau Python en un tableau Java.

    Args:
        tableau (list): Tableau Python à convertir (listes imbriquées).

    Returns:
        str: Code Java correspondant au tableau Python.
    """

    return start + " = " + convert_rec(tableau, type_principale, convert_map_location) + ";"

if __name__ == "__main__":
    # Exemple d'utilisation
    tableau = [[1, 2], [3, 4, 5]]
    print(python_to_java_string(tableau, "int[][] demo", int, False))

    print("\n\n---\n\n")
    radius_squared = 20
    result = []
    my_cells = cell_on_range((0,0), 20)
    for position in ((0, 1), (1, 1), (1, 0), (1, -1), (0, -1), (-1, -1), (-1, 0), (-1, 1)):
        result.append(array_difference(
            my_cells,
            cell_on_range(position, 20)
        ))

    print(python_to_java_string(result, "MapLocation[][] shifts", int, True))

