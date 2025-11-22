#!/bin/bash

# Quick start script for Stall Service

set -e

echo "🚀 Starting Stall Service with Docker Compose..."
echo ""

# Check if .env exists, if not copy from template
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp .env.template .env
    echo "✅ .env file created. You can edit it to customize configuration."
    echo ""
fi

# Build and start all services
echo "🏗️  Building and starting all services..."
docker-compose up -d --build

echo ""
echo "⏳ Waiting for services to be healthy..."
sleep 10

# Check service status
echo ""
echo "📊 Service Status:"
docker-compose ps

echo ""
echo "✅ Services are starting up!"
echo ""
echo "🌐 Available endpoints:"
echo "   - Stall Service API:    http://localhost:8081"
echo "   - Swagger UI:           http://localhost:8081/swagger-ui.html"
echo "   - API Docs:             http://localhost:8081/api-docs"
echo "   - Health Check:         http://localhost:8081/actuator/health"
echo "   - Keycloak Admin:       http://localhost:8080 (admin/admin)"
echo ""
echo "📊 View logs with:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Stop services with:"
echo "   docker-compose down"
echo ""
echo "💡 Check DOCKER.md for more information and troubleshooting tips."
