@echo off
title Retail Inventory Pro - Console Version
echo Starting Retail Inventory Pro Console Version...
echo.

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH.
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)

REM Set Java options
set JAVA_OPTS=-Xmx512m -Dfile.encoding=UTF-8

REM Check for JAR file
if not exist "build\libs\RetailInventoryPro-2.0.0.jar" (
    echo Error: JAR file not found. Building project first...
    call gradlew build
    if %errorlevel% neq 0 (
        echo Error: Build failed.
        pause
        exit /b 1
    )
)

REM Run the application
echo Starting application...
java %JAVA_OPTS% -cp "build\libs\RetailInventoryPro-2.0.0.jar;lib\*" com.retailinventory.console.ConsoleApp

echo.
echo Application terminated.
pause