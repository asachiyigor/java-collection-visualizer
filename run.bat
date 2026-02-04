@echo off
echo ========================================
echo   Java Collection Visualizer
echo ========================================
echo.

:: Check if compiled
if not exist "out\game\Main.class" (
    echo [INFO] Compiling...
    if not exist out mkdir out
    javac -d out -sourcepath src\main\java src\main\java\game\Main.java src\main\java\game\model\*.java src\main\java\game\ui\*.java
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Compilation failed!
        pause
        exit /b 1
    )
)

echo [INFO] Starting application...
java -cp out game.Main
