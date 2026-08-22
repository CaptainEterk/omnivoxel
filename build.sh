#!/usr/bin/env bash
set -e

SRC_DIR="src"
LIB_DIR="lib"
BUILD_DIR="build"
CLASS_DIR="$BUILD_DIR/classes"
TEMP_DIR="$BUILD_DIR/temp"
JAR_FILE="$BUILD_DIR/omnivoxel-client.jar"
MAIN_CLASS="omnivoxel.client.launcher.Launcher"

rm -rf "$BUILD_DIR"
mkdir -p "$CLASS_DIR"
mkdir -p "$TEMP_DIR"

# Build recursive compilation classpath.
CLASSPATH=$(find "$LIB_DIR" -type f -name "*.jar" -printf '%p:')
CLASSPATH="${CLASSPATH%:}"

# Find all Java files.
find "$SRC_DIR" -name "*.java" > "$BUILD_DIR/sources.txt"

# Compile project.
javac \
    -encoding UTF-8 \
    -cp "$CLASSPATH" \
    -d "$CLASS_DIR" \
    @"$BUILD_DIR/sources.txt"

# Copy compiled classes into temporary fat-JAR directory.
cp -r "$CLASS_DIR"/. "$TEMP_DIR"/

# Extract every dependency JAR recursively.
while IFS= read -r -d '' jar; do
    echo "Adding dependency: $jar"

    (
        cd "$TEMP_DIR"
        unzip -oq "../../$jar"
    )
done < <(find "$LIB_DIR" -type f -name "*.jar" -print0)

# Remove dependency signatures.
find "$TEMP_DIR/META-INF" \
    -type f \
    \( \
        -name "*.SF" \
        -o -name "*.DSA" \
        -o -name "*.RSA" \
    \) \
    -delete 2>/dev/null || true

# Create fat JAR.
jar \
    --create \
    --file "$JAR_FILE" \
    --main-class "$MAIN_CLASS" \
    -C "$TEMP_DIR" .

echo "Build complete:"
echo "$JAR_FILE"