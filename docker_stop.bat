@echo off
echo ========================================
echo    Stopping Imovel API Docker Container
echo ========================================

echo Stopping container...
docker stop imovel-api-container

echo Removing container...
docker rm imovel-api-container

echo.
echo Container stopped and removed successfully!
pause