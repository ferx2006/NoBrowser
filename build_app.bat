@echo off
setlocal

:: Set Java Home
echo Setting JAVA_HOME...
set JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"

:: Add Java to PATH
echo Updating PATH...
set PATH=%JAVA_HOME%\bin;%PATH%

:: Clean and build
echo Starting clean build...
call gradlew clean build --stacktrace

if %ERRORLEVEL% EQU 0 (
    echo Build completed successfully!
    echo You can find the APK in: app\build\outputs\apk\debug\
) else (
    echo Build failed with error code %ERRORLEVEL%
)

pause
