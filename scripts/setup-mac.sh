#!/bin/bash
# Byte Me - macOS Development Environment Setup Script
# This script installs all required dependencies for the Rescue Bites food waste
# reduction marketplace project on macOS.
#
# Usage: chmod +x setup-mac.sh && ./setup-mac.sh
set -e  # Exit on error

# CONFIGURATION
NODE_VERSION="20"
JAVA_VERSION="17"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# HELPER FUNCTIONS
print_step() {
    echo -e "\n${CYAN}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[OK]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_info() {
    echo -e "${NC}[INFO]${NC} $1"
}

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    print_error "This script is for macOS only. Use setup-linux.sh for Linux."
    exit 1
fi

print_success "Detected macOS $(sw_vers -productVersion)"

# INSTALL XCODE COMMAND LINE TOOLS
print_step "Checking Xcode Command Line Tools..."
if ! xcode-select -p &>/dev/null; then
    print_info "Installing Xcode Command Line Tools..."
    xcode-select --install
    echo "Please complete the Xcode installation and run this script again."
    exit 0
else
    print_success "Xcode Command Line Tools already installed"
fi

# INSTALL HOMEBREW
print_step "Checking Homebrew..."
if ! command_exists brew; then
    print_info "Installing Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    
    # Add Homebrew to PATH for Apple Silicon Macs
    if [[ $(uname -m) == "arm64" ]]; then
        echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
        eval "$(/opt/homebrew/bin/brew shellenv)"
    fi
    
    print_success "Homebrew installed successfully"
else
    print_success "Homebrew already installed: $(brew --version | head -n1)"
    print_info "Updating Homebrew..."
    brew update
fi

# INSTALL GIT
print_step "Checking Git..."
if ! command_exists git; then
    print_info "Installing Git..."
    brew install git
    print_success "Git installed successfully"
else
    print_success "Git already installed: $(git --version)"
fi

# INSTALL NODE.JS
print_step "Checking Node.js..."

# Check for nvm first 
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

if command_exists nvm; then
    print_info "nvm detected. Installing Node.js v$NODE_VERSION via nvm..."
    nvm install $NODE_VERSION
    nvm use $NODE_VERSION
    nvm alias default $NODE_VERSION
    print_success "Node.js installed via nvm: $(node --version)"
elif command_exists node; then
    NODE_CURRENT=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$NODE_CURRENT" -ge "$NODE_VERSION" ]; then
        print_success "Node.js already installed: $(node --version)"
    else
        print_warning "Node.js version is older than v$NODE_VERSION"
        print_info "Installing Node.js v$NODE_VERSION via Homebrew..."
        brew install node@$NODE_VERSION
        brew link node@$NODE_VERSION --force --overwrite
    fi
else
    print_info "Installing Node.js v$NODE_VERSION..."
    brew install node@$NODE_VERSION
    brew link node@$NODE_VERSION --force --overwrite
    print_success "Node.js installed successfully"
fi

# Verify npm
if command_exists npm; then
    print_success "npm version: $(npm --version)"
else
    print_error "npm not found. Please reinstall Node.js"
    exit 1
fi

# INSTALL JAVA (OpenJDK)
print_step "Checking Java..."
if ! command_exists java; then
    print_info "Installing OpenJDK $JAVA_VERSION..."
    brew install openjdk@$JAVA_VERSION
    
    # Create symlink for system Java wrappers
    sudo ln -sfn /opt/homebrew/opt/openjdk@$JAVA_VERSION/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-$JAVA_VERSION.jdk 2>/dev/null || \
    sudo ln -sfn /usr/local/opt/openjdk@$JAVA_VERSION/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-$JAVA_VERSION.jdk 2>/dev/null || true
    
    # Add to PATH
    if [[ $(uname -m) == "arm64" ]]; then
        JAVA_PATH="/opt/homebrew/opt/openjdk@$JAVA_VERSION"
    else
        JAVA_PATH="/usr/local/opt/openjdk@$JAVA_VERSION"
    fi
    
    echo "export PATH=\"$JAVA_PATH/bin:\$PATH\"" >> ~/.zshrc
    echo "export JAVA_HOME=\"$JAVA_PATH\"" >> ~/.zshrc
    export PATH="$JAVA_PATH/bin:$PATH"
    export JAVA_HOME="$JAVA_PATH"
    
    print_success "Java installed successfully"
else
    JAVA_VER=$(java -version 2>&1 | head -n1)
    print_success "Java already installed: $JAVA_VER"
fi

# INSTALL MAVEN
print_step "Checking Maven..."
if ! command_exists mvn; then
    print_info "Installing Maven..."
    brew install maven
    print_success "Maven installed successfully"
else
    print_success "Maven already installed: $(mvn --version | head -n1)"
fi

# VERIFICATION
echo -e "${CYAN}Installed Components:${NC}"
echo "  - Git:        $(git --version 2>/dev/null || echo 'NOT INSTALLED')"
echo "  - Node.js:    $(node --version 2>/dev/null || echo 'NOT INSTALLED')"
echo "  - npm:        $(npm --version 2>/dev/null || echo 'NOT INSTALLED')"
echo "  - Java:       $(java -version 2>&1 | head -n1 || echo 'NOT INSTALLED')"
echo "  - Maven:      $(mvn --version 2>/dev/null | head -n1 || echo 'NOT INSTALLED')"
echo ""
echo -e "${MAGENTA}                    NEXT STEPS                              ${NC}"
echo -e "${YELLOW}1. Restart your terminal or run:${NC}"
echo "   source ~/.zshrc"
echo ""
echo -e "${YELLOW}2. Configure your .env files with your settings${NC}"
echo ""
echo -e "${YELLOW}3. Start the development servers:${NC}"
echo "   Frontend:  cd frontend && npm run dev"
echo "   Backend:   cd backend && mvn spring-boot:run"