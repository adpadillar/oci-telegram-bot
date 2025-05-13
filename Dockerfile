# Use Ubuntu as the base image
FROM ubuntu:22.04

# Avoid prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install git and basic utilities
RUN apt-get update && apt-get install -y \
    git \
    curl \
    wget \
    sudo \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN useradd -m -s /bin/bash developer && \
    echo "developer ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# Switch to the non-root user
USER developer
WORKDIR /home/developer

# Clone the repository
RUN git clone https://github.com/adpadillar/oci-telegram-bot.git

# Copy wallet.zip if it exists in build context
COPY --chown=developer:developer wallet.zip /home/developer/oci-telegram-bot/wallet.zip

# Set working directory to the project
WORKDIR /home/developer/oci-telegram-bot

# Add a helpful message about required dependencies
RUN echo '#!/bin/bash\n\
# Update package list and install editors\n\
echo "Updating package list..."\n\
sudo apt-get update >> /dev/null\n\
echo "Installing nano, unzip, and neofetch..."\n\
sudo apt-get install -y nano unzip neofetch >> /dev/null\n\
echo "Installing Node.js and npm..."\n\
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash - >> /dev/null\n\
sudo apt-get install -y nodejs npm >> /dev/null\n\
echo "Installing Java JDK..."\n\
sudo apt-get install -y default-jdk >> /dev/null\n\
echo "Installing Maven..."\n\
sudo apt-get install -y maven >> /dev/null\n\
echo "Welcome to the OCI Telegram Bot development environment!\n\
\n\
Required dependencies that need to be installed:\n\
- Node.js and npm: sudo apt install nodejs npm\n\
- Java JDK: sudo apt install default-jdk\n\
- Maven: sudo apt install maven\n\
\n\
After installing dependencies:\n\
1. Create .env file in MtdrSpring/backend/\n\
2. Run ./dev.sh in MtdrSpring/backend/\n\
"\n\
exec /bin/bash' > /home/developer/welcome.sh && \
    chmod +x /home/developer/welcome.sh

# Set the welcome script to run when container starts
ENTRYPOINT ["/home/developer/welcome.sh"] 