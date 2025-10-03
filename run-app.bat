@echo off
echo Starting Imovel API with SQLite profile...
echo.

REM Set up environment variables
call setup.bat

echo.
echo Running application...
mvn spring-boot:run

echo.
echo Application finished with exit code: %ERRORLEVEL%
pause