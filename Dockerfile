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
    apt-utils \
    nano \
    vim \
    unzip \
    default-jdk \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN useradd -m -s /bin/bash developer && \
    echo "developer ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# Switch to the non-root user
USER developer
WORKDIR /home/developer

# Install NVM and Node.js
ENV NVM_DIR=/home/developer/.nvm
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash && \
    . $NVM_DIR/nvm.sh && \
    nvm install 20 && \
    nvm alias default 20

# Add NVM to PATH and set default Node version
ENV NODE_VERSION=20
ENV PATH="/home/developer/.nvm/versions/node/v${NODE_VERSION}/bin:${PATH}"

# Clone the repository
RUN git clone https://github.com/adpadillar/oci-telegram-bot.git

# Copy wallet.zip if it exists in build context
COPY --chown=developer:developer wallet.zip /home/developer/oci-telegram-bot/wallet.zip

# Pre-download Maven dependencies and compile
WORKDIR /home/developer/oci-telegram-bot/MtdrSpring/backend
RUN mvn dependency:go-offline && \
    mvn clean install -DskipTests && \
    rm -rf target/

# Install frontend dependencies
WORKDIR /home/developer/oci-telegram-bot/MtdrSpring/backend/src/main/frontend
RUN . $NVM_DIR/nvm.sh && \
    npm install

# Copy setup script
WORKDIR /home/developer/oci-telegram-bot
COPY --chown=developer:developer setup.sh /home/developer/setup.sh
RUN chmod +x /home/developer/setup.sh

# Set the setup script to run when container starts
ENTRYPOINT ["/home/developer/setup.sh"] 