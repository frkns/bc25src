import re
import signal
import subprocess
import sys
from argparse import ArgumentParser
from datetime import datetime
from multiprocessing import Pool, Manager
from pathlib import Path
from typing import Any

# Dynamically calculate the directory containing gradlew.bat
# This assumes the script is located in `src/` and the `java` directory is one level up
SCRIPT_DIR = Path(__file__).resolve().parent  # Absolute path to the script's directory
JAVA_DIR = SCRIPT_DIR.parent  # Absolute path to the `java` directory (parent of `src`)

# Ensure the `java` directory contains the Gradle files
if not (JAVA_DIR / "gradlew.bat").exists():
    raise FileNotFoundError(f"Could not find 'gradlew.bat' in the Java directory: {JAVA_DIR}")

NUM_CORES = 6
assert NUM_CORES % 2 == 0
NUM_CORES_PER_SIDE = NUM_CORES // 2

# Define maps
early_maps = 'DefaultSmall DefaultMedium DefaultLarge DefaultHuge'.split()
sprint1_maps = 'Fossil,Gears,Justice,Mirage,Money,MoneyTower,Racetrack,Restart,SMILE,SaltyPepper,TargetPractice,Thirds,UglySweater,UnderTheSea,catface,gardenworld,memstore'.split(',')
sprint2_maps = []
qual_maps = []
hs_maps = []
all_maps = early_maps + sprint1_maps + sprint2_maps + qual_maps + hs_maps
maps = all_maps
assert all(m in all_maps for m in maps)

def partition_list(maps, n):
    if n <= 0:
        raise ValueError("Number of partitions must be greater than 0")

    partition_length, remainder = divmod(len(maps), n)
    partitions = []
    start = 0
    for i in range(n):
        end = start + partition_length + (1 if i < remainder else 0)
        partitions.append(maps[start:end])
        start = end

    return partitions

def run_matches(player1: str, player2: str, mymaps: list[str], timestamp: str, counter, lock) -> dict[str, Any]:
    result = {
        "player1": player1,
        "player2": player2
    }

    winners_by_map = {}
    current_map = None
    current_result = None

    # Use the absolute path to gradlew.bat
    gradlew_path = JAVA_DIR / "gradlew.bat"
    if not gradlew_path.exists():
        raise FileNotFoundError(f"Could not find 'gradlew.bat' at: {gradlew_path}")

    args = [
        str(gradlew_path),  # Absolute path to gradlew.bat
        "run",
        f"-PteamA={player1}",
        f"-PteamB={player2}",
        f"-Pmaps={','.join(mymaps)}",
    ]

    # Run the command in the `java` directory
    proc = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, cwd=str(JAVA_DIR))

    lines = []
    while True:
        line = proc.stdout.readline()
        if not line:
            break

        line = line.decode("utf-8").rstrip()
        lines.append(line)

        map_match = re.search(r"[^ ]+ vs\. [^ ]+ on ([^ ]+)", line)
        if map_match is not None:
            current_map = map_match[1]
        result_match = re.search(r"([^ ]+) \([AB]\) wins \(round (\d+)\)", line)
        if result_match is not None:
            current_result = result_match
        reason = re.search(r"Reason: (.*)", line)
        if reason is not None:
            reason = reason[1]
            if 'resigned' in reason:
                reason = 'resign!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!'
                print('\n'.join(lines))

            # Increment the shared counter with the lock
            with lock:
                counter.value += 1
                current_match = counter.value

            total_matches = len(maps) * 2
            prefix = f"[{str(current_match).rjust(len(str(total_matches)))}/{total_matches}]"

            winner_color = "red" if current_result[1] == player1 else "blue"

            print(f"{prefix} {current_result[1]} wins in {current_result[2]} rounds as {winner_color} on {current_map}: {reason}")
            winners_by_map[current_map] = current_result[1]
            lines = []

    if proc.wait() != 0:
        result["type"] = "error"
        result["message"] = "\n".join(lines)
        return result

    result["type"] = "success"
    result["winners"] = winners_by_map
    return result

def main() -> None:
    parser = ArgumentParser(description="Compare the performance of two players.")
    parser.add_argument("player1", help="name of the first player")
    parser.add_argument("player2", help="name of the second player")

    args = parser.parse_args()

    signal.signal(signal.SIGINT, lambda a, b: sys.exit(1))

    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")

    # Create a Manager for shared objects
    manager = Manager()
    counter = manager.Value("i", 0)  # Shared counter
    lock = manager.Lock()  # Shared lock

    print(f"Running {len(maps) * 2} matches")

    maps_partition = partition_list(maps, NUM_CORES_PER_SIDE)
    pool_args = [(args.player1, args.player2, m, timestamp, counter, lock) for m in maps_partition] + [
        (args.player2, args.player1, m, timestamp, counter, lock) for m in maps_partition
    ]

    with Pool(NUM_CORES) as pool:
        results = pool.starmap(run_matches, pool_args)

    if any(r["type"] == "error" for r in results):
        for r in results:
            if r["type"] == "error":
                print(f"{r['player1']} versus {r['player2']} failed with the following error:")
                print(r["message"])
        sys.exit(1)

    map_winners = {}

    player1_wins = 0
    player2_wins = 0

    for r in results:
        for map, winner in r["winners"].items():
            if map in map_winners and map_winners[map] != winner:
                map_winners[map] = "Tied"
            else:
                map_winners[map] = winner

            if winner == args.player1:
                player1_wins += 1
            else:
                player2_wins += 1

    tied_maps = [k for k, v in map_winners.items() if v == "Tied"]
    player1_superior_maps = [k for k, v in map_winners.items() if v == args.player1]
    player2_superior_maps = [k for k, v in map_winners.items() if v == args.player2]

    if len(tied_maps) > 0:
        print(f"Tied maps ({len(tied_maps)}):")
        for map in tied_maps:
            print(f"- {map}")
    else:
        print(f"There are no tied maps")

    if len(player1_superior_maps) > 0:
        print(f"Maps {args.player1} wins on as both red and blue ({len(player1_superior_maps)}):")
        for map in player1_superior_maps:
            print(f"- {map}")
    else:
        print(f"There are no maps {args.player1} wins on as both red and blue")

    if len(player2_superior_maps) > 0:
        print(f"Maps {args.player2} wins on as both red and blue ({len(player2_superior_maps)}):")
        for map in player2_superior_maps:
            print(f"- {map}")
    else:
        print(f"There are no maps {args.player2} wins on as both red and blue")

    print(f"{args.player1} wins: {player1_wins} ({player1_wins / (player1_wins + player2_wins) * 100:,.2f}% win rate)")
    print(f"{args.player2} wins: {player2_wins} ({player2_wins / (player1_wins + player2_wins) * 100:,.2f}% win rate)")

    # Generate the new log content
    log_timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    new_log_content = f"\n\n=== Results at {log_timestamp} ===\n"
    new_log_content += f"{args.player1} wins: {player1_wins} ({player1_wins / (player1_wins + player2_wins) * 100:,.2f}% win rate)\n"
    new_log_content += f"{args.player2} wins: {player2_wins} ({player2_wins / (player1_wins + player2_wins) * 100:,.2f}% win rate)\n"

    if len(tied_maps) > 0:
        new_log_content += f"Tied maps ({len(tied_maps)}):\n"
        for map in tied_maps:
            new_log_content += f"- {map}\n"

    if len(player1_superior_maps) > 0:
        new_log_content += f"Maps {args.player1} wins on as both red and blue ({len(player1_superior_maps)}):\n"
        for map in player1_superior_maps:
            new_log_content += f"- {map}\n"

    if len(player2_superior_maps) > 0:
        new_log_content += f"Maps {args.player2} wins on as both red and blue ({len(player2_superior_maps)}):\n"
        for map in player2_superior_maps:
            new_log_content += f"- {map}\n"

    # Read the existing content of the log file
    try:
        with open("log.txt", "r") as log_file:
            existing_content = log_file.read()
    except FileNotFoundError:
        existing_content = ""

    # Write the new content followed by the existing content
    with open("log.txt", "w") as log_file:
        log_file.write(new_log_content + existing_content)



if __name__ == "__main__":
    main()
