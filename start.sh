#!/bin/bash

# Todoist Scheduler Bot (Kotlin + Spring Boot) - Startup Script
# This script sets up the environment and starts the bot

set -e  # Exit on any error

echo "🚀 Starting Todoist Scheduler Bot (Kotlin + Spring Boot)..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17+ first."
    echo "   macOS: brew install openjdk@17"
    echo "   Ubuntu: sudo apt-get install openjdk-17-jdk"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17+ is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"

# Make gradlew executable
chmod +x ./gradlew

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "⚠️  .env file not found. Copying from env_example.txt..."
    if [ -f "../env_example.txt" ]; then
        cp ../env_example.txt .env
        echo "✅ Created .env file from env_example.txt"
        echo "⚠️  Please edit .env file with your tokens before running the bot!"
        exit 1
    else
        echo "❌ Neither .env nor env_example.txt found!"
        exit 1
    fi
fi

# Validate that required environment variables are set
echo "🔍 Validating configuration..."

REQUIRED_VARS=("TELEGRAM_BOT_TOKEN" "TODOIST_API_TOKEN")
MISSING_VARS=()

for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        MISSING_VARS+=("$var")
    fi
done

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    echo "❌ Missing required environment variables:"
    printf '   - %s\n' "${MISSING_VARS[@]}"
    echo "   Please set them in your .env file"
    exit 1
fi

echo "✅ Configuration validated"

# Clean and build the project
echo "🔨 Building the project..."
./gradlew clean build --no-daemon --quiet

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful"

# Start the bot
echo "🤖 Starting the bot..."
echo "   Press Ctrl+C to stop"
echo ""

./gradlew bootRun --no-daemon
