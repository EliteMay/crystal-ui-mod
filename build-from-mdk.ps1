param(
    [Parameter(Mandatory=$true)]
    [string]$MdkZip
)
$ErrorActionPreference = 'Stop'
$zipPath = (Resolve-Path $MdkZip).Path
$sourceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$workspace = Join-Path $sourceRoot 'forge-workspace'
if (Test-Path $workspace) {
    throw "Output already exists: $workspace. Backup or choose a fresh source folder; no overwrite performed."
}
if ($zipPath -notmatch 'forge-1\.21\.1-52\.1\.0-mdk\.zip$') {
    throw 'Incorrect MDK filename. Use the official Forge 1.21.1-52.1.0 MDK ZIP.'
}
$expectedSha1 = 'A081DA53578F1BD9B053CF7D65DBE9E95D2B351C'
$actualSha1 = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA1).Hash
if ($actualSha1 -ne $expectedSha1) {
    throw "Official MDK SHA1 mismatch. Expected $expectedSha1, got $actualSha1. Download from Forge official site again."
}
New-Item -ItemType Directory -Path $workspace | Out-Null
try {
    Expand-Archive -LiteralPath $zipPath -DestinationPath $workspace -Force
    Remove-Item -LiteralPath (Join-Path $workspace 'src') -Recurse -Force
    Copy-Item -LiteralPath (Join-Path $sourceRoot 'src') -Destination $workspace -Recurse -Force
    foreach ($file in @('build.gradle', 'gradle.properties', 'settings.gradle')) {
        Copy-Item -LiteralPath (Join-Path $sourceRoot $file) -Destination $workspace -Force
    }
    $launcher = Join-Path $workspace 'gradlew.bat'
    if (-not (Test-Path $launcher)) { throw 'Official MDK gradlew.bat is missing.' }
    Push-Location $workspace
    try {
        & .\gradlew.bat --no-daemon build
        if ($LASTEXITCODE -ne 0) { throw "Forge Gradle build failed: exit $LASTEXITCODE" }
        Write-Host "SUCCESS - JAR: $workspace\build\libs\prismglass-0.1.0.jar"
    } finally { Pop-Location }
} catch {
    Write-Error $_
    Write-Host "Source and logs remain at: $workspace"
    exit 1
}
