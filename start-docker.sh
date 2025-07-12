#!/bin/bash

echo "Starting Pay System with Docker Compose..."

# Set environment variables
export APP_PORT=8080
export MYSQL_ROOT_PASSWORD=rootpassword
export MYSQL_DATABASE=paysystem
export MYSQL_USER=paysystem
export MYSQL_PASSWORD=paysystem123
export REDIS_PASSWORD=redis123
export MONGO_ROOT_USER=admin
export MONGO_ROOT_PASSWORD=admin123

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Docker is not running. Please start Docker first."
    exit 1
fi

# Stop existing containers if any
echo "Stopping existing containers..."
docker-compose down

# Build and start services
echo "Building and starting services..."
docker-compose up -d --build

# Wait for services to be healthy
echo "Waiting for services to be ready..."
sleep 60

# Check service health
echo "Checking service health..."
docker-compose ps

echo ""
echo "Pay System is starting up!"
echo ""
echo "Services:"
echo "- MySQL: localhost:3306"
echo "- Redis: localhost:6379"
echo "- MongoDB: localhost:27017"
echo "- Elasticsearch: localhost:9200"
echo "- Kafka: localhost:9092"
echo "- Zookeeper: localhost:2181"
echo "- Pay System App: http://localhost:8080"
echo ""
echo "Health Check: http://localhost:8080/actuator/health"
echo ""
echo "To view logs: docker-compose logs -f"
echo "To stop services: docker-compose down"
echo "" 