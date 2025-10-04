# Environment Setup Guide

This project requires environment variables to be configured before running. The setup is automated through Maven, but you need to create the necessary configuration files first.

## Quick Setup

1. **Copy the template file:**
   ```bash
   cp private_file.template src/main/resources/private_file
   ```

2. **Edit the private_file with your actual values:**
   - Open `src/main/resources/private_file`
   - Replace all placeholder values with your actual configuration
   - **NEVER commit this file to git** (it's already in .gitignore)

3. **Run Maven build:**
   ```bash
   mvn compile
   ```
   The environment variables will be automatically set up during the build process.

## Environment Variables Required

### Database Configuration
- `RDS_HOSTNAME` - PostgreSQL database hostname
- `RDS_PORT` - Database port (usually 5432)
- `RDS_DB_NAME` - Database name
- `RDS_USERNAME` - Database username
- `RDS_PASSWORD` - Database password

### AWS Configuration
- `AWS_ACCESS_KEY` - AWS access key ID
- `AWS_SECRET_KEY` - AWS secret access key
- `AWS_S3_BUCKET_NAME` - S3 bucket name for file storage
- `AWS_S3_REGION` - AWS region

### Stripe Payment Configuration
- `STRIPE_PUBLIC_KEY` - Stripe publishable key
- `STRIPE_SECRET_KEY` - Stripe secret key
- `STRIPE_WEBHOOK_SECRET` - Stripe webhook endpoint secret

### Email Configuration
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password

### Security Configuration
- `_ACCESS_TOKEN_ALIAS` - JWT access token alias
- `_ACCESS_TOKEN_PASS` - JWT access token password
- `_REFRESH_TOKEN_ALIAS` - JWT refresh token alias
- `_REFRESH_TOKEN_PASS` - JWT refresh token password
- `_KEYSTORE_PASS` - Keystore password

### Application Configuration
- `SPRING_PROFILES_ACTIVE` - Active Spring profile (sqlite/postgresql)

## How It Works

The project uses Maven exec plugin to automatically run environment setup scripts:

- **Windows:** `setup.bat` is executed
- **Unix/Linux/Mac:** `setup.sh` is executed

These scripts are run during the Maven `initialize` phase, before compilation begins.

## Security Notes

⚠️ **IMPORTANT SECURITY INFORMATION:**

- The `private_file` contains sensitive credentials and is automatically ignored by git
- The setup scripts (`setup.bat`, `setup.sh`) are also ignored by git as they contain sensitive data
- Never commit files containing actual credentials to version control
- Use the `private_file.template` as a reference for required variables

## Troubleshooting

If environment variables are not being set:

1. Verify the `private_file` exists in `src/main/resources/`
2. Check that the file format matches the template exactly
3. Ensure Maven is running the setup scripts (you should see `[SETUP]` messages during build)
4. On Unix systems, make sure `setup.sh` has execute permissions: `chmod +x setup.sh`

## Manual Setup (Alternative)

If you prefer to set environment variables manually:

### Windows (PowerShell)
```powershell
$env:RDS_HOSTNAME="your_hostname"
$env:RDS_PORT="5432"
# ... set other variables
```

### Unix/Linux/Mac (Bash)
```bash
export RDS_HOSTNAME="your_hostname"
export RDS_PORT="5432"
# ... set other variables
```