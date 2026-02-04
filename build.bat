@echo off
echo ========================================
echo  ArrayList Visualizer - Build Script
echo ========================================
echo.

set SRC_DIR=src\main\java
set OUT_DIR=out
set MAIN_CLASS=game.Main

echo [1/3] Cleaning output directory...
if exist %OUT_DIR% rmdir /s /q %OUT_DIR%
mkdir %OUT_DIR%

echo [2/3] Compiling Java sources...
javac -d %OUT_DIR% -sourcepath %SRC_DIR% %SRC_DIR%\game\Main.java %SRC_DIR%\game\model\*.java %SRC_DIR%\game\ui\*.java

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [3/3] Compilation successful!
echo.
echo ========================================
echo  Run with: java -cp out game.Main
echo  Or use:   run.bat
echo ========================================
pause
