@echo off
title Retail Inventory Pro - Quick Start
echo ============================================
echo   Retail Inventory Pro - Starting Now
echo ============================================
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found!
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)

REM Create directories
if not exist "out" mkdir out
if not exist "data" mkdir data
if not exist "data\inventory" mkdir data\inventory
if not exist "data\orders" mkdir data\orders
if not exist "data\customers" mkdir data\customers
if not exist "data\users" mkdir data\users
if not exist "data\backups" mkdir data\backups
if not exist "data\reports" mkdir data\reports
if not exist "data\logs" mkdir data\logs

echo Compiling source files...
REM Compile all Java files
dir /s /b src\*.java > sources.txt 2>nul
if not exist sources.txt dir /s /b src\main\java\*.java > sources.txt 2>nul
if not exist sources.txt dir /s /b .\*.java > sources.txt 2>nul

if exist sources.txt (
    javac -d out @sources.txt
    del sources.txt
) else (
    echo ERROR: No Java source files found!
    echo Looking for Java files in common locations...
    dir /s /b *.java
    pause
    exit /b 1
)

if errorlevel 1 (
    echo.
    echo COMPILATION FAILED!
    echo Please check your Java files for errors.
    pause
    exit /b 1
)

echo.
echo ============================================
echo   COMPILATION SUCCESSFUL!
echo ============================================
echo.
echo Which version would you like to run?
echo.
echo   1. GUI Version (Recommended)
echo   2. Console Version
echo   3. Both (GUI first, then Console)
echo   4. Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto gui
if "%choice%"=="2" goto console
if "%choice%"=="3" goto both
if "%choice%"=="4" goto exit

:gui
echo.
echo Starting GUI version...
echo.
java -cp out com.retailinventory.Main
goto exit

:console
echo.
echo Starting Console version...
echo.
java -cp out com.retailinventory.console.ConsoleApp
goto exit

:both
echo.
echo Starting GUI version...
echo.
start "Retail Inventory Pro GUI" java -cp out com.retailinventory.Main
timeout /t 2 /nobreak >nul
echo.
echo Starting Console version in new window...
echo.
start "Retail Inventory Pro Console" cmd /k "java -cp out com.retailinventory.console.ConsoleApp"

:exit
echo.
echo Program execution completed.
pause