#!/bin/bash

echo "Pulling latest changes..."
git pull >> /dev/null 2>&1

echo "🚀 Development Environment Ready!"
echo ""

# Show installed versions
echo "📦 Installed Components:"
echo "⚡ Node $(node -v)"
echo "☕ $(java -version 2>&1 | head -n 1)"
echo "🏗️  Apache Maven $(mvn -v | head -n 1 | cut -d' ' -f3)"
echo ""

# Print instructions
echo "Next steps:

1️⃣  Create .env file:
   touch MtdrSpring/backend/.env
   nano MtdrSpring/backend/.env

2️⃣  Extract wallet:
   unzip wallet.zip
   
3️⃣  Start development:
   cd MtdrSpring/backend
   ./dev.sh

⚠️  Note: Make sure to properly configure your .env file before running dev.sh
"

# Return to project root
cd /home/developer/oci-telegram-bot

# Keep container running with bash
exec /bin/bash 