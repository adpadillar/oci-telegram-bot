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
    # Kill all background processes
    kill $(jobs -p) 2>/dev/null
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
npm install
npm run dev &
FRONTEND_PID=$!
echo "Frontend server started with PID: $FRONTEND_PID"

# Start backend
echo "Starting Spring Boot backend..."
cd ../../..
$MVN_CMD spring-boot:run &
BACKEND_PID=$!
echo "Backend server started with PID: $BACKEND_PID"

# Function to handle Ctrl+C
handle_interrupt() {
    echo "Shutting down servers..."
    kill $FRONTEND_PID 2>/dev/null
    kill $BACKEND_PID 2>/dev/null
    cleanup
}

trap handle_interrupt INT

# Wait for either process to exit
wait -n $FRONTEND_PID $BACKEND_PID
# If we get here, one of the processes died, kill the other one
kill $FRONTEND_PID 2>/dev/null
kill $BACKEND_PID 2>/dev/null