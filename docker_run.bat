@echo off
setlocal enabledelayedexpansion

REM Default values
set PORT=8082
set CONTAINER_NAME=imovel-api-container
set BUILD_IMAGE_NAME=kintwadi/imovel-api
set BUILD_IMAGE_TAG=v1.0.0
set PUSH_REPOSITORY=kintwadi/imovel_api_dev
set PUSH_TAG=latest

REM Parse command line arguments
:parse_args
if "%1"=="" goto end_parse
if "%1"=="-p" set PORT=%2& shift& shift& goto parse_args
if "%1"=="-n" set CONTAINER_NAME=%2& shift& shift& goto parse_args
if "%1"=="--build-name" set BUILD_IMAGE_NAME=%2& shift& shift& goto parse_args
if "%1"=="--build-tag" set BUILD_IMAGE_TAG=%2& shift& shift& goto parse_args
if "%1"=="--push-repo" set PUSH_REPOSITORY=%2& shift& shift& goto parse_args
if "%1"=="--push-tag" set PUSH_TAG=%2& shift& shift& goto parse_args
shift
goto parse_args
:end_parse

REM Construct image names
set BUILD_IMAGE_FULL=%BUILD_IMAGE_NAME%:%BUILD_IMAGE_TAG%
set PUSH_IMAGE_FULL=%PUSH_REPOSITORY%:%PUSH_TAG%

echo ========================================
echo    Docker Image Build and Push Script
echo ========================================
echo Build Image: %BUILD_IMAGE_FULL%
echo Push Repository: %PUSH_IMAGE_FULL%
echo Container Name: %CONTAINER_NAME%
echo Port: %PORT%
echo.

REM Build the image
echo Building Docker image...
docker build -t %BUILD_IMAGE_FULL% .

REM Tag the image for pushing to development repository
echo Tagging image for development repository...
docker tag %BUILD_IMAGE_FULL% %PUSH_IMAGE_FULL%

REM Push to development repository
echo Pushing to development repository...
docker push %PUSH_IMAGE_FULL%

echo.
echo ========================================
echo    Running Container Locally
echo ========================================

REM Stop and remove if already running
echo Stopping existing container if running...
docker stop %CONTAINER_NAME% 2>nul
docker rm %CONTAINER_NAME% 2>nul

REM Run the container with env file
echo Starting new container...
docker run -d -p %PORT%:8082 ^
  --env-file docker.env ^
  --name %CONTAINER_NAME% %BUILD_IMAGE_FULL%



REM Wait for startup
timeout /t 10 /nobreak >nul

REM Show logs
echo Container started. Showing logs:
echo.
docker logs %CONTAINER_NAME%

echo.
echo ========================================
echo    Deployment Summary
echo ========================================
echo Local Container: %CONTAINER_NAME%
echo Application: http://localhost:%PORT%/imovel
echo Swagger UI: http://localhost:%PORT%/imovel/swagger-ui.html
echo.
echo Built Image: %BUILD_IMAGE_FULL%
echo Pushed Image: %PUSH_IMAGE_FULL%
echo ========================================