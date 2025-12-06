@echo off
title Retail Inventory Pro - Simple Compiler
echo =============================================
echo  Retail Inventory Pro - Compilation Script
echo =============================================
echo.

REM Check Java installation
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH.
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)

echo [1/5] Creating directory structure...
if not exist "out" mkdir out
if not exist "data" mkdir data
if not exist "data\inventory" mkdir data\inventory
if not exist "data\orders" mkdir data\orders
if not exist "data\customers" mkdir data\customers
if not exist "data\users" mkdir data\users
if not exist "data\backups" mkdir data\backups
if not exist "data\reports" mkdir data\reports
if not exist "data\logs" mkdir data\logs

echo [2/5] Finding all Java source files...
dir /s /b src\main\java\*.java > sources.txt

echo [3/5] Compiling Java files...
javac -d out -cp "lib\*" @sources.txt

if errorlevel 1 (
    echo.
    echo COMPILATION FAILED!
    echo.
    del sources.txt
    pause
    exit /b 1
)

echo [4/5] Copying resources...
if exist "src\main\resources" (
    xcopy /E /I "src\main\resources" "out" >nul
)

echo [5/5] Cleaning up...
del sources.txt

echo.
echo =============================================
echo  COMPILATION SUCCESSFUL!
echo =============================================
echo.
echo To run the application:
echo.
echo   GUI Version:    java -cp "out;lib\*" com.retailinventory.Main
echo   Console Version: java -cp "out;lib\*" com.retailinventory.console.ConsoleApp
echo.
echo Or use the provided batch files:
echo   run_gui_simple.bat
echo   run_console_simple.bat
echo.
pause