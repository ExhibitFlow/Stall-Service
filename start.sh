#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Start the Stall Service
echo "Starting Stall Service on port 8081..."
echo "Identity Service URL: $IDENTITY_SERVICE_URL"
echo "JWT Secret configured: ${JWT_SECRET:0:20}..."

# Run Maven Spring Boot
./mvnw spring-boot:run

