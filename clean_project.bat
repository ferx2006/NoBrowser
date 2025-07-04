@echo off
echo Setting up Java environment...
set JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
set PATH=%JAVA_HOME%\bin;%PATH%

echo Java version:
java -version
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java is not properly set up!
    echo Please make sure Android Studio is installed at 'C:\Program Files\Android\Android Studio'
    pause
    exit /b 1
)

echo Closing any running Java processes...
taskkill /F /IM java.exe /T >nul 2>&1

echo Deleting build directories...
rd /s /q "%CD%\build" >nul 2>&1
rd /s /q "%CD%\app\build" >nul 2>&1

echo Cleaning project...
call gradlew clean --no-daemon

if %ERRORLEVEL% EQU 0 (
    echo Build directory cleaned successfully!
    echo You can now build the project using: gradlew assembleDebug
) else (
    echo Failed to clean build directory.
    echo Make sure Android Studio is closed and no Java processes are running.
)

pause
pause