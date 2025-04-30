#!/bin/bash

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Error: .env file not found!"
    echo "Please create a .env file with your environment variables."
    exit 1
fi

echo "Loading environment variables from .env file..."

# Export environment variables from .env file
export $(grep -v '^#' .env | xargs)

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