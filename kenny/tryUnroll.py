import re

def unroll_loops(java_code):
    # Unroll main direction loops (for i from 7 down to 0)
    java_code = re.sub(
        r'for\s*\(\s*int\s+i\s*=\s*8;\s*i--\s*>\s*0;\s*\)\s*{([^}]*)}',
        lambda m: unroll_loop_body(m.group(1)),
        java_code,
        flags=re.DOTALL
    )

    # Unroll max/min finding loops
    java_code = re.sub(
        r'for\s*\(\s*int\s+i\s*=\s*8;\s*i--\s*>\s*0;\s*\)\s*{([^}]*)}',
        lambda m: unroll_minmax_loop(m.group(1)),
        java_code,
        flags=re.DOTALL
    )

    return java_code

def unroll_loop_body(body):
    unrolled = []
    for i in range(7, -1, -1):
        code = body.replace('i]', f'{i}]')
        code = code.replace('directions[i]', f'directions[{i}]')
        code = code.replace('directionCost[i]', f'directionCost[{i}]')
        code = code.replace('i-- > 0;', 'false;')  # Prevent accidental matches
        code = re.sub(r'\bi\b', str(i), code)
        unrolled.append(f'// i={i}\n{code.strip()}')
    return '\n\n'.join(unrolled)

def unroll_minmax_loop(body):
    unrolled = []
    for i in range(7, -1, -1):
        code = body.replace('i]', f'{i}]')
        code = code.replace('directionCost[i]', f'directionCost[{i}]')
        code = re.sub(r'\bi\b', str(i), code)
        unrolled.append(code.strip())
    return '\n'.join(unrolled)

if __name__ == '__main__':
    with open('HeuristicPath.java', 'r') as f:
        code = f.read()

    unrolled_code = unroll_loops(code)

    # Post-process to handle variable declarations
    unrolled_code = re.sub(
        r'Direction dir = directions\[(\d+)\];',
        lambda m: f'Direction dir{m.group(1)} = directions[{m.group(1)}];',
        unrolled_code
    )

    unrolled_code = re.sub(
        r'MapLocation newLoc = rc\.adjacentLocation\(dir(\d+)\);',
        lambda m: f'MapLocation newLoc{m.group(1)} = rc.adjacentLocation(dir{m.group(1)});',
        unrolled_code
    )

    # Add missing closing braces
    unrolled_code = re.sub(
        r'(minDir = directions\[\d+\];)',
        r'\1\n}',
        unrolled_code
    )

    with open('y', 'w') as f:
        f.write(unrolled_code)