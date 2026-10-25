# Prism Glass 0.11.0: switch only the isolated ATLauncher modtest MOD jar.
# -Restore reinstates the saved v0.10.0 JAR; saves/configs are never touched.
param([switch]$Restore)
$ErrorActionPreference = 'Stop'
$instance = Join-Path $env:APPDATA 'ATLauncher\instances\modtest'
$mods = Join-Path $instance 'mods'
$stage = 'D:\AI\exports\MinecraftMods\PrismGlass_0.11.0_20261025'
$old = Join-Path $mods 'prismglass-0.10.0.jar'
$new = Join-Path $mods 'prismglass-0.11.0.jar'
$oldReference = 'D:\AI\exports\MinecraftMods\PrismGlass_0.10.0_20261025\prismglass-0.10.0.jar'
$newReference = Join-Path $stage 'prismglass-0.11.0.jar'
$oldBackup = Join-Path $stage 'backup\prismglass-0.10.0.jar'
$doctor = Join-Path $mods 'prismdoctor-0.1.1.jar'
$doctorReference = 'D:\AI\exports\MinecraftMods\PrismDoctor_PrismGlass_mapping_fix_20261025\prismdoctor-0.1.1.jar'

function EqualFile($a, $b) {
    if (!(Test-Path -LiteralPath $a -PathType Leaf) -or
        !(Test-Path -LiteralPath $b -PathType Leaf)) { return $false }
    return (Get-FileHash -LiteralPath $a -Algorithm SHA256).Hash -eq
           (Get-FileHash -LiteralPath $b -Algorithm SHA256).Hash
}
foreach ($dir in @($instance, $mods, (Join-Path $instance 'saves'))) {
    if (!(Test-Path -LiteralPath $dir)) { throw "Expected test directory missing: $dir" }
    if ((Get-Item -LiteralPath $dir -Force).Attributes -band [IO.FileAttributes]::ReparsePoint) {
        throw 'Linked directory detected: refuse modification'
    }
}
$running = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
    Where-Object { $_.CommandLine -like "*$instance*" -or $_.CommandLine -match 'forge_userdev_client' })
if ($running.Count -ne 0) { throw 'Minecraft / Forge still running: close it normally first' }
if (!(EqualFile $doctor $doctorReference)) { throw 'Unexpected Prism Doctor JAR: stop' }
$jarFiles = @(Get-ChildItem -LiteralPath $mods -Filter '*.jar' -File)
if ($jarFiles.Count -ne 2) { throw "Unexpected mod count $($jarFiles.Count): stop" }

if ($Restore) {
    if (!(EqualFile $new $newReference)) { throw 'Unknown installed v0.11.0 JAR: no changes made' }
    if (!(EqualFile $oldBackup $oldReference)) { throw 'Original v0.10.0 backup invalid: no changes made' }
    if (Test-Path -LiteralPath $old) { throw 'v0.10.0 JAR already exists: abort' }
    Copy-Item -LiteralPath $oldBackup -Destination $old
    if (!(EqualFile $old $oldReference)) {
        Remove-Item -LiteralPath $old -ErrorAction SilentlyContinue
        throw 'Restored JAR verification failed'
    }
    Remove-Item -LiteralPath $new
    Write-Output 'RESTORE_OK: Prism Glass 0.10.0 restored. Worlds and Prism Doctor unchanged.'
    exit 0
}
if (!(EqualFile $newReference (Join-Path $PSScriptRoot 'build\libs\prismglass-0.11.0.jar'))) {
    throw 'Staged v0.11.0 JAR does not match verified development build'
}
if ((Test-Path -LiteralPath $new) -and !(Test-Path -LiteralPath $old) -and
    (EqualFile $new $newReference)) {
    Write-Output 'ALREADY_INSTALLED: Prism Glass 0.11.0 verified.'
    exit 0
}
if (!(EqualFile $old $oldReference) -or (Test-Path -LiteralPath $new)) {
    throw 'Old JAR does not match expected v0.10.0, or new JAR exists. No changes made.'
}
$backupDir = Split-Path -Parent $oldBackup
if (!(Test-Path -LiteralPath $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir | Out-Null
}
if (!(Test-Path -LiteralPath $oldBackup)) {
    Copy-Item -LiteralPath $old -Destination $oldBackup
}
if (!(EqualFile $oldBackup $old)) { throw 'Backup integrity failed before changes' }
try {
    Copy-Item -LiteralPath $newReference -Destination $new
    if (!(EqualFile $new $newReference)) { throw 'Installed 0.11.0 checksum mismatch' }
    Remove-Item -LiteralPath $old
} catch {
    Remove-Item -LiteralPath $new -ErrorAction SilentlyContinue
    if (!(Test-Path -LiteralPath $old)) {
        Copy-Item -LiteralPath $oldBackup -Destination $old
    }
    throw
}
Write-Output ('BACKUP='+$oldBackup)
Write-Output ('INSTALLED_SHA256='+(Get-FileHash -LiteralPath $new -Algorithm SHA256).Hash)
Write-Output 'INSTALL_OK: Prism Glass 0.11.0 in modtest; worlds / Prism Doctor untouched.'
