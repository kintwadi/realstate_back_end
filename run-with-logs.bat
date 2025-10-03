@echo off
echo Starting Imovel API with SQLite profile...
echo.

REM Set up environment variables
call setup.bat

echo.
echo Running application with logging...
mvn spring-boot:run > app-startup.log 2>&1

echo.
echo Application finished. Check app-startup.log for details.
echo Exit code: %ERRORLEVEL%
pause