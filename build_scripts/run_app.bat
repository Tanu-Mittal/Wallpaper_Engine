@echo off
REM Jump to the project root (one level up from build_scripts)
cd /d "%~dp0.."

REM Move into the Java app directory
cd java_app

REM Compile the Java GUI
javac MainWindow.java

REM Run the Java GUI
java MainWindow

pause
