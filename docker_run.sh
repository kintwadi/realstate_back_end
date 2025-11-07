#!/bin/bash

echo "========================================"
echo "   Starting Imovel API Docker Container"
echo "========================================"

# Default values
PORT=8082
PROFILE="default,docker"
CONTAINER_NAME="imovel-api-container"

# Parse command line arguments
while getopts "p:e:n:" opt; do
  case $opt in
    p) PORT="$OPTARG" ;;
    e) PROFILE="$OPTARG" ;;
    n) CONTAINER_NAME="$OPTARG" ;;
    *) echo "Usage: $0 [-p port] [-e profile] [-n container_name]" >&2
       exit 1 ;;
  esac
done

echo "Port: $PORT"
echo "Profile: $PROFILE"
echo "Container Name: $CONTAINER_NAME"
echo

# Build the image
echo "Building Docker image..."
docker build -t imovel-api .

# Stop and remove if already running
echo "Stopping existing container if running..."
docker stop $CONTAINER_NAME >/dev/null 2>&1
docker rm $CONTAINER_NAME >/dev/null 2>&1

# Run the container with environment variables
echo "Starting new container..."
docker run -d -p $PORT:8082 \
  -e SPRING_PROFILES_ACTIVE=$PROFILE \
  -e RDS_HOSTNAME="$RDS_HOSTNAME" \
  -e RDS_PORT="$RDS_PORT" \
  -e RDS_DB_NAME="$RDS_DB_NAME" \
  -e RDS_USERNAME="$RDS_USERNAME" \
  -e RDS_PASSWORD="$RDS_PASSWORD" \
  -e STRIPE_PUBLIC_KEY="$STRIPE_PUBLIC_KEY" \
  -e STRIPE_SECRET_KEY="$STRIPE_SECRET_KEY" \
  -e STRIPE_WEBHOOK_SECRET="$STRIPE_WEBHOOK_SECRET" \
  -e PAYPAL_CLIENT_ID="$PAYPAL_CLIENT_ID" \
  -e PAYPAL_CLIENT_SECRET="$PAYPAL_CLIENT_SECRET" \
  -e PAYPAL_WEBHOOK_ID="$PAYPAL_WEBHOOK_ID" \
  -e AWS_ACCESS_KEY="$AWS_ACCESS_KEY" \
  -e AWS_SECRET_KEY="$AWS_SECRET_KEY" \
  -e AWS_S3_REGION="$AWS_S3_REGION" \
  -e AWS_S3_BUCKET_NAME="$AWS_S3_BUCKET_NAME" \
  -e MAIL_HOST="$MAIL_HOST" \
  -e MAIL_PORT="$MAIL_PORT" \
  -e MAIL_USERNAME="$MAIL_USERNAME" \
  -e MAIL_PASSWORD="$MAIL_PASSWORD" \
  -e STORAGE_TYPE_PROVIDER="$STORAGE_TYPE_PROVIDER" \
  -e ENVIRONMENT="$ENVIRONMENT" \
  -e ACCESS_TOKEN_ALIAS="$ACCESS_TOKEN_ALIAS" \
  -e ACCESS_TOKEN_SECRET="$ACCESS_TOKEN_SECRET" \
  -e REFRESH_TOKEN_ALIAS="$REFRESH_TOKEN_ALIAS" \
  -e REFRESH_TOKEN_SECRET="$REFRESH_TOKEN_SECRET" \
  -e KEYSTORE_SECRET="$KEYSTORE_SECRET" \
  --name $CONTAINER_NAME \
  imovel-api

# Wait for startup
echo "Waiting for application to start..."
sleep 10

# Show logs
echo "Container started. Showing logs:"
echo
docker logs $CONTAINER_NAME

echo
echo "========================================"
echo "   Application should be available at:"
echo "   http://localhost:$PORT/imovel"
echo "   Swagger UI: http://localhost:$PORT/imovel/swagger-ui.html"
echo "========================================"