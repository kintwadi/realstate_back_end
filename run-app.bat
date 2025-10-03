

REM Set up environment variables
call setup.bat

@echo off
echo Starting Imovel API with %SPRING_PROFILES_ACTIVE% profile...
echo.

echo.
echo Running application...
mvn spring-boot:run

echo.
echo Application finished with exit code: %ERRORLEVEL%
pause