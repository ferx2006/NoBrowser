@echo off
setlocal enabledelayedexpansion

:: Configurar rutas
set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
set JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
set PATH=%JAVA_HOME%\bin;%PATH%

echo Configurando entorno de compilación...

:: Limpiar proyecto
call gradlew clean

:: Crear APK de depuración
call gradlew assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ¡Compilación exitosa!
    echo El APK se encuentra en: app\build\outputs\apk\debug\
    echo.
    pause
) else (
    echo.
    echo Error durante la compilación.
    echo.
    pause
    exit /b 1
)
