@echo off
title Retail Inventory Pro - Simple Launcher
echo Starting Retail Inventory Pro...

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH.
    echo Please install Java 11 or higher from https://adoptium.net/
    pause
    exit /b 1
)

REM Create necessary directories
if not exist "data" mkdir data
if not exist "data/inventory" mkdir data/inventory
if not exist "data/orders" mkdir data/orders
if not exist "data/customers" mkdir data/customers
if not exist "data/users" mkdir data/users
if not exist "data/backups" mkdir data/backups
if not exist "data/reports" mkdir data/reports
if not exist "data/logs" mkdir data/logs

REM Compile and run
echo Compiling source files...
javac -cp ".;lib/*" -d out src/main/java/com/retailinventory/*.java src/main/java/com/retailinventory/**/*.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Starting application...
java -cp "out;lib/*" com.retailinventory.Main

pause