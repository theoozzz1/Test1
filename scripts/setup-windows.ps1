#Requires -Version 5.1
<#
.SYNOPSIS
    Byte me - Windows Development Environment Setup Script
.DESCRIPTION
    This script installs all required dependencies for the Rescue Bites food waste
    reduction marketplace project on Windows.
.NOTES
    Run this script in PowerShell as Administrator
    Usage: .\setup-windows.ps1
#>

# CONFIGURATION
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

# Versions
$NODE_VERSION = "20"
$JAVA_VERSION = "17"

# Colors for output
function Write-Step { param($Message) Write-Host "`n[STEP] $Message" -ForegroundColor Cyan }
function Write-Success { param($Message) Write-Host "[OK] $Message" -ForegroundColor Green }
function Write-Warning { param($Message) Write-Host "[WARN] $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "[ERROR] $Message" -ForegroundColor Red }
function Write-Info { param($Message) Write-Host "[INFO] $Message" -ForegroundColor White }

# HELPER FUNCTIONS
function Test-Administrator {
    $currentUser = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($currentUser)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Test-Command {
    param($Command)
    $null = Get-Command $Command -ErrorAction SilentlyContinue
    return $?
}

function Install-Chocolatey {
    if (!(Test-Command "choco")) {
        Write-Step "Installing Chocolatey package manager..."
        Set-ExecutionPolicy Bypass -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
        Write-Success "Chocolatey installed successfully"
    } else {
        Write-Success "Chocolatey already installed"
    }
}

function Refresh-Path {
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
}


# Check for administrator privileges
if (!(Test-Administrator)) {
    Write-Error "This script requires Administrator privileges."
    Write-Info "Please right-click PowerShell and select 'Run as Administrator'"
    exit 1
}

Write-Success "Running with Administrator privileges"

# INSTALL CHOCOLATEY (Package Manager)
Install-Chocolatey

# INSTALL GIT
Write-Step "Checking Git..."
if (!(Test-Command "git")) {
    Write-Info "Installing Git..."
    choco install git -y --no-progress
    Refresh-Path
    Write-Success "Git installed successfully"
} else {
    $gitVersion = git --version
    Write-Success "Git already installed: $gitVersion"
}

# INSTALL NODE.JS
Write-Step "Checking Node.js..."
if (!(Test-Command "node")) {
    Write-Info "Installing Node.js v$NODE_VERSION..."
    choco install nodejs-lts -y --no-progress
    Refresh-Path
    Write-Success "Node.js installed successfully"
} else {
    $nodeVersion = node --version
    Write-Success "Node.js already installed: $nodeVersion"
}

# Verify npm
if (!(Test-Command "npm")) {
    Write-Error "npm not found. Please reinstall Node.js"
    exit 1
}
$npmVersion = npm --version
Write-Success "npm version: $npmVersion"

# INSTALL JAVA (OpenJDK)
Write-Step "Checking Java..."
$javaInstalled = $false
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion) {
        $javaInstalled = $true
        Write-Success "Java already installed: $javaVersion"
    }
} catch {
    $javaInstalled = $false
}

if (!$javaInstalled) {
    Write-Info "Installing OpenJDK $JAVA_VERSION..."
    choco install openjdk$JAVA_VERSION -y --no-progress
    Refresh-Path
    
    # Set JAVA_HOME
    $javaPath = (Get-ChildItem "C:\Program Files\OpenJDK\*" -Directory | Sort-Object Name -Descending | Select-Object -First 1).FullName
    if ($javaPath) {
        [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, "Machine")
        $env:JAVA_HOME = $javaPath
        Write-Success "JAVA_HOME set to: $javaPath"
    }
    Write-Success "Java installed successfully"
}

# INSTALL MAVEN
Write-Step "Checking Maven..."
if (!(Test-Command "mvn")) {
    Write-Info "Installing Maven..."
    choco install maven -y --no-progress
    Refresh-Path
    Write-Success "Maven installed successfully"
} else {
    $mvnVersion = mvn --version | Select-Object -First 1
    Write-Success "Maven already installed: $mvnVersion"
}

Write-Host "Installed Components:" -ForegroundColor Cyan
Write-Host "  - Git:        $(if (Test-Command 'git') { git --version } else { 'NOT INSTALLED' })"
Write-Host "  - Node.js:    $(if (Test-Command 'node') { node --version } else { 'NOT INSTALLED' })"
Write-Host "  - npm:        $(if (Test-Command 'npm') { npm --version } else { 'NOT INSTALLED' })"
Write-Host "  - Java:       $(if (Test-Command 'java') { 'Installed' } else { 'NOT INSTALLED' })"
Write-Host "  - Maven:      $(if (Test-Command 'mvn') { 'Installed' } else { 'NOT INSTALLED' })"
Write-Host "  - PostgreSQL: $(if (Test-Command 'psql') { psql --version } else { 'NOT INSTALLED' })"
Write-Host ""
Write-Host "                    NEXT STEPS                              " -ForegroundColor Magenta
Write-Host "1. Close and reopen your terminal to refresh PATH" -ForegroundColor Yellow
Write-Host ""
Write-Host "2. Configure your .env files with your settings" -ForegroundColor Yellow
Write-Host ""
Write-Host "3. Start the development servers:" -ForegroundColor Yellow
Write-Host "   Frontend:  cd frontend && npm run dev" -ForegroundColor White
Write-Host "   Backend:   cd backend && mvn spring-boot:run" -ForegroundColor White