@echo off
echo Starting application debug...
echo Current directory: %CD%
echo Java version:
java -version
echo.
echo Maven version:
mvn -version
echo.
echo Starting Spring Boot application...
mvn spring-boot:run > startup-debug.log 2>&1
echo Application exited with code: %ERRORLEVEL%
echo.
echo Last 50 lines of log:
tail -n 50 startup-debug.log 2>nul || powershell "Get-Content startup-debug.log | Select-Object -Last 50"
pause