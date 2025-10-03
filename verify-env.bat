@echo off
REM Verification script to check if environment variables are set
echo [VERIFY] Checking environment variables...
echo.

echo Database Configuration:
echo   RDS_HOSTNAME=%RDS_HOSTNAME%
echo   RDS_PORT=%RDS_PORT%
echo   RDS_DB_NAME=%RDS_DB_NAME%
echo   RDS_USERNAME=%RDS_USERNAME%
echo.

echo AWS Configuration:
echo   AWS_ACCESS_KEY=%AWS_ACCESS_KEY%
echo   AWS_S3_BUCKET_NAME=%AWS_S3_BUCKET_NAME%
echo   AWS_S3_REGION=%AWS_S3_REGION%
echo.

echo Spring Configuration:
echo   SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%
echo.

echo Mail Configuration:
echo   MAIL_USERNAME=%MAIL_USERNAME%
echo.

echo Stripe Configuration:
echo   STRIPE_PUBLIC_KEY=%STRIPE_PUBLIC_KEY%
echo.

echo [VERIFY] Environment verification complete!