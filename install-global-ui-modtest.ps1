# Install or restore Prism Glass v0.10.0 in ATLauncher modtest only.
# Never touches saves, options, other instances, or running game processes.
param([switch]$Restore)
$ErrorActionPreference = 'Stop'
$instance = Join-Path $env:APPDATA 'ATLauncher\instances\modtest'
$mods = Join-Path $instance 'mods'
$stage = 'D:\AI\exports\MinecraftMods\PrismGlass_0.10.0_20261025'
$newSource = Join-Path $stage 'prismglass-0.10.0.jar'
$original = Join-Path $mods 'prismglass-0.9.0.jar'
$installed = Join-Path $mods 'prismglass-0.10.0.jar'
$backup = Join-Path $stage 'backup\prismglass-0.9.0.jar'
$expectedOriginal = 'D:\AI\exports\MinecraftMods\PrismDoctor_PrismGlass_mapping_fix_20261025\prismglass-0.9.0.jar'
$doctor = Join-Path $mods 'prismdoctor-0.1.1.jar'

function SameHash($left, $right) {
    if (!(Test-Path -LiteralPath $left -PathType Leaf) -or
        !(Test-Path -LiteralPath $right -PathType Leaf)) { return $false }
    (Get-FileHash -LiteralPath $left -Algorithm SHA256).Hash -eq
        (Get-FileHash -LiteralPath $right -Algorithm SHA256).Hash
}

foreach ($dir in @($instance,$mods,(Join-Path $instance 'saves'))) {
    if (Test-Path -LiteralPath $dir) {
        if ((Get-Item -LiteralPath $dir -Force).Attributes -band [IO.FileAttributes]::ReparsePoint) {
            throw "Linked directory is not allowed: $dir"
        }
    }
}
if (!(Test-Path -LiteralPath $doctor -PathType Leaf)) {
    throw 'Expected Prism Doctor missing; unexpected modtest state'
}
$running = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
    Where-Object { $_.CommandLine -like "*$instance*" -or $_.CommandLine -match 'forge_userdev_client' })
if ($running.Count -gt 0) {
    throw 'Minecraft/Forge is running. Close modtest normally, then run this script again.'
}
if ($Restore) {
    if (!(SameHash $installed $newSource) -or !(Test-Path -LiteralPath $backup)) {
        throw 'Unexpected installed or backup file; refusing to restore blindly'
    }
    if (Test-Path -LiteralPath $original) { throw 'Old JAR already present: refuse duplicate mods' }
    Copy-Item -LiteralPath $backup -Destination $original
    if (!(SameHash $original $backup)) {
        Remove-Item -LiteralPath $original -ErrorAction SilentlyContinue
        throw 'Restored file checksum mismatch'
    }
    Remove-Item -LiteralPath $installed
    Write-Output 'RESTORE_OK: Prism Glass 0.9.0 restored; no saves changed.'
    exit 0
}

if (!(Test-Path -LiteralPath $newSource -PathType Leaf)) {
    throw 'v0.10.0 staging JAR missing; do not install'
}
if ((Test-Path -LiteralPath $installed) -and !(Test-Path -LiteralPath $original) -and
    (SameHash $installed $newSource)) {
    Write-Output 'ALREADY_INSTALLED: v0.10.0 present and verified.'
    exit 0
}
if (!(SameHash $original $expectedOriginal) -or (Test-Path -LiteralPath $installed)) {
    throw 'Existing mod files are different from the verified v0.9.0 baseline; no changes made'
}
$activeMods = @(Get-ChildItem -LiteralPath $mods -Filter '*.jar' -File)
if ($activeMods.Count -ne 2) { throw 'Unexpected enabled mods; refusing to alter installation' }
$backupDir = Split-Path $backup -Parent
if (!(Test-Path -LiteralPath $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir | Out-Null
}
if (!(Test-Path -LiteralPath $backup)) {
    Copy-Item -LiteralPath $original -Destination $backup
}
if (!(SameHash $original $backup)) { throw 'Baseline backup hash mismatch; no mods changed' }
try {
    Copy-Item -LiteralPath $newSource -Destination $installed
    if (!(SameHash $newSource $installed)) { throw 'New JAR checksum mismatch' }
    Remove-Item -LiteralPath $original
} catch {
    Remove-Item -LiteralPath $installed -ErrorAction SilentlyContinue
    if (!(Test-Path -LiteralPath $original)) {
        Copy-Item -LiteralPath $backup -Destination $original
    }
    throw
}
Write-Output ('OLD_BACKUP='+$backup)
Write-Output ('NEW_JAR_SHA256='+(Get-FileHash -LiteralPath $installed -Algorithm SHA256).Hash)
Write-Output 'INSTALL_OK: only modtest Prism Glass replaced; worlds untouched.'
