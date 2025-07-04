# PowerShell script to build the Android app

# Set Java home - trying common Android Studio locations
$javaPaths = @(
    "C:\Program Files\Android\Android Studio\jbr",
    "${env:LOCALAPPDATA}\Android\Sdk\jbr",
    "${env:ProgramFiles}\Android\Android Studio\jbr"
)

foreach ($path in $javaPaths) {
    if (Test-Path "$path\bin\java.exe") {
        $env:JAVA_HOME = $path
        $env:Path = "$path\bin;" + $env:Path
        Write-Host "Java found at: $path"
        break
    }
}

if (-not $env:JAVA_HOME) {
    Write-Host "Java not found in standard locations. Please set JAVA_HOME manually."
    exit 1
}

# Verify Java
Write-Host "Java version:"
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Java is not working properly!"
    exit 1
}

# Build the app
Write-Host "Building the app..."
.\gradlew assembleDebug --stacktrace

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful! APK should be in: app\build\outputs\apk\debug\"
} else {
    Write-Host "Build failed with error code: $LASTEXITCODE"
}

# Keep the window open to see the output
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
