# oci-react-samples

## MOMO
Ok lol me ovy a matar saludos desde venezuela

A repository for full stack Cloud Native applications with a React JS frontend and various backends (Java, Python, DotNet, and so on) on the Oracle Cloud Infrastructure.

## Quick Start

1. Create `.env` file in `MtdrSpring/backend/`:

```bash
# Database configuration
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@your_db_name_high?TNS_ADMIN=/path/to/wallet
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password

# Telegram bot configuration (optional)
TELEGRAM_BOT_NAME=your_bot_name
TELEGRAM_BOT_TOKEN=your_bot_token

# Server configuration
SERVER_PORT=8081
```

2. Run the application:

```bash
cd MtdrSpring/backend
chmod +x dev.sh  # Make script executable (first time only)
./dev.sh
```

The application will start on port 8081.

## Project Structure and Requirements

### Requirements

The lab executes scripts that require the following software to run properly: (These are already installed on and included with the OCI Cloud Shell)

- oci-cli
- python 2.7^
- terraform
- kubectl
- mvn (maven)

## Expect more ...
