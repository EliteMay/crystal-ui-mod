# Non-destructive Prism Glass iteration runner.
# This script NEVER removes saved worlds, kills Minecraft processes or sends keyboard input.
param(
    [string]$Tag = 'manual',
    [switch]$RunClient
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
if (!(Test-Path -LiteralPath (Join-Path $root 'gradlew.bat'))) {
    throw 'Gradle wrapper not found at script location'
}
if ($Tag -notmatch '^[A-Za-z0-9_-]{1,48}$') {
    throw 'Tag must be 1 to 48 letters, digits, underscores or hyphens'
}

$jdk = 'D:\AI\tools\runtimes\prismglass-jdk21\jdk-21.0.12.1+1'
if (!(Test-Path -LiteralPath (Join-Path $jdk 'bin\java.exe'))) {
    throw 'Expected Java 21 runtime not found'
}
$env:JAVA_HOME = $jdk
$env:PATH = (Join-Path $jdk 'bin') + ';' + $env:PATH
$env:GRADLE_USER_HOME = 'D:\AI\projects\workspaces\prismglass-build\.gradle-cache'

Push-Location $root
try {
    Write-Host "[PrismGlass] ${Tag}: non-destructive build and tests"
    & .\gradlew.bat --no-daemon --console=plain build
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed: exit code $LASTEXITCODE"
    }

    if ($RunClient) {
        # Never terminate running Java/Minecraft processes. User must close clients manually.
        $running = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
            Where-Object { $_.CommandLine -match 'forge_userdev_client' -and
                            $_.CommandLine -like "*$root*" })
        if ($running.Count -gt 0) {
            throw 'A Prism Glass dev client is already running. Close it manually before another launch.'
        }
        Write-Host '[PrismGlass] Starting Forge dev client. Existing saves are preserved.'
        & .\gradlew.bat --no-daemon --console=plain runClient
        if ($LASTEXITCODE -ne 0) {
            throw "Forge dev client exited with code $LASTEXITCODE"
        }
    }
    Write-Host '[PrismGlass] Validation completed. No worlds, saves or sessions were deleted.'
} finally {
    Pop-Location
}
