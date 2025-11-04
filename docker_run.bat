@echo off
setlocal enabledelayedexpansion

REM Default values
set PORT=8082
set CONTAINER_NAME=imovel-api-container

REM Parse command line arguments
if "%1"=="-p" set PORT=%2
if "%1"=="-n" set CONTAINER_NAME=%2

echo ========================================
echo    Starting Imovel API Docker Container
echo ========================================
echo Port: %PORT%
echo Container Name: %CONTAINER_NAME%
echo.

REM Build the image
echo Building Docker image...
docker build -t imovel-api .

REM Stop and remove if already running
echo Stopping existing container if running...
docker stop %CONTAINER_NAME% 2>nul
docker rm %CONTAINER_NAME% 2>nul

REM Run the container with env file
echo Starting new container...
docker run -d -p %PORT%:8082 --env-file docker.env --name %CONTAINER_NAME% imovel-api

REM Wait for startup
timeout /t 10 /nobreak >nul

REM Show logs
echo Container started. Showing logs:
echo.
docker logs %CONTAINER_NAME%

echo.
echo ========================================
echo    Application should be available at:
echo    http://localhost:%PORT%/imovel
echo    Swagger UI: http://localhost:%PORT%/imovel/swagger-ui.html
echo ========================================