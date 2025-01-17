import sys
import os
import shutil

def main():
    args = sys.argv

    if len(args) != 3:
        print("Usage: {} <source directory> <destination directory>".format(sys.argv[0]))
        print("Usage: {} initialBot nextBot".format(sys.argv[0]))
        return

    source = args[1]
    destination = args[2]

    # source = source.replace('', '')
    # destination = destination.replace('', '')

    print("Copying {} to {}".format(source, destination))

    # Copy folder
    shutil.copytree(source, destination, dirs_exist_ok=True)

    # Replace all instances of source in the destination directory
    for root, dirs, files in os.walk(destination):
        for file in files:
            file_path = os.path.join(root, file)
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                content = content.replace(source, destination)
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
            except (UnicodeDecodeError, PermissionError):
                # Skip binary files or files that can't be read/written
                continue

if __name__ == "__main__":
    main()