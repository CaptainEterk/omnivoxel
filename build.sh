#!/usr/bin/env bash
set -e

SRC_DIR="src"
LIB_DIR="lib"
BUILD_DIR="build"
CLASS_DIR="$BUILD_DIR/classes"
FAT_DIR="$BUILD_DIR/fat"
JAR_FILE="$BUILD_DIR/omnivoxel-client.jar"
MAIN_CLASS="omnivoxel.client.launcher.Launcher"

rm -rf "$BUILD_DIR"

mkdir -p "$CLASS_DIR"
mkdir -p "$FAT_DIR"

# Build recursive dependency classpath.
CLASSPATH=$(find "$LIB_DIR" -type f -name "*.jar" -print0 | \
    xargs -0 printf '%s:')

CLASSPATH="${CLASSPATH%:}"

# Find all source files.
find "$SRC_DIR" -name "*.java" > "$BUILD_DIR/sources.txt"

# Compile.
javac \
    -encoding UTF-8 \
    -cp "$CLASSPATH" \
    -d "$CLASS_DIR" \
    @"$BUILD_DIR/sources.txt"

# Copy our compiled classes.
cp -R "$CLASS_DIR"/. "$FAT_DIR"/

# Extract every dependency recursively.
while IFS= read -r -d '' JAR; do
    echo "Adding $JAR"

    unzip -oq "$JAR" -d "$FAT_DIR"

done < <(find "$LIB_DIR" -type f -name "*.jar" -print0)

# Remove dependency signatures.
find "$FAT_DIR/META-INF" \
    -type f \
    \( -name "*.SF" -o -name "*.RSA" -o -name "*.DSA" \) \
    -delete 2>/dev/null || true

# Create the fat JAR.
jar \
    --create \
    --file "$JAR_FILE" \
    --main-class "$MAIN_CLASS" \
    -C "$FAT_DIR" .

echo
echo "Build complete:"
echo "$JAR_FILE"