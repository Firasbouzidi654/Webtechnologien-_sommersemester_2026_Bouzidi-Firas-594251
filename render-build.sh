#!/bin/bash
# render-build.sh - Build script for Render deployment

echo "=========================================="
echo "KinderCareConnect - Build Script"
echo "=========================================="

# Set Java version
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk

echo "Java Version:"
java -version

echo ""
echo "Building Backend..."
cd backend

# Clean build
./gradlew clean build --stacktrace --no-daemon

if [ $? -ne 0 ]; then
    echo "❌ Backend build FAILED"
    exit 1
fi

echo "✅ Backend build SUCCESS"
echo ""
echo "Build artifacts located at:"
ls -lh build/libs/

echo ""
echo "=========================================="
echo "Build Complete!"
echo "=========================================="

