param([string]$Tag='it')
$ErrorActionPreference='Continue'
Set-Location 'D:\AI\projects\workspaces\prismglass-v0.9'
Get-CimInstance Win32_Process | Where-Object { $_.Name -match 'java' -and $_.CommandLine -match 'forge_userdev_client' -and $_.CommandLine -match 'prismglass-v0\.9' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
Start-Sleep 3
powershell -NoProfile -ExecutionPolicy Bypass -File local-build.ps1 *>&1 | Select-String 'BUILD'
Remove-Item run\saves -Recurse -Force -ErrorAction SilentlyContinue
Start-Process powershell -ArgumentList '-NoProfile','-ExecutionPolicy','Bypass','-File','smoke-run.ps1' -WindowStyle Hidden
for($i=0;$i -lt 40;$i++){ Start-Sleep 5; $p=Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -like '*Singleplayer*' }; if($p){break} }
Start-Sleep 20
powershell -NoProfile -ExecutionPolicy Bypass -File dev-shot.ps1 -Out "$Tag-a.png" -Keys 'e'
Start-Sleep 1
powershell -NoProfile -ExecutionPolicy Bypass -File dev-shot.ps1 -Out "$Tag-b.png" -Keys 'e'
Select-String -Path run\logs\latest.log -Pattern 'GPU glass disabled|Error compiling|Failed to link' | Select -First 3
