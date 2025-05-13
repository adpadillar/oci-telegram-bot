#!/bin/bash

# Function to check if a command exists
check_dependency() {
    local cmd=$1
    local package=$2
    local install_instructions=$3
    
    if ! command -v "$cmd" &> /dev/null; then
        echo "Error: $cmd is not installed!"
        echo "To install $package on Ubuntu/Debian:"
        echo "$install_instructions"
        echo ""
        return 1
    fi
    return 0
}

# Check all required dependencies
echo "Checking dependencies..."

# Check Java
if ! check_dependency java "OpenJDK" "sudo apt update && sudo apt install default-jdk"; then
    exit 1
fi

# Check Node.js and npm
if ! check_dependency node "Node.js" "curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash - && sudo apt install nodejs"; then
    exit 1
fi

if ! check_dependency npm "npm" "npm is usually installed with Node.js. If not, run: sudo apt install npm"; then
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed system-wide, but we can use the Maven wrapper (./mvnw)."
    if [ ! -f "./mvnw" ]; then
        echo "Error: Neither Maven nor Maven Wrapper (./mvnw) found!"
        echo "To install Maven on Ubuntu/Debian:"
        echo "sudo apt update && sudo apt install maven"
        echo "Or make sure the Maven Wrapper (mvnw) is present in your project root."
        exit 1
    fi
fi

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Error: .env file not found!"
    echo "Please create a .env file with your environment variables."
    exit 1
fi

echo "Loading environment variables from .env file..."

# Export environment variables from .env file
export $(grep -v '^#' .env | xargs)

# Check if SPRING_DATASOURCE_URL is set and extract TNS_ADMIN path
if [[ -n "$SPRING_DATASOURCE_URL" ]]; then
    # Extract the TNS_ADMIN path using regex
    if [[ $SPRING_DATASOURCE_URL =~ TNS_ADMIN=([^[:space:]]+) ]]; then
        WALLET_PATH="${BASH_REMATCH[1]}"
        echo "Checking Oracle wallet directory at: $WALLET_PATH"
        if [ ! -d "$WALLET_PATH" ]; then
            echo "Error: Oracle wallet directory not found!"
            echo "Please ensure your wallet directory exists at: $WALLET_PATH"
            echo "You need to download and extract your Oracle Cloud wallet files to this location."
            exit 1
        else
            echo "Oracle wallet directory found."
        fi
    else
        echo "Warning: Could not extract TNS_ADMIN path from SPRING_DATASOURCE_URL"
    fi
fi

# Check Telegram bot credentials
echo "Checking Telegram bot credentials..."
if [[ -z "$TELEGRAM_BOT_NAME" || -z "${TELEGRAM_BOT_NAME// }" ]]; then
    echo "Error: TELEGRAM_BOT_NAME is not set or is empty!"
    echo "Please follow these steps to create a Telegram bot:"
    echo "1. Open Telegram and search for @BotFather"
    echo "2. Start a chat and send /newbot command"
    echo "3. Follow the instructions to create your bot"
    echo "4. Add the bot's username to your .env file as TELEGRAM_BOT_NAME"
    echo "5. Add the bot's token to your .env file as TELEGRAM_BOT_TOKEN"
    exit 1
fi

if [[ -z "$TELEGRAM_BOT_TOKEN" || -z "${TELEGRAM_BOT_TOKEN// }" ]]; then
    echo "Error: TELEGRAM_BOT_TOKEN is not set or is empty!"
    echo "Please follow these steps to get your bot token:"
    echo "1. Open Telegram and search for @BotFather"
    echo "2. If you already created a bot, send /mybots to see your bots"
    echo "3. Select your bot and click 'API Token' to get the token"
    echo "4. Add the token to your .env file as TELEGRAM_BOT_TOKEN"
    exit 1
fi

echo "Telegram bot credentials found."

# Function to cleanup before exit
cleanup() {
    echo "Cleaning up..."
    # Unset environment variables
    for var in $(grep -v '^#' .env | sed -E 's/([^=]+)=.*/\1/'); do
        unset "$var"
    done
    # Kill processes if they're still running
    if [[ ! -z "${FRONTEND_PID}" ]]; then
        echo "Stopping frontend process..."
        kill -TERM $FRONTEND_PID 2>/dev/null || true
    fi
    if [[ ! -z "${BACKEND_PID}" ]]; then
        echo "Stopping backend process..."
        kill -TERM $BACKEND_PID 2>/dev/null || true
    fi
    exit
}

# Set up trap for cleanup
trap cleanup EXIT INT TERM

# Determine which Maven command to use
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
    echo "Using installed Maven."
else
    MVN_CMD="./mvnw"
    echo "Maven not found, using ./mvnw."
fi

# Start frontend in background
echo "Starting Vite development server..."
cd src/main/frontend
if ! npm install; then
    echo "Failed to install frontend dependencies"
    cleanup
    exit 1
fi

npm run dev &
FRONTEND_PID=$!
echo "Frontend server started with PID: $FRONTEND_PID"

# Check if frontend started successfully
sleep 2
if ! kill -0 $FRONTEND_PID 2>/dev/null; then
    echo "Frontend failed to start"
    cleanup
    exit 1
fi

# Start backend
echo "Starting Spring Boot backend..."
cd ../../..
$MVN_CMD spring-boot:run &
BACKEND_PID=$!
echo "Backend server started with PID: $BACKEND_PID"

# Check if backend started successfully
sleep 5
if ! kill -0 $BACKEND_PID 2>/dev/null; then
    echo "Backend failed to start"
    cleanup
    exit 1
fi

# Function to handle Ctrl+C
handle_interrupt() {
    echo "Received interrupt signal..."
    cleanup
}

trap handle_interrupt INT

# Monitor both processes
while kill -0 $FRONTEND_PID 2>/dev/null && kill -0 $BACKEND_PID 2>/dev/null; do
    sleep 1
done

# If we get here, one of the processes died
echo "One of the processes has terminated unexpectedly"
cleanup